package org.mozartoz.truffle.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.mozartoz.bootcompiler.ast.BinaryOp;
import org.mozartoz.bootcompiler.ast.BindCommon;
import org.mozartoz.bootcompiler.ast.CallCommon;
import org.mozartoz.bootcompiler.ast.CallStatement;
import org.mozartoz.bootcompiler.ast.CompoundStatement;
import org.mozartoz.bootcompiler.ast.Constant;
import org.mozartoz.bootcompiler.ast.Expression;
import org.mozartoz.bootcompiler.ast.FailStatement;
import org.mozartoz.bootcompiler.ast.IfCommon;
import org.mozartoz.bootcompiler.ast.ListExpression;
import org.mozartoz.bootcompiler.ast.LocalCommon;
import org.mozartoz.bootcompiler.ast.MatchClauseCommon;
import org.mozartoz.bootcompiler.ast.MatchCommon;
import org.mozartoz.bootcompiler.ast.NoElseCommon;
import org.mozartoz.bootcompiler.ast.Node;
import org.mozartoz.bootcompiler.ast.ProcExpression;
import org.mozartoz.bootcompiler.ast.RaiseCommon;
import org.mozartoz.bootcompiler.ast.RawDeclarationOrVar;
import org.mozartoz.bootcompiler.ast.Record;
import org.mozartoz.bootcompiler.ast.RecordField;
import org.mozartoz.bootcompiler.ast.SkipStatement;
import org.mozartoz.bootcompiler.ast.StatAndExpression;
import org.mozartoz.bootcompiler.ast.StatOrExpr;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.ast.TailMarkerStatement;
import org.mozartoz.bootcompiler.ast.TryCommon;
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
import org.mozartoz.bootcompiler.symtab.Builtin;
import org.mozartoz.bootcompiler.symtab.Symbol;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.ExecuteValuesNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.TopLevelHandlerNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.FailNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseNodeFactory;
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
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.call.TailCallThrowerNode;
import org.mozartoz.truffle.nodes.control.AndNode;
import org.mozartoz.truffle.nodes.control.IfNode;
import org.mozartoz.truffle.nodes.control.NoElseNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.control.SkipNode;
import org.mozartoz.truffle.nodes.control.TryNode;
import org.mozartoz.truffle.nodes.literal.BooleanLiteralNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNodeGen;
import org.mozartoz.truffle.nodes.literal.EnsureOzLiteralNode;
import org.mozartoz.truffle.nodes.literal.ListLiteralNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.literal.MakeDynamicRecordNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.literal.UnboundLiteralNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeArgNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchDynamicArityNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchOpenRecordNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.Unit;

import scala.collection.JavaConversions;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.SourceSection;

public class Translator {

	static class Environment {
		private final Environment parent;
		private final FrameDescriptor frameDescriptor;
		private final String identifier;

