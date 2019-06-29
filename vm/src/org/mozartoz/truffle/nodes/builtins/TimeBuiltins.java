package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzThread;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class TimeBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("delay")
	public static abstract class AlarmNode extends OzNode {

		@Specialization(guards = "delay == 0")
		Object yield(long delay) {
			OzThread.getCurrent().yield(this);
			return unit;
		}

		@Specialization(guards = "delay != 0")
		Object alarm(long delay) {
			long end = now() + delay;
			while (now() < end) {
				sleep();
				OzThread.getCurrent().yield(this);
			}
			return unit;
		}

		private long now() {
			return System.currentTimeMillis();
		}

		@TruffleBoundary
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
		long getMonotonicTime() {
			return System.nanoTime();
		}

	}

}
