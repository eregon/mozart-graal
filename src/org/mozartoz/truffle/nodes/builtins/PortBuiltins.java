package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzPort;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PortBuiltins {

	@Builtin(name = "new", tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("stream")
	public static abstract class NewPortNode extends OzNode {

		@Specialization
		OzPort newPort(OzVar stream) {
			return new OzPort(stream);
		}

	}

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsPortNode extends OzNode {

		@Specialization
		boolean isPort(OzPort port) {
			return true;
		}

		@Specialization(guards = "!isPort(value)")
		boolean isPort(Object value) {
			return false;
		}

	}

	@Builtin(proc = true, deref = 1, tryDeref = 2)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("port"), @NodeChild("value") })
	public static abstract class SendNode extends OzNode {

		@Specialization
		Object send(OzPort port, Object value) {
			port.send(value);
			return unit;
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
