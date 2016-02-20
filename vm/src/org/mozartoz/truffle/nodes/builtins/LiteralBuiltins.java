package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class LiteralBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsLiteralNode extends OzNode {

		@CreateCast("value")
		protected OzNode derefValue(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		boolean isLiteral(String atom) {
			return true;
		}

	}

}
