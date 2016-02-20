package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ProcedureBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsProcedureNode extends OzNode {

		@Specialization
		Object isProcedure(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("procedure")
	public static abstract class ArityNode extends OzNode {

		@Specialization
		Object arity(Object procedure) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("procedure"), @NodeChild("args") })
	public static abstract class ApplyNode extends OzNode {

		@Specialization
		Object apply(Object procedure, Object args) {
			return unimplemented();
		}

	}

}
