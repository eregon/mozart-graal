package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.coro.Coroutine;

public abstract class TimeBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("delay")
	public static abstract class AlarmNode extends OzNode {

		@Specialization
		Object alarm(long delay) {
			long end = now() + delay;
			while (now() < end) {
				sleep();
				Coroutine.yield();
			}
			return unit;
		}

		private long now() {
			return System.currentTimeMillis();
		}

		private void sleep() {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@GenerateNodeFactory
	public static abstract class GetReferenceTimeNode extends OzNode {

		@Specialization
		Object getReferenceTime() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class GetMonotonicTimeNode extends OzNode {

		@Specialization
		Object getMonotonicTime() {
			return unimplemented();
		}

	}

}
