package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.GetThreadProcNode;
import org.mozartoz.truffle.runtime.ArrayUtils;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzThread;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ThreadBuiltins {

	@Builtin(name = "create", proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("target")
	public static abstract class CreateThreadNode extends OzNode {

		final CallTarget startThread = OzProc.wrap("Thread.create", new GetThreadProcNode(), ArrayUtils.EMPTY);

		@TruffleBoundary
		@Specialization
		OzThread createThread(OzProc target) {
			return new OzThread(target, startThread);
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsThreadNode extends OzNode {

		@Specialization
		Object isThread(Object value) {
			return unimplemented();
		}

	}

	@Builtin(name = "this")
	@GenerateNodeFactory
	public static abstract class ThisThreadNode extends OzNode {

		@Specialization
		OzThread thisThread() {
			return OzThread.getCurrent();
		}

	}

	@GenerateNodeFactory
	@NodeChild("thread")
	public static abstract class GetPriorityNode extends OzNode {

		@Specialization
		Object getPriority(Object thread) {
			return unimplemented();
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("priority") })
	public static abstract class SetPriorityNode extends OzNode {

		@Specialization
		Object setPriority(OzThread thread, String priority) {
			// TODO: ignored for now
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("exception") })
	public static abstract class InjectExceptionNode extends OzNode {

		@Specialization
		Object injectException(Object thread, Object exception) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("thread")
	public static abstract class StateNode extends OzNode {

		@Specialization
		String state(OzThread thread) {
			return thread.getStatus();
		}

	}

}
