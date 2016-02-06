package org.mozartoz.truffle.translator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mozartoz.bootcompiler.Main;
import org.mozartoz.bootcompiler.ast.BinaryOp;
import org.mozartoz.bootcompiler.ast.BindStatement;
import org.mozartoz.bootcompiler.ast.CallExpression;
import org.mozartoz.bootcompiler.ast.CallStatement;
import org.mozartoz.bootcompiler.ast.CompoundStatement;
import org.mozartoz.bootcompiler.ast.Constant;
import org.mozartoz.bootcompiler.ast.Expression;
import org.mozartoz.bootcompiler.ast.IfExpression;
import org.mozartoz.bootcompiler.ast.LocalExpression;
import org.mozartoz.bootcompiler.ast.LocalStatement;
import org.mozartoz.bootcompiler.ast.ProcExpression;
import org.mozartoz.bootcompiler.ast.Record;
import org.mozartoz.bootcompiler.ast.RecordField;
import org.mozartoz.bootcompiler.ast.SkipStatement;
import org.mozartoz.bootcompiler.ast.StatAndExpression;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.ast.Variable;
import org.mozartoz.bootcompiler.ast.VariableOrRaw;
import org.mozartoz.bootcompiler.oz.OzArity;
import org.mozartoz.bootcompiler.oz.OzAtom;
import org.mozartoz.bootcompiler.oz.OzBuiltin;
import org.mozartoz.bootcompiler.oz.OzFeature;
import org.mozartoz.bootcompiler.oz.OzInt;
import org.mozartoz.bootcompiler.oz.OzLiteral;
import org.mozartoz.bootcompiler.oz.OzRecord;
import org.mozartoz.bootcompiler.oz.OzValue;
import org.mozartoz.bootcompiler.oz.UnitVal;
import org.mozartoz.bootcompiler.parser.OzParser;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.bootcompiler.symtab.Symbol;
import org.mozartoz.bootcompiler.transform.Desugar;
import org.mozartoz.bootcompiler.transform.DesugarClass;
import org.mozartoz.bootcompiler.transform.DesugarFunctor;
import org.mozartoz.bootcompiler.transform.Namer;
import org.mozartoz.bootcompiler.transform.PatternMatcher;
import org.mozartoz.truffle.nodes.IfNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.SequenceNode;
import org.mozartoz.truffle.nodes.SkipNode;
import org.mozartoz.truffle.nodes.builtins.AddNodeGen;
import org.mozartoz.truffle.nodes.builtins.DotNodeGen;
import org.mozartoz.truffle.nodes.builtins.EqualNodeGen;
import org.mozartoz.truffle.nodes.builtins.MulNodeGen;
import org.mozartoz.truffle.nodes.builtins.ShowNodeGen;
import org.mozartoz.truffle.nodes.builtins.SubNodeGen;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNodeGen;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.local.BindVariablesNode;
import org.mozartoz.truffle.nodes.local.InitializeArgNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.Nil;
import org.mozartoz.truffle.runtime.Unit;

import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.util.parsing.combinator.Parsers.ParseResult;
import scala.util.parsing.input.CharSequenceReader;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.object.Shape;

public class Translator {

	static class Environment {
		private final Environment parent;
		private final FrameDescriptor frameDescriptor;

		public Environment(Environment parent, FrameDescriptor frameDescriptor) {
			this.parent = parent;
			this.frameDescriptor = frameDescriptor;
		}
	}

	private Environment environment = new Environment(null, new FrameDescriptor());
	private final Environment rootEnvironment = environment;

	private static long id = 0;

	public Translator() {
	}

	private void pushEnvironment(FrameDescriptor frameDescriptor) {
		environment = new Environment(environment, frameDescriptor);
	}

	private void popEnvironment() {
		environment = environment.parent;
	}

	private long nextID() {
		return id++;
	}

