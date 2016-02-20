package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ThreadBuiltins {

	@Builtin(name = "create", proc = true)
	@GenerateNodeFactory
	@NodeChild("target")
	public static abstract class CreateThreadNode extends OzNode {

		@Specialization
		Object createThread(Object target) {
			return unimplemented();
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
		Object thisThread() {
			return unimplemented();
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
