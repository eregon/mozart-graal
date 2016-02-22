package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class AtomBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsAtomNode extends OzNode {

		@CreateCast("value")
		protected OzNode derefValue(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		boolean isAtom(String value) {
			assert OzGuards.isInterned(value);
			return true;
		}

	}

}
