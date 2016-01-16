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
import org.mozartoz.bootcompiler.ast.FunExpression;
import org.mozartoz.bootcompiler.ast.IfExpression;
import org.mozartoz.bootcompiler.ast.LocalExpression;
import org.mozartoz.bootcompiler.ast.LocalStatement;
import org.mozartoz.bootcompiler.ast.Record;
import org.mozartoz.bootcompiler.ast.RecordField;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.ast.Variable;
import org.mozartoz.bootcompiler.ast.VariableOrRaw;
import org.mozartoz.bootcompiler.oz.OzAtom;
import org.mozartoz.bootcompiler.oz.OzBuiltin;
import org.mozartoz.bootcompiler.oz.OzInt;
import org.mozartoz.bootcompiler.oz.OzRecord;
import org.mozartoz.bootcompiler.oz.OzValue;
import org.mozartoz.bootcompiler.parser.OzParser;
import org.mozartoz.bootcompiler.symtab.Program;
import org.mozartoz.bootcompiler.transform.Desugar;
import org.mozartoz.bootcompiler.transform.DesugarClass;
import org.mozartoz.bootcompiler.transform.DesugarFunctor;
import org.mozartoz.bootcompiler.transform.Namer;
import org.mozartoz.bootcompiler.transform.PatternMatcher;
import org.mozartoz.truffle.nodes.AddNodeGen;
import org.mozartoz.truffle.nodes.CallFunctionNodeGen;
import org.mozartoz.truffle.nodes.ConsLiteralNode;
import org.mozartoz.truffle.nodes.DotNodeGen;
import org.mozartoz.truffle.nodes.EqualNodeGen;
import org.mozartoz.truffle.nodes.FunctionDeclarationNode;
import org.mozartoz.truffle.nodes.IfNode;
import org.mozartoz.truffle.nodes.LiteralNode;
import org.mozartoz.truffle.nodes.LongLiteralNode;
import org.mozartoz.truffle.nodes.MulNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.ReadArgumentNode;
import org.mozartoz.truffle.nodes.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.SequenceNode;
import org.mozartoz.truffle.nodes.ShowNodeGen;
import org.mozartoz.truffle.nodes.SubNodeGen;
import org.mozartoz.truffle.nodes.WriteLocalVariableNode;
import org.mozartoz.truffle.runtime.Nil;

import scala.collection.JavaConversions;
import scala.collection.immutable.HashSet;
import scala.util.parsing.combinator.Parsers.ParseResult;
import scala.util.parsing.input.CharSequenceReader;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

public class Translator {

	static class Environment {
		private final Environment parent;
		private final FrameDescriptor frameDescriptor;

		public Environment(Environment parent, FrameDescriptor frameDescriptor) {
			this.parent = parent;
			this.frameDescriptor = frameDescriptor;
		}
	}

	static class FrameSlotAndDepth {
		private FrameSlot slot;
		private int depth;

		public FrameSlotAndDepth(FrameSlot frameSlot, int depth) {
			this.slot = frameSlot;
			this.depth = depth;
		}
	}

	private Environment environment = new Environment(null, new FrameDescriptor());

	public Translator() {
	}

	private void pushEnvironment(FrameDescriptor frameDescriptor) {
		environment = new Environment(environment, frameDescriptor);
	}

	private void popEnvironment() {
		environment = environment.parent;
	}

