package org.mozartoz.truffle.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.mozartoz.bootcompiler.ast.BinaryOp;
import org.mozartoz.bootcompiler.ast.BindCommon;
import org.mozartoz.bootcompiler.ast.CallCommon;
import org.mozartoz.bootcompiler.ast.ClearVarsCommon;
import org.mozartoz.bootcompiler.ast.CompoundStatement;
import org.mozartoz.bootcompiler.ast.Constant;
import org.mozartoz.bootcompiler.ast.Expression;
import org.mozartoz.bootcompiler.ast.FailStatement;
import org.mozartoz.bootcompiler.ast.ForStatement;
import org.mozartoz.bootcompiler.ast.IfCommon;
import org.mozartoz.bootcompiler.ast.ListExpression;
import org.mozartoz.bootcompiler.ast.LocalCommon;
import org.mozartoz.bootcompiler.ast.MatchClauseCommon;
import org.mozartoz.bootcompiler.ast.MatchCommon;
import org.mozartoz.bootcompiler.ast.NoElseCommon;
import org.mozartoz.bootcompiler.ast.Node;
import org.mozartoz.bootcompiler.ast.OpenRecordPattern;
import org.mozartoz.bootcompiler.ast.ProcExpression;
import org.mozartoz.bootcompiler.ast.RaiseCommon;
import org.mozartoz.bootcompiler.ast.RawDeclarationOrVar;
import org.mozartoz.bootcompiler.ast.Record;
import org.mozartoz.bootcompiler.ast.RecordField;
import org.mozartoz.bootcompiler.ast.ShortCircuitBinaryOp;
import org.mozartoz.bootcompiler.ast.SkipStatement;
import org.mozartoz.bootcompiler.ast.StatAndExpression;
import org.mozartoz.bootcompiler.ast.StatOrExpr;
import org.mozartoz.bootcompiler.ast.Statement;
import org.mozartoz.bootcompiler.ast.TryCommon;
import org.mozartoz.bootcompiler.ast.UnboundExpression;
import org.mozartoz.bootcompiler.ast.Variable;
import org.mozartoz.bootcompiler.ast.VariableOrRaw;
import org.mozartoz.bootcompiler.oz.False;
import org.mozartoz.bootcompiler.oz.OzArity;
import org.mozartoz.bootcompiler.oz.OzAtom;
import org.mozartoz.bootcompiler.oz.OzBaseValue;
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
import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.ExecuteValuesNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.TopLevelHandlerNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.FailNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseNodeFactory;
import org.mozartoz.truffle.nodes.builtins.FloatBuiltinsFactory.FloatDivNodeFactory;
import org.mozartoz.truffle.nodes.builtins.IntBuiltinsFactory.DivNodeFactory;
import org.mozartoz.truffle.nodes.builtins.IntBuiltinsFactory.ModNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.HeadNodeGen;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.TailNodeGen;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.AddNodeFactory;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.MulNodeFactory;
import org.mozartoz.truffle.nodes.builtins.NumberBuiltinsFactory.SubNodeFactory;
import org.mozartoz.truffle.nodes.builtins.RecordBuiltinsFactory.LabelNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.DotNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.CatExchangeNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.GreaterThanNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.GreaterThanOrEqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.LesserThanNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.LesserThanOrEqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.NotEqualNodeFactory;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.call.SelfTailCallCatcherNode;
import org.mozartoz.truffle.nodes.call.SelfTailCallThrowerNode;
import org.mozartoz.truffle.nodes.call.TailCallThrowerNode;
import org.mozartoz.truffle.nodes.control.AndNode;
import org.mozartoz.truffle.nodes.control.AndThenNode;
import org.mozartoz.truffle.nodes.control.ForNode;
import org.mozartoz.truffle.nodes.control.IfNode;
import org.mozartoz.truffle.nodes.control.NoElseNode;
import org.mozartoz.truffle.nodes.control.OrElseNode;
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
import org.mozartoz.truffle.nodes.literal.ProcDeclarationAndExtractionNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.literal.UnboundLiteralNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeArgNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ResetSlotsNode;
import org.mozartoz.truffle.nodes.local.CopyVariableToFrameNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchDynamicArityNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchOpenRecordNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import scala.collection.JavaConversions;

public class Translator {

	static class Environment {
		private final Environment parent;
		private final FrameDescriptor capturedVariables = new FrameDescriptor();
		private final FrameDescriptor frameDescriptor;

