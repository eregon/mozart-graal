package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.OzContext;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class BootBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetInternalNode extends OzNode {

		@CreateCast("name")
		protected OzNode castName(OzNode name) {
			return ToAtomNodeFactory.create(name);
		}

		@Specialization
		DynamicObject getInternal(String name) {
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

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("base")
	public static abstract class RegisterBaseNode extends OzNode {

		@Specialization
		Object registerBase(DynamicObject base) {
			OzContext.getInstance().registerBase(base);
			return unit;
		}

	}

}
