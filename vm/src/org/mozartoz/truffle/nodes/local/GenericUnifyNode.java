package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.DepthLimitedUnifyNodeGen;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.OnHeapUnifyNodeGen;
import org.mozartoz.truffle.runtime.DeoptimizingException;
import org.mozartoz.truffle.runtime.IdentityPair;
import org.mozartoz.truffle.runtime.MutableInt;
import org.mozartoz.truffle.runtime.OnHeapSearchState;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class GenericUnifyNode extends OzNode {

	protected static boolean useCycleDetection() {
		return OzLanguage.getOptions().get(Options.CYCLE_DETECTION);
	}

	public abstract Object executeUnify(Object a, Object b);

	public static GenericUnifyNode create() {
		return GenericUnifyNodeGen.create(null, null);
	}

	@Specialization(guards = "!useCycleDetection()")
	protected Object dfsUnify(Object a, Object b,
			@Cached("create()") DFSUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	@Specialization(guards = "useCycleDetection()", rewriteOn = DeoptimizingException.class)
	protected Object depthLimitedUnify(Object a, Object b,
			@Cached("create()") DepthLimitedUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	@Specialization(guards = "useCycleDetection()", replaces = "depthLimitedUnify")
	protected Object cycleDetectingUnify(Object a, Object b,
			@Cached("create()") OnHeapUnifyNode unifyNode) {
		return unifyNode.executeUnify(a, b, null);
	}

	public static abstract class DepthLimitedUnifyNode extends DFSUnifyNode {

		public static DepthLimitedUnifyNode create() {
			return DepthLimitedUnifyNodeGen.create(null, null, null);
		}

		private final int cycleThreshold;

		DepthLimitedUnifyNode() {
			this.cycleThreshold = OzLanguage.getOptions().get(Options.CYCLE_THRESHOLD);
		}

		@Override
		protected Object initState() {
			return new MutableInt(0);
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b, Object state) {
			MutableInt n = (MutableInt) state;
			if (++n.value > cycleThreshold) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeUnify(deref(a), deref(b), n);
		}
	}

	public static abstract class OnHeapUnifyNode extends DFSUnifyNode {
		// No need to put it as CoroutineLocal, as unification cannot be interrupted.

		public static OnHeapUnifyNode create() {
			return OnHeapUnifyNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new OnHeapSearchState();
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b, Object state) {
			OnHeapSearchState searchState = (OnHeapSearchState) state;
			IdentityPair pair = new IdentityPair(a, b);
			if (a instanceof Variable || b instanceof Variable) {
				if (!searchState.encountered.add(pair)) {
					return b;
				}
			}
			searchState.toExplore.add(pair);

			if (searchState.root) {
				searchState.root = false;
				while (!searchState.toExplore.isEmpty()) {
					IdentityPair current = searchState.toExplore.removeLast();
					executeUnify(deref(current.a), deref(current.b), searchState);
				}
				searchState.root = true;
			}
			return b;
		}
	}

}
