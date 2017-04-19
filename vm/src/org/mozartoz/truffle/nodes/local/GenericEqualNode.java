package org.mozartoz.truffle.nodes.local;

import java.util.HashSet;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.CycleDetectingEqualNodeGen;
import org.mozartoz.truffle.nodes.local.GenericEqualNodeGen.DummyEqualNodeGen;
import org.mozartoz.truffle.runtime.DeoptimizingException;
import org.mozartoz.truffle.runtime.IdentityPair;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.coro.CoroutineLocal;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class GenericEqualNode extends OzNode {

	public static GenericEqualNode create() {
		return GenericEqualNodeGen.create(null, null);
	}

	public abstract boolean executeEqual(Object a, Object b);

	@Specialization(rewriteOn = DeoptimizingException.class)
	protected boolean dummyEqual(Object a, Object b,
			@Cached("create()") DummyEqualNode equalNode) {
		return equalNode.resetAndEqual(a, b);
	}

	@Specialization(contains = "dummyEqual")
	protected boolean cycleDetectingEqual(Object a, Object b,
			@Cached("create()") CycleDetectingEqualNode equalNode) {
		return equalNode.resetAndEqual(a, b);
	}

	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class DummyEqualNode extends DFSEqualNode {
		CoroutineLocal<MutableInt> count = new CoroutineLocal<GenericEqualNode.MutableInt>() {
			protected MutableInt initialValue() {
				return new MutableInt();
			}
		};
		
		public static DummyEqualNode create() {
			return DummyEqualNodeGen.create(null, null);
		}

		public boolean resetAndEqual(Object a, Object b) {
			MutableInt counter = count.get();
			counter.reset();
			return this.executeEqual(a, b);
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b) {
			MutableInt counter = count.get();
			if (counter.inc() > Options.CYCLE_THRESHOLD) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeEqual(deref(a), deref(b));
		}
	}

	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class CycleDetectingEqualNode extends DFSEqualNode {
		CoroutineLocal<HashSet<IdentityPair>> encountered = new CoroutineLocal<>();

		public static CycleDetectingEqualNode create() {
			return CycleDetectingEqualNodeGen.create(null, null);
		}

		public boolean resetAndEqual(Object a, Object b) {
			encountered.set(new HashSet<>());
			boolean result = this.executeEqual(a, b);
			encountered.set(null);
			return result;
		}

		@Override
		@TruffleBoundary
		protected boolean equalRec(Object a, Object b) {
			if (a instanceof Variable || b instanceof Variable) {
				IdentityPair pair = new IdentityPair(a, b);
				if (!encountered.get().add(pair)) {
					return true;
				}
			}
			return executeEqual(deref(a), deref(b));
		}
	}

	public static final class MutableInt {
		public int value;

		public int inc() {
			return ++value;
		}

		public void reset() {
			value = 0;
		}
	}

}
