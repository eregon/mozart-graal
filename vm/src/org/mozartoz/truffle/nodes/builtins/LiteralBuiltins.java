package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzName;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class LiteralBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsLiteralNode extends OzNode {

		@Specialization
		boolean isLiteral(String atom) {
			return true;
		}

		@Specialization
		boolean isLiteral(OzName name) {
			return true;
		}

		@Specialization(guards = "!isLiteral(value)")
		boolean isLiteral(Object value) {
			return false;
		}

	}

}
