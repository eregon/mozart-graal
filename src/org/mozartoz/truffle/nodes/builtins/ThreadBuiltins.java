package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.CallProcNode;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzThread;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ThreadBuiltins {

	@Builtin(name = "create", proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("target")
	public static abstract class CreateThreadNode extends OzNode {

		@Child CallProcNode callProcNode = CallProcNodeGen.create(new OzNode[0], null);

		@Specialization
		OzThread createThread(VirtualFrame frame, OzProc target) {
			return new OzThread(target);
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

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("priority") })
	public static abstract class SetPriorityNode extends OzNode {

		@Specialization
		Object setPriority(Object thread, Object priority) {
			return unimplemented();
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

	@GenerateNodeFactory
	@NodeChild("thread")
	public static abstract class StateNode extends OzNode {

		@Specialization
		Object state(Object thread) {
			return unimplemented();
		}

	}

}
