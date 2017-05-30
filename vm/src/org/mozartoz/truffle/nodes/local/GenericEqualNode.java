package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.CycleDetectingEqualNodeGen;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.DepthLimitedEqualNodeGen;
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
public abstract class GenericEqualNode extends OzNode {

	protected static boolean CYCLE_DETECTION = Options.CYCLE_DETECTION;

	public static GenericEqualNode create() {
		return GenericEqualNodeGen.create(null, null);
	}

	public abstract boolean executeEqual(Object a, Object b);

	@Specialization(guards = "!CYCLE_DETECTION")
	protected boolean dfsEqual(Object a, Object b,
			@Cached("create()") DFSEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	@Specialization(guards = "CYCLE_DETECTION", rewriteOn = DeoptimizingException.class)
	protected boolean depthLimitedEqual(Object a, Object b,
			@Cached("create()") DepthLimitedEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	@Specialization(guards = "CYCLE_DETECTION", replaces = "depthLimitedEqual")
	protected boolean cycleDetectingEqual(Object a, Object b,
			@Cached("create()") CycleDetectingEqualNode equalNode) {
		return equalNode.executeEqual(a, b, null);
	}

	public static abstract class DepthLimitedEqualNode extends DFSEqualNode {
		
		public static DepthLimitedEqualNode create() {
			return DepthLimitedEqualNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new MutableInt(0);
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b, Object state) {
			MutableInt n = (MutableInt) state;
			if (++n.value > Options.CYCLE_THRESHOLD) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeEqual(deref(a), deref(b), n);
		}
	}

	public static abstract class CycleDetectingEqualNode extends DFSEqualNode {

		public static CycleDetectingEqualNode create() {
			return CycleDetectingEqualNodeGen.create(null, null, null);
		}

		@Override
		protected Object initState() {
			return new EncounteredPairSet();
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b, Object state) {
			EncounteredPairSet encountered = (EncounteredPairSet) state;
			if (a instanceof Variable || b instanceof Variable) {
				IdentityPair pair = new IdentityPair(a, b);
				if (!encountered.add(pair)) {
					return true;
				}
			}
			return executeEqual(deref(a), deref(b), encountered);
		}
	}

}
