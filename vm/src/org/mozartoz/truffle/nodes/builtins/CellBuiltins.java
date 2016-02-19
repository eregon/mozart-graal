package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class CellBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("initial")
	public static abstract class NewCellNode extends OzNode {

		@Specialization
		Object newCell(Object initial) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsCellNode extends OzNode {

		@Specialization
		Object isCell(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("cell"), @NodeChild("newValue") })
	public static abstract class ExchangeFunNode extends OzNode {

		@Specialization
		Object exchangeFun(Object cell, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("cell")
	public static abstract class AccessNode extends OzNode {

		@Specialization
		Object access(Object cell) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("cell"), @NodeChild("newValue") })
	public static abstract class AssignNode extends OzNode {

		@Specialization
		Object assign(Object cell, Object newValue) {
			return unimplemented();
		}

	}

}
