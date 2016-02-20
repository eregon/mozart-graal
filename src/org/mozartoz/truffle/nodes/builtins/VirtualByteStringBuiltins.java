package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class VirtualByteStringBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsVirtualByteStringNode extends OzNode {

		@Specialization
		Object isVirtualByteString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToCompactByteStringNode extends OzNode {

		@Specialization
		Object toCompactByteString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("tail") })
	public static abstract class ToByteListNode extends OzNode {

		@Specialization
		Object toByteList(Object value, Object tail) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class LengthNode extends OzNode {

		@Specialization
		Object length(Object value) {
			return unimplemented();
		}

	}

}
