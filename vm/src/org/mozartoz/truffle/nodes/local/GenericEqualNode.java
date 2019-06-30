package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.DepthLimitedEqualNodeGen;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.OnHeapEqualNodeGen;
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
public abstract class GenericEqualNode extends OzNode {

	protected static boolean useCycleDetection() {
		return OzLanguage.getOptions().get(Options.CYCLE_DETECTION);
	}

	public static GenericEqualNode create() {
		return GenericEqualNodeGen.create(null, null);
	}

	public abstract boolean executeEqual(Object a, Object b);

	@Specialization(guards = "!useCycleDetection()")
	protected boolean dfsEqual(Object a, Object b,
			@Cached DFSEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	@Specialization(guards = "useCycleDetection()", rewriteOn = DeoptimizingException.class)
	protected boolean depthLimitedEqual(Object a, Object b,
			@Cached DepthLimitedEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	@Specialization(guards = "useCycleDetection()", replaces = "depthLimitedEqual")
	protected boolean cycleDetectingEqual(Object a, Object b,
			@Cached OnHeapEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	public static abstract class DepthLimitedEqualNode extends DFSEqualNode {

		public static DepthLimitedEqualNode create() {
			return DepthLimitedEqualNodeGen.create(null, null, null);
		}

		private final int cycleThreshold;

		DepthLimitedEqualNode() {
			this.cycleThreshold = OzLanguage.getOptions().get(Options.CYCLE_THRESHOLD);
		}

		@Override
		protected Object initState() {
			return new MutableInt(0);
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b, Object state) {
			MutableInt n = (MutableInt) state;
			if (++n.value > cycleThreshold) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeEqual(deref(a), deref(b), n);
		}
	}

	public static abstract class OnHeapEqualNode extends DFSEqualNode {

		public static OnHeapEqualNode create() {
			return OnHeapEqualNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new OnHeapSearchState();
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b, Object state) {
			OnHeapSearchState searchState = (OnHeapSearchState) state;
			IdentityPair pair = new IdentityPair(a, b);
			if (a instanceof Variable || b instanceof Variable) {
				if (!searchState.encountered.add(pair)) {
					return true;
				}
			}
			searchState.toExplore.add(pair);

			if (searchState.root) {
				searchState.root = false;
				while (!searchState.toExplore.isEmpty()) {
					IdentityPair current = searchState.toExplore.removeLast();
					if (!executeEqual(deref(current.a), deref(current.b), searchState)) {
						return false;
					}
				}
				searchState.root = true;
			}
			return true;
		}
	}

}
