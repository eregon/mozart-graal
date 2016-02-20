package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ArrayBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("low"), @NodeChild("high"), @NodeChild("initValue") })
	public static abstract class NewArrayNode extends OzNode {

		@Specialization
		Object newArray(Object low, Object high, Object initValue) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsArrayNode extends OzNode {

		@Specialization
		Object isArray(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("array")
	public static abstract class LowNode extends OzNode {

		@Specialization
		Object low(Object array) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("array")
	public static abstract class HighNode extends OzNode {

		@Specialization
		Object high(Object array) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("array"), @NodeChild("index") })
	public static abstract class GetNode extends OzNode {

		@Specialization
		Object get(Object array, Object index) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("array"), @NodeChild("index"), @NodeChild("newValue") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		Object put(Object array, Object index, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("array"), @NodeChild("index"), @NodeChild("newValue") })
	public static abstract class ExchangeFunNode extends OzNode {

		@Specialization
		Object exchangeFun(Object array, Object index, Object newValue) {
			return unimplemented();
		}

	}

}
