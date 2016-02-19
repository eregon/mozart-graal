package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class LiteralBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsLiteralNode extends OzNode {

		@Specialization
		Object isLiteral(Object value) {
			return unimplemented();
		}

	}

}
