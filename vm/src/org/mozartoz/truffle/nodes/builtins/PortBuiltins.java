package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PortBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("stream")
	public static abstract class NewPortNode extends OzNode {

		@Specialization
		Object newPort(OzVar stream) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsPortNode extends OzNode {

		@Specialization
		Object isPort(Object value) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("port"), @NodeChild("value") })
	public static abstract class SendNode extends OzNode {

		@Specialization
		Object send(Object port, Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("port"), @NodeChild("value") })
	public static abstract class SendRecvNode extends OzNode {

		@Specialization
		Object sendRecv(Object port, Object value) {
			return unimplemented();
		}

	}

}