		public Environment(Environment parent, FrameDescriptor frameDescriptor, String identifier) {
			this.parent = parent;
			this.frameDescriptor = frameDescriptor;
			this.identifier = identifier;
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

	private Environment environment = new Environment(null, new FrameDescriptor(), "<toplevel>");
	private final Environment rootEnvironment = environment;

	public Translator() {
	}

	public FrameSlot addRootSymbol(Symbol symbol) {
		return rootEnvironment.addLocalVariable(symbol);
	}

	private void pushEnvironment(FrameDescriptor frameDescriptor, String identifier) {
		environment = new Environment(environment, frameDescriptor, identifier);
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

	OzNode translate(StatOrExpr node) {
		if (node instanceof Expression) {
			// Literal expressions
			if (node instanceof Constant) {
				Constant constant = (Constant) node;
				return translateConstantNode(constant.value());
			} else if (node instanceof Record) {
				Record record = (Record) node;
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
				} else if (record.fields().size() == 0) {
					return new EnsureOzLiteralNode(translate(record.label()));
				} else {
					OzNode label = translate(record.label());
					OzNode[] features = map(record.fields(), field -> translate(field.feature()));
					OzNode[] values = map(record.fields(), field -> translate(field.value()));
					return new MakeDynamicRecordNode(label, features, values);
				}
			} else if (node instanceof ListExpression) {
				ListExpression list = (ListExpression) node;
				OzNode[] elements = map(list.elements(), this::translate);
				return new ListLiteralNode(elements);
			} else if (node instanceof Variable) {
				Variable variable = (Variable) node;
				return t(node, findVariable(variable.symbol()).createReadNode());
			} else if (node instanceof UnboundExpression) {
				return t(node, new UnboundLiteralNode());
			} else if (node instanceof BinaryOp) {
				BinaryOp binaryOp = (BinaryOp) node;
				return translateBinaryOp(binaryOp.operator(),
						translate(binaryOp.left()),
						translate(binaryOp.right()));
			} else if (node instanceof ProcExpression) { // proc/fun literal
				return translateProc((ProcExpression) node);
			}
		}

		// Structural nodes
		if (node instanceof CompoundStatement) {
			CompoundStatement compound = (CompoundStatement) node;
			return SequenceNode.sequence(map(compound.statements(), this::translate));
		} else if (node instanceof StatAndExpression) {
			StatAndExpression statAndExpr = (StatAndExpression) node;
			return SequenceNode.sequence(translate(statAndExpr.statement()), translate(statAndExpr.expression()));
		} else if (node instanceof LocalCommon) {
			LocalCommon local = (LocalCommon) node;
			List<OzNode> decls = new ArrayList<>(local.declarations().size());
			for (RawDeclarationOrVar variable : toJava(local.declarations())) {
				Symbol symbol = ((Variable) variable).symbol();
				FrameSlot slot = environment.addLocalVariable(symbol);
				// No need to initialize captures, the slot will be set directly
				if (!symbol.isCapture()) {
					decls.add(t(variable, new InitializeVarNode(slot)));
				}
			}
			return SequenceNode.sequence(decls.toArray(new OzNode[decls.size()]), translate(local.body()));
		} else if (node instanceof CallCommon) {
			return translateCall((CallCommon) node);
		} else if (node instanceof TailMarkerStatement) {
			return translateTailCall(((TailMarkerStatement) node).call());
		} else if (node instanceof SkipStatement) {
			return new SkipNode();
		} else if (node instanceof BindCommon) {
			BindCommon bind = (BindCommon) node;
			Expression left = bind.left();
			Expression right = bind.right();
			return t(node, BindNodeGen.create(translate(left), translate(right)));
		} else if (node instanceof IfCommon) {
			IfCommon ifNode = (IfCommon) node;
			return new IfNode(translate(ifNode.condition()),
					translate(ifNode.truePart()),
					translate(ifNode.falsePart()));
		} else if (node instanceof MatchCommon) {
			return translateMatch((MatchCommon) node);
		} else if (node instanceof TryCommon) {
			TryCommon tryNode = (TryCommon) node;
			FrameSlotAndDepth exceptionVarSlot = findVariable(((Variable) tryNode.exceptionVar()).symbol());
			return new TryNode(
					exceptionVarSlot.createWriteNode(),
					translate(tryNode.body()),
					translate(tryNode.catchBody()));
		} else if (node instanceof RaiseCommon) {
			RaiseCommon raiseNode = (RaiseCommon) node;
			return RaiseNodeFactory.create(translate(raiseNode.exception()));
		} else if (node instanceof FailStatement) {
			return FailNodeFactory.create();
		}

		throw unknown("expression or statement", node);
	}

	public OzNode translateProc(ProcExpression procExpression) {
		SourceSection sourceSection = t(procExpression);
		pushEnvironment(new FrameDescriptor(), sourceSection.getIdentifier());
		try {
			int arity = procExpression.args().size();
			OzNode[] nodes = new OzNode[arity + 1];
			int i = 0;
			for (VariableOrRaw variable : toJava(procExpression.args())) {
				if (variable instanceof Variable) {
					FrameSlot argSlot = environment.addLocalVariable(((Variable) variable).symbol());
					nodes[i] = new InitializeArgNode(argSlot, i);
					i++;
				} else {
					throw unknown("variable", variable);
				}
			}

			nodes[i] = translate(procExpression.body());

			OzNode procBody = SequenceNode.sequence(nodes);
			OzRootNode rootNode = new OzRootNode(sourceSection, environment.frameDescriptor, procBody, arity);
			RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
			return new ProcDeclarationNode(callTarget);
		} finally {
			popEnvironment();
		}
	}

	private OzNode translateCall(CallCommon call) {
		OzNode receiver = translate(call.callable());
		OzNode[] argsNodes = map(call.args(), this::translate);
		return t(call, CallNode.create(receiver, new ExecuteValuesNode(argsNodes)));
	}

	private OzNode translateTailCall(CallStatement call) {
		OzNode receiver = translate(call.callable());
		OzNode[] argsNodes = map(call.args(), this::translate);
		return t(call, new TailCallThrowerNode(receiver, new ExecuteValuesNode(argsNodes)));
	}

	private OzNode translateMatch(MatchCommon match) {
		assert match.value() instanceof Variable;
		OzNode valueNode = translate(match.value());
		StatOrExpr elsePart = match.elsePart();

		final OzNode elseNode;
		if (elsePart instanceof NoElseCommon) {
			elseNode = t(elsePart, new NoElseNode(valueNode));
		} else {
			elseNode = translate(elsePart);
		}

		List<MatchClauseCommon> clauses = new ArrayList<MatchClauseCommon>(toJava(match.clauses()));
		Collections.reverse(clauses);

		OzNode caseNode = elseNode;
		for (MatchClauseCommon clause : clauses) {
			caseNode = translateMatchClause(copy(valueNode), caseNode, clause);
		}

		return caseNode;
	}

	private OzNode translateMatchClause(OzNode valueNode, OzNode elseNode, MatchClauseCommon clause) {
		List<OzNode> checks = new ArrayList<>();
		List<OzNode> bindings = new ArrayList<>();
		translateMatcher(clause.pattern(), valueNode, checks, bindings);
		OzNode body = translate(clause.body());
		final IfNode matchNode;
		if (clause.hasGuard()) {
			OzNode guard = translate(clause.guard().get());
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
		return t((Node) clause, matchNode);
	}

	private void translateMatcher(Object matcher, OzNode valueNode, List<OzNode> checks, List<OzNode> bindings) {
		if (matcher instanceof Constant) {
			translateMatcher(((Constant) matcher).value(), valueNode, checks, bindings);
		} else if (matcher instanceof OzPatMatWildcard) {
			// Nothing to do
		} else if (matcher instanceof OzPatMatCapture) {
			FrameSlotAndDepth slot = findVariable(((OzPatMatCapture) matcher).variable());
			// Set the slot directly since the variable is born here
			assert slot.getDepth() == 0;
			bindings.add(new InitializeTmpNode(slot.getSlot(), copy(valueNode)));
		} else if (matcher instanceof Variable) {
			Variable var = (Variable) matcher;
			OzNode left = findVariable(var.symbol()).createReadNode();
			checks.add(EqualNodeFactory.create(deref(left), deref(copy(valueNode))));
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
		return DerefNode.create(node);
	}

	private static <E> Collection<E> toJava(scala.collection.Iterable<E> scalaIterable) {
		return JavaConversions.asJavaCollection(scalaIterable);
	}

	private static <E> OzNode[] map(scala.collection.Iterable<E> scalaIterable, Function<E, OzNode> apply) {
		Collection<E> collection = toJava(scalaIterable);
		OzNode[] result = new OzNode[collection.size()];
		int i = 0;
		for (E element : collection) {
			result[i++] = apply.apply(element);
		}
		return result;
	}

	private static <E> Object[] mapObjects(scala.collection.Iterable<E> scalaIterable, Function<E, Object> apply) {
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

	private <T extends OzNode> T t(Node node, T ozNode) {
		SourceSection sourceSection = t(node);
		ozNode.setSourceSection(sourceSection);
		return ozNode;
	}

	private <T extends OzNode> T t(Object node, T ozNode) {
		return t((Node) node, ozNode);
	}

	private SourceSection t(Node node) {
		if (node.section() != null) {
			return sectionWithIdentifier(node.section(), environment.identifier);
		} else {
			return SourceSection.createUnavailable("unavailable", "");
		}
	}

	private SourceSection t(ProcExpression node) {
		if (node.section() != null) {
			if (node.name().isDefined()) {
				String identifier = ((Variable) node.name().get()).symbol().name();
				return sectionWithIdentifier(node.section(), identifier);
			} else {
				return node.section();
			}
		} else {
			return SourceSection.createUnavailable("unavailable", "");
		}
	}

	private SourceSection sectionWithIdentifier(SourceSection section, String identifier) {
		if (section.getSource() == null) {
			return section;
		}
		return section.getSource().createSection(identifier,
				section.getStartLine(), section.getStartColumn(), section.getCharIndex(), section.getCharLength());
	}

}
