package org.mozartoz.truffle.translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.mozartoz.bootcompiler.ast.BinaryOp;
import org.mozartoz.bootcompiler.ast.BindStatement;
import org.mozartoz.bootcompiler.ast.CallStatement;
import org.mozartoz.bootcompiler.ast.CompoundStatement;
import org.mozartoz.bootcompiler.ast.Constant;
import org.mozartoz.bootcompiler.ast.Expression;
import org.mozartoz.bootcompiler.ast.IfStatement;
import org.mozartoz.bootcompiler.ast.LocalExpression;
import org.mozartoz.bootcompiler.ast.LocalStatement;
import org.mozartoz.bootcompiler.ast.MatchStatement;
import org.mozartoz.bootcompiler.ast.MatchStatementClause;
import org.mozartoz.bootcompiler.ast.NoElseStatement;
import org.mozartoz.bootcompiler.ast.ProcExpression;
import org.mozartoz.bootcompiler.ast.Record;
import org.mozartoz.bootcompiler.ast.RecordField;
import org.mozartoz.bootcompiler.ast.SkipStatement;
import org.mozartoz.bootcompiler.ast.StatAndExpression;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.ast.TryStatement;
import org.mozartoz.bootcompiler.ast.UnboundExpression;
import org.mozartoz.bootcompiler.ast.Variable;
import org.mozartoz.bootcompiler.ast.VariableOrRaw;
import org.mozartoz.bootcompiler.oz.False;
import org.mozartoz.bootcompiler.oz.OzArity;
import org.mozartoz.bootcompiler.oz.OzAtom;
import org.mozartoz.bootcompiler.oz.OzBuiltin;
import org.mozartoz.bootcompiler.oz.OzFeature;
import org.mozartoz.bootcompiler.oz.OzFloat;
import org.mozartoz.bootcompiler.oz.OzInt;
import org.mozartoz.bootcompiler.oz.OzLiteral;
import org.mozartoz.bootcompiler.oz.OzPatMatCapture;
import org.mozartoz.bootcompiler.oz.OzPatMatConjunction;
import org.mozartoz.bootcompiler.oz.OzPatMatOpenRecord;
import org.mozartoz.bootcompiler.oz.OzPatMatWildcard;
import org.mozartoz.bootcompiler.oz.OzRecord;
import org.mozartoz.bootcompiler.oz.OzRecordField;
import org.mozartoz.bootcompiler.oz.OzValue;
import org.mozartoz.bootcompiler.oz.True;
import org.mozartoz.bootcompiler.oz.UnitVal;
import org.mozartoz.bootcompiler.parser.OzPreprocessor.PreprocessorPosition;
import org.mozartoz.bootcompiler.symtab.Builtin;
import org.mozartoz.bootcompiler.symtab.Symbol;
import org.mozartoz.bootcompiler.util.FilePosition;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.TopLevelHandlerNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.IntBuiltinsFactory.DivNodeFactory;
import org.mozartoz.truffle.nodes.builtins.IntBuiltinsFactory.ModNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.HeadNodeGen;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.TailNodeGen;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.AddNodeFactory;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.MulNodeFactory;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.SubNodeFactory;
import org.mozartoz.truffle.nodes.builtins.RecordBuiltinsFactory.LabelNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.DotNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.GreaterThanNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.LesserThanNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.LesserThanOrEqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.NotEqualNodeFactory;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.control.AndNode;
import org.mozartoz.truffle.nodes.control.IfNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.control.SkipNode;
import org.mozartoz.truffle.nodes.control.TryNode;
import org.mozartoz.truffle.nodes.literal.BooleanLiteralNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNodeGen;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.literal.UnboundLiteralNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeArgNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchCaptureNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchDynamicArityNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchOpenRecordNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.Unit;

