package org.mozartoz.truffle.nodes.local;

import java.util.HashSet;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.CycleDetectingUnifyNodeGen;
import org.mozartoz.truffle.nodes.local.GenericUnifyNodeGen.DummyUnifyNodeGen;
import org.mozartoz.truffle.runtime.DeoptimizingException;
import org.mozartoz.truffle.runtime.IdentityPair;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
public abstract class GenericUnifyNode extends OzNode {
	public abstract Object executeUnify(Object a, Object b);

	public static GenericUnifyNode create() {
		return GenericUnifyNodeGen.create(null, null);
	}

	@Specialization(rewriteOn = DeoptimizingException.class)
	protected Object dummyUnify(Object a, Object b,
			@Cached("create()") DummyUnifyNode unifyNode) {
		return unifyNode.resetAndUnify(a, b);
	}

	@Specialization(contains = "dummyUnify")
	protected Object cycleDetectingUnify(Object a, Object b,
			@Cached("create()") CycleDetectingUnifyNode unifyNode) {
		return unifyNode.resetAndUnify(a, b);
	}

	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class DummyUnifyNode extends DFSUnifyNode {
		// No need to put it as CoroutineLocal, as unification cannot be interrupted.
		private int count = 0;

		public static DummyUnifyNode create() {
			return DummyUnifyNodeGen.create(null, null);
		}

		public Object resetAndUnify(Object a, Object b) {
			count = 0;
			return this.executeUnify(a, b);
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b) {
			if (++count > Options.CYCLE_THRESHOLD) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				throw DeoptimizingException.INSTANCE;
			}
			return executeUnify(deref(a), deref(b));
		}
	}

	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class CycleDetectingUnifyNode extends DFSUnifyNode {
		// No need to put it as CoroutineLocal, as unification cannot be interrupted.
		private HashSet<IdentityPair> encountered = new HashSet<>();

		public static CycleDetectingUnifyNode create() {
			return CycleDetectingUnifyNodeGen.create(null, null);
		}

		public Object resetAndUnify(Object a, Object b) {
			encountered = new HashSet<>();
			Object result = this.executeUnify(a, b);
			encountered = null;
			return result;
		}

		@Override
		@TruffleBoundary
		protected Object unify(Object a, Object b) {
			if (a instanceof Variable || b instanceof Variable) {
				IdentityPair pair = new IdentityPair(a, b);
				if (!encountered.add(pair)) {
					return a;
				}
			}
			return executeUnify(deref(a), deref(b));
		}
	}

}
