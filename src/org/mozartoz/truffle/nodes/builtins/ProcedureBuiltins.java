package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ProcedureBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsProcedureNode extends OzNode {

		@Specialization
		boolean isProcedure(OzProc value) {
			return true;
		}

		@Specialization(guards = "!isProc(value)")
		boolean isProcedure(Object value) {
			return false;
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
