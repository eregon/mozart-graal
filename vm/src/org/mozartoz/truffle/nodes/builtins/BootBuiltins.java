package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltins.ToAtomNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class BootBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetInternalNode extends OzNode {

		@Child ToAtomNode toAtomNode = ToAtomNodeFactory.create(null);

		@Specialization
		DynamicObject getInternal(Object nameVS) {
			String name = toAtomNode.executeToAtom(nameVS);
			return BuiltinsManager.getBootModule(("Boot_" + name).intern());
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetNativeNode extends OzNode {

		@Specialization
		Object getNative(Object name) {
			return unimplemented();
		}

	}

}
