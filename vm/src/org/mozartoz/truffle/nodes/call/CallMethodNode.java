package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.RecordBuiltins.LabelNode;
import org.mozartoz.truffle.nodes.call.CallMethodNodeGen.MethodDispatchNodeGen;
import org.mozartoz.truffle.nodes.call.CallMethodNodeGen.MethodLookupNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.RecordFactory;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChildren({ @NodeChild("object"), @NodeChild("arguments") })
public abstract class CallMethodNode extends OzNode {

	public static int methodCache() {
		return OzLanguage.getOptions().get(Options.INLINE_CACHE_METHOD);
	}

	/** Must only be used by CallNode */
	public static CallMethodNode create() {
		return CallMethodNodeGen.create(null, null);
	}

	abstract Object executeCall(OzObject self, Object[] args);

	@Specialization
	public Object callMethod(OzObject self, Object[] args,
			@Cached DerefNode derefNode,
			@Cached LabelNode labelNode,
			@Cached MethodLookupNode lookupNode,
			@Cached MethodDispatchNode dispatchNode) {
		assert args.length == 1;
		Object message = derefNode.executeDeref(args[0]);
		Object name = labelNode.executeLabel(message);
		assert OzGuards.isLiteral(name);

		OzProc method = lookupNode.executeLookup(self, name);
		return dispatchNode.executeDispatch(self, method, message);
	}

	@ImportStatic(CallMethodNode.class)
	@NodeChildren({ @NodeChild("self"), @NodeChild("name") })
	public abstract static class MethodLookupNode extends OzNode {

		static final OzUniqueName ooMeth = OzUniqueName.get("ooMeth");

		public static MethodLookupNode create() {
			return MethodLookupNodeGen.create(null, null);
		}

		public abstract OzProc executeLookup(OzObject self, Object name);

		@Specialization(guards = {
				"self.getClazz() == cachedClazz",
				"name == cachedName",
		}, limit = "methodCache()")
		protected OzProc cachedLookup(OzObject self, Object name,
				@Cached("self.getClazz()") DynamicObject cachedClazz,
				@Cached("name") Object cachedName,
				@Cached("getMethod(self, name)") OzProc cachedMethod) {
			return cachedMethod;
		}

		@Specialization(replaces = "cachedLookup")
		protected OzProc uncachedLookup(OzObject self, Object name) {
			return getMethod(self, name);
		}

		@TruffleBoundary
		protected static OzProc getMethod(OzObject self, Object name) {
			DynamicObject methods = (DynamicObject) self.getClazz().get(ooMeth);
			return (OzProc) methods.get(name);
		}

	}

	@ImportStatic(CallMethodNode.class)
	@NodeChildren({ @NodeChild("self"), @NodeChild("method"), @NodeChild("args") })
	public abstract static class MethodDispatchNode extends OzNode {

		static final String OTHERWISE = "otherwise";
		static final RecordFactory OTHERWISE_MESSAGE_FACTORY = Arity.build("otherwise", 1L).createFactory();

		public static MethodDispatchNode create() {
			return MethodDispatchNodeGen.create(null, null, null);
		}

		public abstract Object executeDispatch(OzObject self, OzProc method, Object message);

		@Specialization(guards = {
				"method == cachedMethod",
				"cachedMethod != null",
		}, limit = "methodCache()")
		protected Object dispatchCached(OzObject self, OzProc method, Object message,
				@Cached("method") OzProc cachedMethod,
				@Cached("createDirectCallNode(cachedMethod.callTarget)") DirectCallNode callNode) {
			Object[] arguments = new Object[] { self, message };
			return callNode.call(OzArguments.pack(cachedMethod.declarationFrame, arguments));
		}

		@Specialization(guards = "method != null", replaces = "dispatchCached")
		protected Object dispatchUncached(OzObject self, OzProc method, Object message,
				@Cached IndirectCallNode callNode) {
			Object[] arguments = new Object[] { self, message };
			return callNode.call(method.callTarget,
					OzArguments.pack(method.declarationFrame, arguments));
		}

		@Specialization(guards = "method == null")
		protected Object dispatchOtherwise(OzObject self, Object method, Object message,
				@Cached MethodLookupNode otherwiseLookupNode,
				@Cached MethodDispatchNode otherwiseDispatchNode) {
			Object otherwiseMessage = OTHERWISE_MESSAGE_FACTORY.newRecord(message);
			OzProc otherwiseMethod = otherwiseLookupNode.executeLookup(self, OTHERWISE);
			return otherwiseDispatchNode.executeDispatch(self, otherwiseMethod, otherwiseMessage);
		}

		protected static DirectCallNode createDirectCallNode(RootCallTarget callTarget) {
			return CallProcNode.createDirectCallNode(callTarget);
		}

	}

}
