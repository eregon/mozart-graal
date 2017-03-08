package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.RecordBuiltins.LabelNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.RecordFactory;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChildren({ @NodeChild("object"), @NodeChild("arguments") })
public abstract class CallMethodNode extends OzNode {

	@Child DerefNode derefNode = DerefNode.create();
	@Child LabelNode labelNode = LabelNode.create();

	protected static int optional(int l) {
		return Options.OPTIMIZE_METHODS ? l : 0;
	}

	/** Must only be used by CallNode */
	public static CallMethodNode create() {
		return CallMethodNodeGen.create(null, null);
	}

	abstract Object executeCall(VirtualFrame frame, OzObject self, Object[] args);

	static final RecordFactory OTHERWISE_MESSAGE_FACTORY = Arity.build("otherwise", 1L).createFactory();

	@Specialization(guards = {
			"cachedProc != null",
			"self.getClazz() == cachedClazz",
			"cachedLabel.equals(labelFromArgs(args))",
	}, limit = "optional(3)")
	protected Object callClassLabelIdentity(VirtualFrame frame, OzObject self, Object[] args,
			@Cached("self.getClazz()") DynamicObject cachedClazz,
			@Cached("labelFromArgs(args)") Object cachedLabel,
			@Cached("methodFromArgs(self, args)") OzProc cachedProc,
			@Cached("createDirectCallNode(cachedProc.callTarget)") DirectCallNode callNode) {
		assert args.length == 1;
		Object[] arguments = new Object[] { self, args[0] };
		return callNode.call(frame, OzArguments.pack(cachedProc.declarationFrame, arguments));
	}

	@Specialization(guards = {
			"cachedProc != null",
			"cachedProc == methodFromArgs(self, args)",
	}, contains = "callClassLabelIdentity", limit = "optional(3)")
	protected Object callProcIdentity(VirtualFrame frame, OzObject self, Object[] args,
			@Cached("methodFromArgs(self, args)") OzProc cachedProc,
			@Cached("createDirectCallNode(cachedProc.callTarget)") DirectCallNode callNode) {
		assert args.length == 1;
		Object[] arguments = new Object[] { self, args[0] };
		return callNode.call(frame, OzArguments.pack(cachedProc.declarationFrame, arguments));
	}

	@Specialization(contains = { "callClassLabelIdentity", "callProcIdentity" })
	protected Object callObject(VirtualFrame frame, OzObject self, Object[] args,
			@Cached("create()") IndirectCallNode callNode,
			@Cached("createBinaryProfile()") ConditionProfile otherwise) {
		assert args.length == 1;
		Object message = derefNode.executeDeref(args[0]);

		final Object name = labelNode.executeLabel(message);
		assert OzGuards.isLiteral(name);

		OzProc method = getMethod(self, name);
		if (otherwise.profile(method == null)) { // redirect to otherwise
			method = getMethod(self, "otherwise");
			message = OTHERWISE_MESSAGE_FACTORY.newRecord(message);
		}

		Object[] arguments = new Object[] { self, message };
		return callNode.call(frame, method.callTarget, OzArguments.pack(method.declarationFrame, arguments));
	}

	static final OzUniqueName ooMeth = OzUniqueName.get("ooMeth");

	@TruffleBoundary
	private OzProc getMethod(OzObject self, Object name) {
		DynamicObject methods = (DynamicObject) self.getClazz().get(ooMeth);
		return (OzProc) methods.get(name);
	}

	protected Object labelFromArgs(Object[] args) {
		return labelNode.executeLabel(derefNode.executeDeref(args[0]));
	}

	protected OzProc methodFromArgs(OzObject self, Object[] args) {
		return getMethod(self, labelFromArgs(args));
	}

	protected static DirectCallNode createDirectCallNode(RootCallTarget callTarget) {
		return CallProcNode.createDirectCallNode(callTarget);
	}

}
