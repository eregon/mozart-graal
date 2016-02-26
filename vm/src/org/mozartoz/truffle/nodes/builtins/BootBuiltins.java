package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class BootBuiltins {

	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetInternalNode extends OzNode {

		@Specialization
		Object getInternal(Object name) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetNativeNode extends OzNode {

		@Specialization
		Object getNative(Object name) {
			return unimplemented();
		}

	}

}
