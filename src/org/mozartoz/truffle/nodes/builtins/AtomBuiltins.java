package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class AtomBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsAtomNode extends OzNode {

		@Specialization
		boolean isAtom(String value) {
			assert OzGuards.isInterned(value);
			return true;
		}

		@Specialization
		boolean isAtom(DynamicObject record) {
			return false;
		}

	}

}