	public FrameSlotAndDepth findVariable(Symbol symbol) {
		int depth = 0;
		Environment environment = this.environment;

		while (environment != null) {
			FrameSlot slot = environment.frameDescriptor.findFrameSlot(symbol);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, depth);
			} else {
				environment = environment.parent;
				depth++;
			}
		}

		if (symbol.name().equals("<Base>")) {
			return new FrameSlotAndDepth(
					rootEnvironment.frameDescriptor.findOrAddFrameSlot(symbol),
					depth - 1);
		}

		throw new RuntimeException(symbol.fullName());
	}

	public OzRootNode parseAndTranslate(String code) {
		Program program = new Program(false);
		// program.baseDeclarations().$plus$eq("Show");
		OzParser parser = new OzParser();

		String[] builtinTypes = { "Value", "Number", "Float", "Int" };

		List<String> builtins = new ArrayList<>();
		for (String buitinType : builtinTypes) {
			builtins.add("/home/eregon/code/mozart-graal/bootcompiler/Mod" + buitinType + "-builtin.json");
		}
		Main.loadModuleDefs(program, JavaConversions.asScalaBuffer(builtins).toList());

		// Add base defs
		code = "local Base Show in " + code + " end";

		CharSequenceReader reader = new CharSequenceReader(code);
		HashSet<String> defines = new HashSet<String>();// .$plus("Show");
		ParseResult<Statement> result = parser.parseStatement(reader, new File("test.oz"), defines);
		if (!result.successful()) {
			System.err.println("Parse error at " + result.next().pos().toString() + "\n" + result + "\n" + result.next().pos().longString());
			throw new RuntimeException();
		}

		program.rawCode_$eq(result.get());

		Namer.apply(program);
		DesugarFunctor.apply(program);
		DesugarClass.apply(program);
		Desugar.apply(program);
		PatternMatcher.apply(program);
		// ConstantFolding.apply(program);
		// Unnester.apply(program);
		// Flattener.apply(program);

		Statement ast = program.rawCode();
		// System.out.println(ast);

		OzNode translated = translate(ast);
		return new OzRootNode(environment.frameDescriptor, translated);
	}

	OzNode translate(Statement statement) {
		if (statement instanceof SkipStatement) {
			return new SkipNode();
		} else if (statement instanceof CompoundStatement) {
			CompoundStatement compoundStatement = (CompoundStatement) statement;
			List<OzNode> stmts = new ArrayList<>();
			for (Statement sub : JavaConversions.asJavaCollection(compoundStatement.statements())) {
				stmts.add(translate(sub));
			}
			return new SequenceNode(stmts.toArray(new OzNode[stmts.size()]));
		} else if (statement instanceof LocalStatement) {
			LocalStatement localStatement = (LocalStatement) statement;
			FrameDescriptor frameDescriptor = environment.frameDescriptor;

			OzNode[] nodes = new OzNode[localStatement.declarations().size() + 1];
			int i = 0;
			for (Variable variable : JavaConversions.asJavaCollection(localStatement.declarations())) {
				FrameSlot slot = frameDescriptor.addFrameSlot(variable.symbol());
				nodes[i++] = new InitializeVarNode(slot);
			}
			nodes[i] = translate(localStatement.statement());
			return new SequenceNode(nodes);
		} else if (statement instanceof BindStatement) {
			BindStatement bindStatement = (BindStatement) statement;
			Expression left = bindStatement.left();
			Expression right = bindStatement.right();
			if (left instanceof Variable) {
				final FrameSlotAndDepth leftSlot = findVariable(((Variable) left).symbol());
				if (right instanceof Variable) {
					final FrameSlotAndDepth rightSlot = findVariable(((Variable) right).symbol());
					return new BindVariablesNode(leftSlot, rightSlot);
				} else {
					return leftSlot.createWriteNode(translate(right));
				}
			}
		} else if (statement instanceof CallStatement) {
			CallStatement callStatement = (CallStatement) statement;
			Expression callable = callStatement.callable();
			List<Expression> args = new ArrayList<>(JavaConversions.asJavaCollection(callStatement.args()));

			if (callable instanceof Variable && ((Variable) callable).symbol().name().equals("Show")) {
				return ShowNodeGen.create(translate(args.get(0)));
			}
		}

		throw new RuntimeException("Unknown statement: " + statement.getClass() + ": " + statement);
	}

	OzNode translate(Expression expression) {
		if (expression instanceof Constant) {
			Constant constant = (Constant) expression;
			OzValue ozValue = constant.value();
			OzNode translated = translateValue(ozValue);
			if (translated != null) {
				return translated;
			}
		} else if (expression instanceof Record) {
			Record record = (Record) expression;
			List<RecordField> fields = new ArrayList<>(JavaConversions.asJavaCollection(record.fields()));
			if (record.isCons()) {
				return buildCons(translate(fields.get(0).value()), translate(fields.get(1).value()));
			} else if (record.isTuple()) {

			} else {
				if (record.hasConstantArity()) {
					OzNode[] values = new OzNode[fields.size()];
					for (int i = 0; i < values.length; i++) {
						values[i] = translate(fields.get(i).value());
					}
					return new RecordLiteralNode(buildArity(record.getConstantArity()), values);
				}
			}
		} else if (expression instanceof Variable) {
			Variable variable = (Variable) expression;
			FrameSlotAndDepth frameSlotAndDepth = findVariable(variable.symbol());
			return frameSlotAndDepth.createReadNode();
		} else if (expression instanceof BinaryOp) {
			BinaryOp binaryOp = (BinaryOp) expression;
			OzNode left = translate(binaryOp.left());
			OzNode right = translate(binaryOp.right());
			return translateBinaryOp(binaryOp.operator(), left, right);
		} else if (expression instanceof IfExpression) {
			IfExpression ifExpression = (IfExpression) expression;
			return new IfNode(translate(ifExpression.condition()),
					translate(ifExpression.trueExpression()),
					translate(ifExpression.falseExpression()));
		} else if (expression instanceof LocalExpression) {
			LocalExpression localExpression = (LocalExpression) expression;
			FrameDescriptor frameDescriptor = environment.frameDescriptor;

			OzNode[] nodes = new OzNode[localExpression.declarations().size() + 1];
			int i = 0;
			for (Variable variable : JavaConversions.asJavaCollection(localExpression.declarations())) {
				FrameSlot slot = frameDescriptor.addFrameSlot(variable.symbol());
				nodes[i++] = new InitializeVarNode(slot);
			}
			nodes[i] = translate(localExpression.expression());
			return new SequenceNode(nodes);
		} else if (expression instanceof ProcExpression) {
			ProcExpression procExpression = (ProcExpression) expression;
			FrameDescriptor frameDescriptor = new FrameDescriptor();

			OzNode[] nodes = new OzNode[procExpression.args().size() + 1];
			int i = 0;
			for (VariableOrRaw variable : JavaConversions.asJavaCollection(procExpression.args())) {
				if (variable instanceof Variable) {
					FrameSlot argSlot = frameDescriptor.addFrameSlot(((Variable) variable).symbol());
					nodes[i] = InitializeArgNodeGen.create(argSlot, new ReadArgumentNode(i));
					i++;
				} else {
					throw new RuntimeException();
				}
			}

			OzNode body;
			pushEnvironment(frameDescriptor);
			try {
				body = translate(procExpression.body());
			} finally {
				popEnvironment();
			}

			nodes[i] = body;

			OzNode procBody = new SequenceNode(nodes);
			return new ProcDeclarationNode(frameDescriptor, procBody);
		} else if (expression instanceof CallExpression) {
			CallExpression callExpression = (CallExpression) expression;
			Expression callable = callExpression.callable();
			List<Expression> args = new ArrayList<>(JavaConversions.asJavaCollection(callExpression.args()));

			if (callable instanceof Constant && ((Constant) callable).value() instanceof OzBuiltin) {
				return translateBuiltin(callable, args);
			}

			OzNode[] argsNodes = new OzNode[args.size() + 1];
			for (int i = 0; i < args.size(); i++) {
				argsNodes[i] = translate(args.get(i));
			}

			// The value of the last implicit argument is returned,
			// so we create a local to hold the result.
			FrameDescriptor frameDescriptor = environment.frameDescriptor;
			FrameSlot resultSlot = frameDescriptor.addFrameSlot("CallExpression-result" + nextID());
			argsNodes[argsNodes.length - 1] = new ReadLocalVariableNode(resultSlot);

			return new SequenceNode(
					new InitializeVarNode(resultSlot),
					CallProcNodeGen.create(argsNodes, translate(callable)),
					new ReadLocalVariableNode(resultSlot));
		} else if (expression instanceof StatAndExpression) {
			StatAndExpression statAndExpression = (StatAndExpression) expression;
			return new SequenceNode(translate(statAndExpression.statement()), translate(statAndExpression.expression()));
		}

		throw new RuntimeException("Unknown expression: " + expression.getClass() + ": " + expression);
	}

	private OzNode translateValue(OzValue ozValue) {
		if (ozValue instanceof OzInt) {
			return new LongLiteralNode(((OzInt) ozValue).value());
		} else if (ozValue instanceof UnitVal) {
			return new LiteralNode(Unit.INSTANCE);
		} else if (ozValue instanceof OzAtom) {
			String atom = ((OzAtom) ozValue).value();
			if (atom.equals("nil")) {
				return new LiteralNode(Nil.INSTANCE);
			} else {
				return new LiteralNode(atom.intern());
			}
		} else if (ozValue instanceof OzRecord) {
			OzRecord ozRecord = (OzRecord) ozValue;
			if (ozRecord.isCons()) {
				OzNode left = translateValue(ozRecord.fields().apply(0).value());
				OzNode right = translateValue(ozRecord.fields().apply(1).value());
				return buildCons(left, right);
			}
		}
		return null;
	}

	private static Object translateFeature(OzFeature feature) {
		if (feature instanceof OzInt) {
			return ((OzInt) feature).value();
		} else if (feature instanceof OzAtom) {
			return ((OzAtom) feature).value().intern();
		} else {
			throw new RuntimeException();
		}
	}

	private static Object translateLiteral(OzLiteral literal) {
		if (literal instanceof OzAtom) {
			return ((OzAtom) literal).value().intern();
		} else {
			throw new RuntimeException();
		}
	}

	private OzNode translateBuiltin(Expression callable, List<Expression> args) {
		String name = ((OzBuiltin) ((Constant) callable).value()).builtin().name();
		if (args.size() == 1) {
			if (name.equals("+1") || name.equals("-1")) {
				String op = name.substring(0, 1);
				return translate(new BinaryOp(args.get(0), op, new Constant(new OzInt(1))));
			}
		} else if (args.size() == 2) {
			OzNode left = translate(args.get(0));
			OzNode right = translate(args.get(1));
			return translateBinaryOp(name, left, right);
		}
		throw new RuntimeException("Unknown builtin: " + name);
	}

	private OzNode buildCons(OzNode head, OzNode tail) {
		return ConsLiteralNodeGen.create(head, tail);
	}

	private Arity buildArity(OzArity arity) {
		return new Arity(translateLiteral(arity.label()), arity2Shape(arity));
	}

	private static Object SOME_OBJECT = new Object();

	private static Shape arity2Shape(OzArity arity) {
		Shape shape = Arity.BASE;
		for (OzFeature feature : JavaConversions.asJavaCollection(arity.features())) {
			shape = shape.defineProperty(translateFeature(feature), SOME_OBJECT, 0);
		}
		return shape;
	}

	private OzNode translateBinaryOp(String operator, OzNode left, OzNode right) {
		switch (operator) {
		case "+":
			return AddNodeGen.create(left, right);
		case "-":
			return SubNodeGen.create(left, right);
		case "*":
			return MulNodeGen.create(left, right);
		case "==":
			return EqualNodeGen.create(left, right);
		case ".":
			return DotNodeGen.create(left, right);
		default:
			throw new RuntimeException(operator);
		}
	}

}