	public FrameSlotAndDepth findVariable(String name) {
		int depth = 0;
		Environment environment = this.environment;

		while (environment != null) {
			FrameSlot slot = environment.frameDescriptor.findFrameSlot(name);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, depth);
			} else {
				environment = environment.parent;
				depth++;
			}
		}

		throw new RuntimeException(name);
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
		if (statement instanceof CompoundStatement) {
			CompoundStatement compoundStatement = (CompoundStatement) statement;
			List<OzNode> stmts = new ArrayList<>();
			for (Statement sub : JavaConversions.asJavaCollection(compoundStatement.statements())) {
				stmts.add(translate(sub));
			}
			return new SequenceNode(stmts.toArray(new OzNode[stmts.size()]));
		} else if (statement instanceof LocalStatement) {
			LocalStatement localStatement = (LocalStatement) statement;
			FrameDescriptor frameDescriptor = environment.frameDescriptor;
			for (Variable variable : JavaConversions.asJavaCollection(localStatement.declarations())) {
				frameDescriptor.addFrameSlot(variable.symbol().name());
			}

			// What if the variable is declared again later?
			// pushEnvironment(frameDescriptor);
			try {
				return translate(localStatement.statement());
			} finally {
				// popEnvironment();
			}
		} else if (statement instanceof BindStatement) {
			BindStatement bindStatement = (BindStatement) statement;
			Expression left = bindStatement.left();
			if (left instanceof Variable) {
				FrameSlotAndDepth frameSlotAndDepth = findVariable(((Variable) left).symbol().name());
				if (frameSlotAndDepth.depth == 0) {
					return new WriteLocalVariableNode(frameSlotAndDepth.slot, translate(bindStatement.right()));
				} else {
					throw new RuntimeException("" + frameSlotAndDepth.depth);
				}
			}
		} else if (statement instanceof CallStatement) {
			CallStatement callStatement = (CallStatement) statement;
			Expression callable = callStatement.callable();
			List<Expression> args = new ArrayList<>(JavaConversions.asJavaCollection(callStatement.args()));

			if (callable instanceof LocalExpression && ((LocalExpression) callable).declarations().head().symbol().name().equals("Show")) {
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
			}
		} else if (expression instanceof Variable) {
			FrameSlotAndDepth frameSlotAndDepth = findVariable(((Variable) expression).symbol().name());
			if (frameSlotAndDepth.depth == 0) {
				return new ReadLocalVariableNode(frameSlotAndDepth.slot);
			} else if (frameSlotAndDepth.depth == 1) {
				return new ReadCapturedVariableNode(frameSlotAndDepth.slot);
			} else {
				throw new RuntimeException("" + frameSlotAndDepth.depth);
			}
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
		} else if (expression instanceof FunExpression) {
			FunExpression funExpression = (FunExpression) expression;
			FrameDescriptor frameDescriptor = new FrameDescriptor();
			for (VariableOrRaw variable : JavaConversions.asJavaCollection(funExpression.args())) {
				if (variable instanceof Variable) {
					frameDescriptor.addFrameSlot(((Variable) variable).symbol().name());
				} else {
					throw new RuntimeException();
				}
			}

			OzNode body;
			pushEnvironment(frameDescriptor);
			try {
				body = translate(funExpression.body());
			} finally {
				popEnvironment();
			}

			OzNode setArgs = new WriteLocalVariableNode(frameDescriptor.getSlots().get(0), new ReadArgumentNode());
			OzNode funBody = new SequenceNode(setArgs, body);
			return new FunctionDeclarationNode(frameDescriptor, funBody);
		} else if (expression instanceof CallExpression) {
			CallExpression callExpression = (CallExpression) expression;
			Expression callable = callExpression.callable();
			List<Expression> args = new ArrayList<>(JavaConversions.asJavaCollection(callExpression.args()));

			if (callable instanceof Constant && ((Constant) callable).value() instanceof OzBuiltin) {
				String name = ((OzBuiltin) ((Constant) callable).value()).builtin().name();
				if (args.size() == 1 && name.equals("-1")) {
					return translate(new BinaryOp(args.get(0), "-", new Constant(new OzInt(1))));
				} else if (args.size() == 2) {
					OzNode left = translate(args.get(0));
					OzNode right = translate(args.get(1));
					return translateBinaryOp(name, left, right);
				}
			}

			if (args.size() != 1) {
				throw new RuntimeException();
			}

			return CallFunctionNodeGen.create(translate(callable), translate(args.get(0)));
		}

		throw new RuntimeException("Unknown expression: " + expression.getClass() + ": " + expression);
	}

	private OzNode translateValue(OzValue ozValue) {
		if (ozValue instanceof OzInt) {
			return new LongLiteralNode(((OzInt) ozValue).value());
		} else if (ozValue instanceof OzAtom) {
			String atom = ((OzAtom) ozValue).value();
			if (atom.equals("nil")) {
				return new LiteralNode(Nil.INSTANCE);
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

	private OzNode buildCons(OzNode head, OzNode tail) {
		return new ConsLiteralNode(head, tail);
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
