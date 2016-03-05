package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ArrayBuiltinsFactory.ArrayGetNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ArrayBuiltinsFactory.ArrayPutNodeFactory;
import org.mozartoz.truffle.runtime.OzArray;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ArrayBuiltins {

	public static int long2int(long value) {
		if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
			return (int) value;
		} else {
			throw new Error();
		}
	}

	@Builtin(name = "new", deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("low"), @NodeChild("high"), @NodeChild("initValue") })
	public static abstract class NewArrayNode extends OzNode {

		@Specialization
		OzArray newArray(long low, long high, Object initValue) {
			assert high >= low;
			int width = long2int(high - low + 1);
			return new OzArray(long2int(low), width, initValue);
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

	@Builtin(name = "get", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("array"), @NodeChild("index") })
	public static abstract class ArrayGetNode extends OzNode {

		public static ArrayGetNode create() {
			return ArrayGetNodeFactory.create(null, null);
		}

		public abstract Object executeGet(OzArray array, long index);

		@Specialization
		Object get(OzArray array, long index) {
			return array.get(long2int(index));
		}

	}

	@Builtin(name = "put", proc = true, deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("array"), @NodeChild("index"), @NodeChild("newValue") })
	public static abstract class ArrayPutNode extends OzNode {

		public static ArrayPutNode create() {
			return ArrayPutNodeFactory.create(null, null, null);
		}

		public abstract Object executePut(OzArray array, long index, Object newValue);

		@Specialization
		Object put(OzArray array, long index, Object newValue) {
			array.set(long2int(index), newValue);
			return unit;
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
