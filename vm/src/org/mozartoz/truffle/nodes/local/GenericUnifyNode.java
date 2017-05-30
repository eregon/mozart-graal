package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.CycleDetectingUnifyNodeGen;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.DepthLimitedUnifyNodeGen;
import org.mozartoz.truffle.runtime.DeoptimizingException;
import org.mozartoz.truffle.runtime.EncounteredPairSet;
import org.mozartoz.truffle.runtime.IdentityPair;
import org.mozartoz.truffle.runtime.MutableInt;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class GenericUnifyNode extends OzNode {

	protected static boolean CYCLE_DETECTION = Options.CYCLE_DETECTION;

	public abstract Object executeUnify(Object a, Object b);

	public static GenericUnifyNode create() {
		return GenericUnifyNodeGen.create(null, null);
	}

	@Specialization(guards = "!CYCLE_DETECTION")
	protected Object dfsUnify(Object a, Object b,
			@Cached("create()") DFSUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	@Specialization(guards = "CYCLE_DETECTION", rewriteOn = DeoptimizingException.class)
	protected Object depthLimitedUnify(Object a, Object b,
			@Cached("create()") DepthLimitedUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	@Specialization(guards = "CYCLE_DETECTION", replaces = "depthLimitedUnify")
	protected Object cycleDetectingUnify(Object a, Object b,
			@Cached("create()") CycleDetectingUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	public static abstract class DepthLimitedUnifyNode extends DFSUnifyNode {

		public static DepthLimitedUnifyNode create() {
			return DepthLimitedUnifyNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new MutableInt(0);
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b, Object state) {
			MutableInt n = (MutableInt) state;
			if (++n.value > Options.CYCLE_THRESHOLD) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeUnify(deref(a), deref(b), n);
		}
	}

	public static abstract class CycleDetectingUnifyNode extends DFSUnifyNode {
		// No need to put it as CoroutineLocal, as unification cannot be interrupted.

		public static CycleDetectingUnifyNode create() {
			return CycleDetectingUnifyNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new EncounteredPairSet();
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b, Object state) {
			EncounteredPairSet encountered = (EncounteredPairSet) state;
			if (a instanceof Variable || b instanceof Variable) {
				IdentityPair pair = new IdentityPair(a, b);
				if (!encountered.add(pair)) {
					return a;
				}
			}
			return executeUnify(deref(a), deref(b), encountered);
		}
	}

}