		public Environment(Environment parent, FrameDescriptor frameDescriptor) {
			this.parent = parent;
			this.frameDescriptor = frameDescriptor;
		}

		public FrameSlot addLocalVariable(String identifier) {
			return frameDescriptor.addFrameSlot(identifier);
		}

		public FrameSlot findLocalVariable(String identifier) {
			return frameDescriptor.findFrameSlot(identifier);
		}

		public FrameSlot addCapturedVariable(String identifier) {
			return capturedVariables.addFrameSlot(identifier);
		}

		public FrameSlot findCapturedVariable(String identifier) {
			return capturedVariables.findFrameSlot(identifier);
		}

		public FrameSlotAndDepth findAndExtractVariable(String identifier) {
			// Look into locals
			FrameSlot slot = this.findLocalVariable(identifier);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, 0);
			}
			if (parent == null) { // If no parent, should have been in the locals
				throw new Error(identifier);
			}
			// Look for already captured
			slot = this.findCapturedVariable(identifier);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, 1);
			}
			// Perform lookup, update parent so it can extract it as well from its parents
			parent.findAndExtractVariable(identifier);
			FrameSlot stored = addCapturedVariable(identifier);
			return new FrameSlotAndDepth(stored, 1);
		}
	}

	private Environment environment = new Environment(null, new FrameDescriptor());
	private final Environment rootEnvironment = environment;
	private final OzLanguage language;
	private final DynamicObject base;

	private boolean isSelfTailRec = false;

	public Translator(OzLanguage language, DynamicObject base) {
		this.language = language;
		this.base = base;
	}

	public FrameSlot addRootSymbol(Symbol symbol) {
		return rootEnvironment.addLocalVariable(symbol2identifier(symbol));
	}

	private void pushEnvironment(FrameDescriptor frameDescriptor, String identifier) {
		environment = new Environment(environment, frameDescriptor);
	}

	private void popEnvironment() {
		environment = environment.parent;
	}

	public String symbol2identifier(Symbol symbol) {
		return symbol.fullName().intern();
	}

	public FrameSlotAndDepth findVariable(Symbol symbol) {
		return findVariable(symbol2identifier(symbol));
	}

	public FrameSlotAndDepth findVariable(String identifier) {
		int depth = 0;
		Environment environment = this.environment;

		while (environment != null) {
			FrameSlot slot = environment.findLocalVariable(identifier);
			if (slot != null) {
				return new FrameSlotAndDepth(slot, depth);
			} else {
				environment = environment.parent;
				depth++;
			}
		}
		throw new Error(identifier);
	}

	public FrameSlotAndDepth findAndExtractVariable(Symbol symbol) {
		return findAndExtractVariable(symbol2identifier(symbol));
	}

	public FrameSlotAndDepth findAndExtractVariable(String identifier) {
		if (!Options.FRAME_FILTERING) {
			return findVariable(identifier);
		}
		return this.environment.findAndExtractVariable(identifier);
	}

	public RootCallTarget translateAST(String description, Statement ast, Function<OzNode, OzNode> wrap) {
		OzNode translated = translate(ast);
		OzNode wrapped = wrap.apply(translated);
		OzNode handler = new TopLevelHandlerNode(wrapped);

		OzRootNode rootNode = new OzRootNode(language, Loader.MAIN_SOURCE_SECTION, description, environment.frameDescriptor, handler, 0, false);
		return rootNode.toCallTarget();
	}

	OzNode translate(StatOrExpr node) {
		if (node instanceof Expression) {
			// Literal expressions
			if (node instanceof Constant) {
				Constant constant = (Constant) node;
				return t(node, translateConstantNode(constant.value()));
			} else if (node instanceof Record) {
				Record record = (Record) node;
				List<RecordField> fields = new ArrayList<>(toJava(record.fields()));
				if (record.isCons()) {
					return t(node, buildCons(translate(fields.get(0).value()), translate(fields.get(1).value())));
				} else if (record.hasConstantArity()) {
					Arity arity = buildArity(record.getConstantArity());
					if (fields.isEmpty()) {
						return t(node, new LiteralNode(arity.getLabel()));
					}
					OzNode[] values = new OzNode[fields.size()];
					for (int i = 0; i < values.length; i++) {
						values[i] = translate(fields.get(i).value());
					}
					return t(node, new RecordLiteralNode(arity, values));
				} else if (record.fields().size() == 0) {
					return t(node, new EnsureOzLiteralNode(translate(record.label())));
				} else {
					OzNode label = translate(record.label());
					OzNode[] features = translate(record.features());
					OzNode[] values = translate(record.values());
					return t(node, new MakeDynamicRecordNode(label, features, values));
				}
			} else if (node instanceof ListExpression) {
				ListExpression list = (ListExpression) node;
				OzNode[] elements = translate(list.elements());
				return t(node, new ListLiteralNode(elements));
			} else if (node instanceof Variable) {
				Variable variable = (Variable) node;
				Symbol sym = variable.symbol();
				OzNode variableNode = t(node, findAndExtractVariable(sym).createReadNode());
				if (variable.clear()) {
					return t(node, new ResetSlotsNode(
							new FrameSlot[0], variableNode,
							new FrameSlot[] { environment.findLocalVariable(symbol2identifier(sym)) }));
				}
				return variableNode;
			} else if (node instanceof UnboundExpression) {
				return t(node, new UnboundLiteralNode());
			} else if (node instanceof BinaryOp) {
				BinaryOp binaryOp = (BinaryOp) node;
				return t(node, translateBinaryOp(binaryOp.operator(),
						translate(binaryOp.left()),
						translate(binaryOp.right())));
			} else if (node instanceof ShortCircuitBinaryOp) {
				ShortCircuitBinaryOp binaryOp = (ShortCircuitBinaryOp) node;
				return t(node, translateShortCircuitBinaryOp(binaryOp.operator(),
						translate(binaryOp.left()),
						translate(binaryOp.right())));
			} else if (node instanceof ProcExpression) { // proc/fun literal
				return translateProc((ProcExpression) node);
			}
		}

		// Structural nodes
		if (node instanceof CompoundStatement) {
			CompoundStatement compound = (CompoundStatement) node;
			return t(node, SequenceNode.sequence(translate(compound.statements())));
		} else if (node instanceof StatAndExpression) {
			StatAndExpression statAndExpr = (StatAndExpression) node;
			return t(node, SequenceNode.sequence(translate(statAndExpr.statement()), translate(statAndExpr.expression())));
		} else if (node instanceof LocalCommon) {
			LocalCommon local = (LocalCommon) node;
			List<OzNode> decls = new ArrayList<>(local.declarations().size());
			for (RawDeclarationOrVar variable : toJava(local.declarations())) {
				Variable var = ((Variable) variable);
				Symbol symbol = var.symbol();
				FrameSlot slot = environment.addLocalVariable(symbol2identifier(symbol));
				// No need to initialize captures, the slot will be set directly
				if (!(symbol.isCapture() || var.onStack())) {
					decls.add(t(variable, new InitializeVarNode(slot)));
				}
			}
			return t(node, SequenceNode.sequence(decls.toArray(new OzNode[decls.size()]), translate(local.body())));
		} else if (node instanceof CallCommon) {
			return translateCall((CallCommon) node);
		} else if (node instanceof SkipStatement) {
			return t(node, new SkipNode());
		} else if (node instanceof BindCommon) {
			BindCommon bind = (BindCommon) node;
			Expression left = bind.left();
			Expression right = bind.right();
			if (bind.onStack()) {
				FrameSlot slot = findAndExtractVariable(((Variable) left).symbol()).slot;
				return t(node, new InitializeTmpNode(slot, translate(right)));
			}
			return t(node, BindNodeGen.create(translate(left), translate(right)));
		} else if (node instanceof IfCommon) {
			IfCommon ifNode = (IfCommon) node;
			return t(node, new IfNode(translate(ifNode.condition()),
					translate(ifNode.truePart()),
					translate(ifNode.falsePart())));
		} else if (node instanceof MatchCommon) {
			return translateMatch((MatchCommon) node);
		} else if (node instanceof ClearVarsCommon) {
			ClearVarsCommon clearVars = (ClearVarsCommon) node;
			FrameSlot[] before = new FrameSlot[clearVars.before().length()];
			for (int i = 0; i < before.length; i++) {
				before[i] = environment.findLocalVariable(symbol2identifier(clearVars.before().apply(i)));
			}
			FrameSlot[] after = new FrameSlot[clearVars.after().length()];
			for (int i = 0; i < after.length; i++) {
				after[i] = environment.findLocalVariable(symbol2identifier(clearVars.after().apply(i)));
			}
			return t(node, new ResetSlotsNode(before, translate(clearVars.node()), after));
		} else if (node instanceof ForStatement) {
			ForStatement forNode = (ForStatement) node;
			return t(node, new ForNode(translate(forNode.from()), translate(forNode.to()),
					translate(forNode.proc())));
		} else if (node instanceof TryCommon) {
			TryCommon tryNode = (TryCommon) node;
			FrameSlotAndDepth exceptionVarSlot = findAndExtractVariable(((Variable) tryNode.exceptionVar()).symbol());
			return t(node, new TryNode(
					exceptionVarSlot.createWriteNode(),
					translate(tryNode.body()),
					translate(tryNode.catchBody())));
		} else if (node instanceof RaiseCommon) {
			RaiseCommon raiseNode = (RaiseCommon) node;
			return t(node, RaiseNodeFactory.create(translate(raiseNode.exception())));
		} else if (node instanceof FailStatement) {
			return t(node, FailNodeFactory.create());
		}

		throw unknown("expression or statement", node);
	}

	public OzNode translateProc(ProcExpression procExpression) {
		SourceSection sourceSection = t(procExpression);
		String identifier = "";
		if (procExpression.name().isDefined()) {
			identifier = procExpression.name().get().name();
		}
		if (Options.SHOW_PROC_AST != null && identifier.endsWith(Options.SHOW_PROC_AST)) {
			System.out.println(procExpression);
		}
		pushEnvironment(new FrameDescriptor(), identifier);
		boolean isSelfTailRecOld = isSelfTailRec;
		isSelfTailRec = false;
		try {
			int arity = procExpression.args().size();
			OzNode[] nodes = new OzNode[arity + 1];
			int i = 0;
			for (VariableOrRaw variable : toJava(procExpression.args())) {
				if (variable instanceof Variable) {
					FrameSlot argSlot = environment.addLocalVariable(symbol2identifier(((Variable) variable).symbol()));
					nodes[i] = new InitializeArgNode(argSlot, i);
					i++;
				} else {
					throw unknown("variable", variable);
				}
			}
			nodes[i] = translate(procExpression.body());

			OzNode procBody = SequenceNode.sequence(nodes);
			if (isSelfTailRec) { // Set when translating a self tail call
				procBody = SelfTailCallCatcherNode.create(procBody, environment.frameDescriptor);
			}
			boolean forceSplitting = Loader.getInstance().isLoadingBase();
			OzRootNode rootNode = new OzRootNode(language, sourceSection, identifier, environment.frameDescriptor, procBody, arity, forceSplitting);

			if (Options.FRAME_FILTERING) {
				CopyVariableToFrameNode[] captureNodes = new CopyVariableToFrameNode[environment.capturedVariables.getSize()];
				int j = 0;
				for (FrameSlot dst : environment.capturedVariables.getSlots()) {
					FrameSlotAndDepth src = environment.parent.findAndExtractVariable((String) dst.getIdentifier());
					captureNodes[j] = CopyVariableToFrameNode.create(src.createReadNode(), dst);
					j++;
				}
				return t(procExpression, new ProcDeclarationAndExtractionNode(rootNode.toCallTarget(), environment.capturedVariables, captureNodes));
			}
			return t(procExpression, new ProcDeclarationNode(rootNode.toCallTarget()));
		} finally {
			popEnvironment();
			isSelfTailRec = isSelfTailRecOld;
		}
	}

	private OzNode translateCall(CallCommon call) {
		OzNode receiver = translate(call.callable());
		OzNode[] argsNodes = translate(call.args());
		if (Options.SELF_TAIL_CALLS && call.isSelfTail()) {
			isSelfTailRec = true;
			return t(call, new SelfTailCallThrowerNode(argsNodes));
		} else if (Options.TAIL_CALLS && call.isTail()) {
			return t(call, new TailCallThrowerNode(receiver, new ExecuteValuesNode(argsNodes)));
		} else {
			return t(call, CallNode.create(receiver, new ExecuteValuesNode(argsNodes)));
		}
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
		Expression pattern = clause.pattern();
		translateMatcher(pattern, valueNode, checks, bindings);
		OzNode body = translate(clause.body());
		final IfNode matchNode;
		if (clause.hasGuard()) {
			OzNode guard = translate(clause.guard().get());
			// First the checks, then the bindings and then the guard (possibly using the bindings)
			bindings.add(guard);
			checks.add(SequenceNode.sequence(bindings.toArray(new OzNode[bindings.size()])));
			OzNode condition = t(pattern, new AndNode(checks.toArray(new OzNode[checks.size()])));
			matchNode = new IfNode(condition, body, elseNode);
		} else {
			matchNode = new IfNode(
					t(pattern, new AndNode(checks.toArray(new OzNode[checks.size()]))),
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
			FrameSlotAndDepth slot = findAndExtractVariable(((OzPatMatCapture) matcher).variable());
			// Set the slot directly since the variable is born here
			assert slot.getDepth() == 0;
			bindings.add(new InitializeTmpNode(slot.getSlot(), copy(valueNode)));
		} else if (matcher instanceof Variable) {
			Variable var = (Variable) matcher;
			OzNode left = findAndExtractVariable(var.symbol()).createReadNode();
			checks.add(EqualNodeFactory.create(deref(left), deref(copy(valueNode))));
		} else if (matcher instanceof OzPatMatConjunction) {
			for (OzValue part : toJava(((OzPatMatConjunction) matcher).parts())) {
				translateMatcher(part, valueNode, checks, bindings);
			}
		} else if (matcher instanceof OzFeature) {
			Object feature = translateFeature((OzFeature) matcher);
			checks.add(PatternMatchEqualNode.create(feature, copy(valueNode)));
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
		} else if (matcher instanceof OpenRecordPattern) {
			OpenRecordPattern record = (OpenRecordPattern) matcher;
			// First match the label
			translateMatcher(record.label(), LabelNodeFactory.create(copy(valueNode)), checks, bindings);
			// Then check if features match
			assert record.fields().isEmpty();
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
		} else if (value instanceof OzBaseValue) {
			String name = ((OzBaseValue) value).name();
			Object baseValue = base.get(name);
			assert baseValue != null;
			return baseValue;
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
		switch (operator) {
		case "==":
			return createBinaryOp(EqualNodeFactory.getInstance(), left, right);
		case "\\=":
			return createBinaryOp(NotEqualNodeFactory.getInstance(), left, right);
		case "+":
			return createBinaryOp(AddNodeFactory.getInstance(), left, right);
		case "-":
			return createBinaryOp(SubNodeFactory.getInstance(), left, right);
		case "*":
			return createBinaryOp(MulNodeFactory.getInstance(), left, right);
		case "/":
			return createBinaryOp(FloatDivNodeFactory.getInstance(), left, right);
		case "div":
			return createBinaryOp(DivNodeFactory.getInstance(), left, right);
		case "mod":
			return createBinaryOp(ModNodeFactory.getInstance(), left, right);
		case "<":
			return createBinaryOp(LesserThanNodeFactory.getInstance(), left, right);
		case "=<":
			return createBinaryOp(LesserThanOrEqualNodeFactory.getInstance(), left, right);
		case ">":
			return createBinaryOp(GreaterThanNodeFactory.getInstance(), left, right);
		case ">=":
			return createBinaryOp(GreaterThanOrEqualNodeFactory.getInstance(), left, right);
		case ".":
			return createBinaryOp(DotNodeFactory.getInstance(), left, right);
		case ":=":
			return createBinaryOp(CatExchangeNodeFactory.getInstance(), left, right);
		default:
			throw unknown("operator", operator);
		}
	}

	private static OzNode createBinaryOp(NodeFactory<? extends OzNode> factory, OzNode left, OzNode right) {
		return BuiltinsManager.createNodeFromFactory(factory, new OzNode[] { left, right });
	}

	private OzNode translateShortCircuitBinaryOp(String operator, OzNode left, OzNode right) {
		switch (operator) {
		case "andthen":
			return new AndThenNode(left, right);
		case "orelse":
			return new OrElseNode(left, right);
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

	private <E extends StatOrExpr> OzNode[] translate(scala.collection.Iterable<E> scalaIterable) {
		Collection<E> collection = toJava(scalaIterable);
		OzNode[] result = new OzNode[collection.size()];
		int i = 0;
		for (E element : collection) {
			result[i++] = translate(element);
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

	private static Error unknown(String type, Object description) {
		return new AssertionError("Unknown " + type + " " + description.getClass() + ": " + description);
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
		assert node.section() != null;
		return node.section();
	}

}
