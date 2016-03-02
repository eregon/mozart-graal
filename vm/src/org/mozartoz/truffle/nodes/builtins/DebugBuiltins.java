package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class DebugBuiltins {

	@GenerateNodeFactory
	@NodeChild("thread")
	public static abstract class GetRaiseOnBlockNode extends OzNode {

		@Specialization
		Object getRaiseOnBlock(Object thread) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("value") })
	public static abstract class SetRaiseOnBlockNode extends OzNode {

		@Specialization
		Object setRaiseOnBlock(Object thread, Object value) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("id") })
	public static abstract class SetIdNode extends OzNode {

		@Specialization
		Object setId(Object thread, Object id) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	public static abstract class BreakpointNode extends OzNode {

		@Specialization
		Object breakpoint() {
			return unimplemented();
		}

	}

}