import scala.collection.JavaConversions;
import scala.util.parsing.input.OffsetPosition;
import scala.util.parsing.input.Position;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class Translator {

	static class Environment {
		private final Environment parent;
		private final FrameDescriptor frameDescriptor;

		public Environment(Environment parent, FrameDescriptor frameDescriptor) {
			this.parent = parent;
			this.frameDescriptor = frameDescriptor;
		}

		private String symbol2identifier(Symbol symbol) {
			return symbol.fullName().intern();
		}

		public FrameSlot findLocalVariable(Symbol symbol) {
			return frameDescriptor.findFrameSlot(symbol2identifier(symbol));
		}

		public FrameSlot addLocalVariable(Symbol symbol) {
			return frameDescriptor.addFrameSlot(symbol2identifier(symbol));
		}
	}

	private Environment environment = new Environment(null, new FrameDescriptor());
	private final Environment rootEnvironment = environment;

	public Translator() {
	}

	public FrameSlot addRootSymbol(Symbol symbol) {
		return rootEnvironment.addLocalVariable(symbol);
	}

	private void pushEnvironment(FrameDescriptor frameDescriptor) {
		environment = new Environment(environment, frameDescriptor);
	}

	private void popEnvironment() {
		environment = environment.parent;
	}

	public FrameSlotAndDepth findVariable(Symbol symbol) {
		int depth = 0;
		Environment environment = this.environment;

		while (environment != null) {
			FrameSlot slot = environment.findLocalVariable(symbol);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, depth);
			} else {
				environment = environment.parent;
				depth++;
			}
		}
		throw new Error(symbol.fullName());
	}

	public OzRootNode translateAST(String description, Statement ast, Function<OzNode, OzNode> wrap) {
		OzNode translated = translate(ast);
		OzNode wrapped = wrap.apply(translated);
		OzNode handler = new TopLevelHandlerNode(wrapped);

		SourceSection sourceSection = SourceSection.createUnavailable("top-level", description);
		return new OzRootNode(sourceSection, environment.frameDescriptor, handler, 0);
	}

	OzNode translate(Statement statement) {
		if (statement instanceof SkipStatement) {
			return new SkipNode();
		} else if (statement instanceof CompoundStatement) {
			CompoundStatement compound = (CompoundStatement) statement;
			return SequenceNode.sequence(map(compound.statements(), this::translate));
		} else if (statement instanceof LocalStatement) {
			LocalStatement local = (LocalStatement) statement;
			OzNode[] decls = map(local.declarations(), variable -> {
				FrameSlot slot = environment.addLocalVariable(variable.symbol());
				return new InitializeVarNode(slot);
			});
			return SequenceNode.sequence(decls, translate(local.statement()));
		} else if (statement instanceof BindStatement) {
			BindStatement bind = (BindStatement) statement;
			Expression left = bind.left();
			Expression right = bind.right();
			FrameSlotAndDepth leftSlot = null;
			if (left instanceof Variable) {
				leftSlot = findVariable(((Variable) left).symbol());
			}
			return t(statement, BindNodeGen.create(leftSlot, leftSlot.createReadNode(), translate(right)));
		} else if (statement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) statement;
			return new IfNode(translate(ifStatement.condition()),
					translate(ifStatement.trueStatement()),
					translate(ifStatement.falseStatement()));
		} else if (statement instanceof TryStatement) {
			TryStatement tryStatement = (TryStatement) statement;
			FrameSlotAndDepth exceptionVarSlot = findVariable(((Variable) tryStatement.exceptionVar()).symbol());
			return new TryNode(
					exceptionVarSlot,
					translate(tryStatement.body()),
					translate(tryStatement.catchBody()));
		} else if (statement instanceof MatchStatement) {
			MatchStatement matchStatement = (MatchStatement) statement;

			assert matchStatement.value() instanceof Variable;
			OzNode valueNode = translate(matchStatement.value());
			Statement elseStatement = matchStatement.elseStatement();

			assert !(elseStatement instanceof NoElseStatement);
			OzNode elseNode = translate(elseStatement);

			List<MatchStatementClause> clauses = new ArrayList<>(toJava(matchStatement.clauses()));
			Collections.reverse(clauses);

			OzNode caseNode = elseNode;
			for (MatchStatementClause clause : clauses) {
				caseNode = translateMatchClause(copy(valueNode), caseNode, clause);
			}

			return caseNode;
		} else if (statement instanceof CallStatement) {
			CallStatement callStatement = (CallStatement) statement;
			Expression callable = callStatement.callable();
			List<Expression> args = new ArrayList<>(toJava(callStatement.args()));

			OzNode[] argsNodes = new OzNode[args.size()];
			for (int i = 0; i < args.size(); i++) {
				argsNodes[i] = translate(args.get(i));
			}
			return t(statement, CallProcNodeGen.create(argsNodes, translate(callable)));
		}

		throw unknown("statement", statement);
	}

	OzNode translate(Expression expression) {
		if (expression instanceof Constant) {
			Constant constant = (Constant) expression;
			return translateConstantNode(constant.value());
		} else if (expression instanceof Record) {
			Record record = (Record) expression;
			List<RecordField> fields = new ArrayList<>(toJava(record.fields()));
			if (record.isCons()) {
				return buildCons(translate(fields.get(0).value()), translate(fields.get(1).value()));
			} else if (record.hasConstantArity()) {
				Arity arity = buildArity(record.getConstantArity());
				if (fields.isEmpty()) {
					return new LiteralNode(arity.getLabel());
				}
				OzNode[] values = new OzNode[fields.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = translate(fields.get(i).value());
				}
				return new RecordLiteralNode(arity, values);
			}
		} else if (expression instanceof Variable) {
			Variable variable = (Variable) expression;
			return findVariable(variable.symbol()).createReadNode();
		} else if (expression instanceof UnboundExpression) {
			return new UnboundLiteralNode();
		} else if (expression instanceof BinaryOp) {
			BinaryOp binaryOp = (BinaryOp) expression;
			return translateBinaryOp(binaryOp.operator(),
					translate(binaryOp.left()),
					translate(binaryOp.right()));
		} else if (expression instanceof ProcExpression) { // proc/fun literal
			ProcExpression procExpression = (ProcExpression) expression;
			pushEnvironment(new FrameDescriptor());

			int arity = procExpression.args().size();
			OzNode[] nodes = new OzNode[arity + 1];
			int i = 0;
			for (VariableOrRaw variable : toJava(procExpression.args())) {
				if (variable instanceof Variable) {
					FrameSlot argSlot = environment.addLocalVariable(((Variable) variable).symbol());
					nodes[i] = InitializeArgNodeGen.create(argSlot, new ReadArgumentNode(i));
					i++;
				} else {
					throw unknown("variable", variable);
				}
			}

			nodes[i] = translate(procExpression.body());

			OzNode procBody = SequenceNode.sequence(nodes);
			SourceSection sourceSection = t(expression);
			OzRootNode rootNode = new OzRootNode(sourceSection, environment.frameDescriptor, procBody, arity);
			RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
			popEnvironment();
			return new ProcDeclarationNode(callTarget);
		}

		throw unknown("expression", expression);
	}

	private OzNode translateMatchClause(OzNode valueNode, OzNode elseNode, MatchStatementClause clause) {
		List<OzNode> checks = new ArrayList<>();
		List<OzNode> bindings = new ArrayList<>();
		translateMatcher(clause.pattern(), valueNode, checks, bindings);
		OzNode body = translate(clause.body());
		final IfNode matchNode;
		if (clause.hasGuard()) {
			OzNode guard = translateGuard(clause.guard().get());
			// First the checks, then the bindings and then the guard (possibly using the bindings)
			bindings.add(guard);
			checks.add(SequenceNode.sequence(bindings.toArray(new OzNode[bindings.size()])));
			matchNode = new IfNode(new AndNode(checks.toArray(new OzNode[checks.size()])), body, elseNode);
		} else {
			matchNode = new IfNode(
					new AndNode(checks.toArray(new OzNode[checks.size()])),
					SequenceNode.sequence(bindings.toArray(new OzNode[bindings.size()]), body),
					elseNode);
		}
		return t(clause, matchNode);
	}

	// We manually translate a LocalExpression here. Other local are already converted to statements.
	private OzNode translateGuard(Expression guard) {
		LocalExpression guardLocal = (LocalExpression) guard;
		StatAndExpression guardExpr = ((StatAndExpression) guardLocal.body());
		Variable resultVar = (Variable) guardExpr.expression();
		FrameSlot slot = environment.addLocalVariable(resultVar.symbol());
		return SequenceNode.sequence(
				new InitializeVarNode(slot),
				translate(guardExpr.statement()),
				deref(translate(resultVar)));
	}

	private void translateMatcher(Object matcher, OzNode valueNode, List<OzNode> checks, List<OzNode> bindings) {
		if (matcher instanceof Constant) {
			translateMatcher(((Constant) matcher).value(), valueNode, checks, bindings);
		} else if (matcher instanceof OzPatMatWildcard) {
			// Nothing to do
		} else if (matcher instanceof OzPatMatCapture) {
			FrameSlotAndDepth slot = findVariable(((OzPatMatCapture) matcher).variable());
			bindings.add(PatternMatchCaptureNodeGen.create(slot.createReadNode(), copy(valueNode)));
		} else if (matcher instanceof OzPatMatConjunction) {
			for (OzValue part : toJava(((OzPatMatConjunction) matcher).parts())) {
				translateMatcher(part, valueNode, checks, bindings);
			}
		} else if (matcher instanceof OzFeature) {
			Object feature = translateFeature((OzFeature) matcher);
			checks.add(PatternMatchEqualNodeGen.create(feature, copy(valueNode)));
		} else if (matcher instanceof OzRecord) {
			OzRecord record = (OzRecord) matcher;
			if (record.isCons()) {
				checks.add(PatternMatchConsNodeGen.create(copy(valueNode)));
				OzValue head = record.values().apply(0);
				translateMatcher(head, HeadNodeGen.create(copy(valueNode)), checks, bindings);
				OzValue tail = record.values().apply(1);
				translateMatcher(tail, TailNodeGen.create(copy(valueNode)), checks, bindings);
			} else {
				Arity arity = buildArity(record.arity());
				checks.add(PatternMatchRecordNodeGen.create(arity, copy(valueNode)));
				for (OzRecordField field : toJava(record.fields())) {
					Object feature = translateFeature(field.feature());
					DotNode dotNode = DotNodeFactory.create(deref(copy(valueNode)), new LiteralNode(feature));
					translateMatcher(field.value(), dotNode, checks, bindings);
				}
			}
		} else if (matcher instanceof Record) {
			Record record = (Record) matcher;
			// First match the label
			translateMatcher(record.label(), LabelNodeFactory.create(copy(valueNode)), checks, bindings);
			// Then check if features match
			Object[] features = mapObjects(record.fields(), field -> {
				Constant constant = (Constant) field.feature();
				return translateFeature((OzFeature) constant.value());
			});
			checks.add(PatternMatchDynamicArityNodeGen.create(features, copy(valueNode)));

			int i = 0;
			for (RecordField field : toJava(record.fields())) {
				Object feature = features[i++];
				DotNode dotNode = DotNodeFactory.create(deref(copy(valueNode)), new LiteralNode(feature));
				translateMatcher(field.value(), dotNode, checks, bindings);
			}
		} else if (matcher instanceof OzPatMatOpenRecord) {
			OzPatMatOpenRecord record = (OzPatMatOpenRecord) matcher;
			Arity arity = buildArity(record.arity());
			checks.add(PatternMatchOpenRecordNodeGen.create(arity, copy(valueNode)));
			for (OzRecordField field : toJava(record.fields())) {
				Object feature = translateFeature(field.feature());
				DotNode dotNode = DotNodeFactory.create(deref(copy(valueNode)), new LiteralNode(feature));
				translateMatcher(field.value(), dotNode, checks, bindings);
			}
		} else {
			throw unknown("pattern matcher", matcher);
		}
	}

	private OzNode translateConstantNode(OzValue value) {
		if (value instanceof OzInt) {
			return new LongLiteralNode(((OzInt) value).value());
		} else if (value instanceof True) {
			return new BooleanLiteralNode(true);
		} else if (value instanceof False) {
			return new BooleanLiteralNode(false);
		} else {
			return new LiteralNode(translateConstantValue(value));
		}
	}

	private Object translateConstantValue(OzValue value) {
		if (value instanceof OzFeature) {
			return translateFeature((OzFeature) value);
		} else if (value instanceof OzFloat) {
			return ((OzFloat) value).value();
		} else if (value instanceof OzRecord) {
			OzRecord ozRecord = (OzRecord) value;
			if (ozRecord.isCons()) {
				Object left = translateConstantValue(ozRecord.fields().apply(0).value());
				Object right = translateConstantValue(ozRecord.fields().apply(1).value());
				return new OzCons(left, right);
			} else {
				Arity arity = buildArity(ozRecord.arity());
				Object[] values = mapObjects(ozRecord.values(), this::translateConstantValue);
				return org.mozartoz.truffle.runtime.OzRecord.buildRecord(arity, values);
			}
		} else if (value instanceof OzBuiltin) {
			Builtin builtin = ((OzBuiltin) value).builtin();
			return BuiltinsManager.getBuiltin(builtin.moduleName(), builtin.name());
		}
		throw unknown("value", value);
	}

	private static Object translateFeature(OzFeature feature) {
		if (feature instanceof OzInt) {
			return ((OzInt) feature).value();
		} else if (feature instanceof True) {
			return true;
		} else if (feature instanceof False) {
			return false;
		} else if (feature instanceof UnitVal) {
			return Unit.INSTANCE;
		} else if (feature instanceof OzAtom) {
			return translateAtom((OzAtom) feature);
		} else {
			throw unknown("feature", feature);
		}
	}

	private static Object translateLiteral(OzLiteral literal) {
		if (literal instanceof OzAtom) {
			return translateAtom((OzAtom) literal);
		} else {
			throw unknown("literal", literal);
		}
	}

	private static String translateAtom(OzAtom atom) {
		return atom.value().intern();
	}

	private OzNode buildCons(OzNode head, OzNode tail) {
		return ConsLiteralNodeGen.create(head, tail);
	}

	private Arity buildArity(OzArity arity) {
		Object[] features = mapObjects(arity.features(), Translator::translateFeature);
		return Arity.build(translateLiteral(arity.label()), features);
	}

	private OzNode translateBinaryOp(String operator, OzNode left, OzNode right) {
		left = deref(left);
		right = deref(right);
		switch (operator) {
		case "+":
			return AddNodeFactory.create(left, right);
		case "-":
			return SubNodeFactory.create(left, right);
		case "*":
			return MulNodeFactory.create(left, right);
		case "div":
			return DivNodeFactory.create(left, right);
		case "mod":
			return ModNodeFactory.create(left, right);
		case "==":
			return EqualNodeFactory.create(left, right);
		case "\\=":
			return NotEqualNodeFactory.create(left, right);
		case "<":
			return LesserThanNodeFactory.create(left, right);
		case "=<":
			return LesserThanOrEqualNodeFactory.create(left, right);
		case ">":
			return GreaterThanNodeFactory.create(left, right);
		case ".":
			return DotNodeFactory.create(left, right);
		default:
			throw unknown("operator", operator);
		}
	}

	private OzNode copy(OzNode node) {
		return NodeUtil.cloneNode(node);
	}

	private OzNode deref(OzNode node) {
		return DerefNodeGen.create(node);
	}

	private static <E> Collection<E> toJava(scala.collection.immutable.Iterable<E> scalaIterable) {
		return JavaConversions.asJavaCollection(scalaIterable);
	}

	private static <E> OzNode[] map(scala.collection.immutable.Iterable<E> scalaIterable, Function<E, OzNode> apply) {
		Collection<E> collection = toJava(scalaIterable);
		OzNode[] result = new OzNode[collection.size()];
		int i = 0;
		for (E element : collection) {
			result[i++] = apply.apply(element);
		}
		return result;
	}

	private static <E> Object[] mapObjects(scala.collection.immutable.Iterable<E> scalaIterable, Function<E, Object> apply) {
		Collection<E> collection = toJava(scalaIterable);
		Object[] result = new Object[collection.size()];
		int i = 0;
		for (E element : collection) {
			result[i++] = apply.apply(element);
		}
		return result;
	}

	private static RuntimeException unknown(String type, Object description) {
		return new RuntimeException("Unknown " + type + " " + description.getClass() + ": " + description);
	}

	private OzNode t(org.mozartoz.bootcompiler.ast.Node node, OzNode ozNode) {
		SourceSection sourceSection = t(node);
		ozNode.setSourceSection(sourceSection);
		return ozNode;
	}

	private static final Map<String, Source> SOURCES = new HashMap<>();

	private SourceSection t(org.mozartoz.bootcompiler.ast.Node node) {
		Position pos = node.pos();
		if (pos instanceof FilePosition) {
			return t(pos);
		} else {
			if (node instanceof CallStatement) {
				CallStatement callStatement = (CallStatement) node;
				if (!callStatement.args().isEmpty() &&
						callStatement.args().head().pos() instanceof FilePosition) {
					Position firstArgPos = callStatement.args().head().pos();
					return t(firstArgPos);
				}
			} else if (node instanceof BindStatement) {
				BindStatement bindStatement = (BindStatement) node;
				if (bindStatement.left().pos() instanceof FilePosition) {
					return t(bindStatement.left().pos());
				}
				if (bindStatement.right().pos() instanceof FilePosition) {
					return t(bindStatement.right().pos());
				}
			}
			return t(pos);
		}
	}

	private SourceSection t(Position pos) {
		if (pos instanceof FilePosition) {
			FilePosition filePosition = (FilePosition) pos;
			String canonicalPath;
			try {
				canonicalPath = filePosition.file().get().getCanonicalPath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Source source = SOURCES.computeIfAbsent(canonicalPath, file -> {
				try {
					return Source.fromFileName(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			PreprocessorPosition preprocessorPosition = (PreprocessorPosition) pos;
			OffsetPosition offsetPosition = (OffsetPosition) preprocessorPosition.getUnderlying();
			SourceSection sourceSection = source.createSection("", offsetPosition.offset(), 0);
			return source.createSection("", sourceSection.getStartLine());
		} else {
			return SourceSection.createUnavailable("unavailable", "");
		}
	}

}
