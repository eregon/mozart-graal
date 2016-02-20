package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class PropertyBuiltins {

	@GenerateNodeFactory
	@NodeChild("property")
	public static abstract class RegisterValueNode extends OzNode {

		@Specialization
		Object registerValue(Object property) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("property")
	public static abstract class RegisterConstantNode extends OzNode {

		@Specialization
		Object registerConstant(Object property) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("result") })
	public static abstract class GetNode extends OzNode {

		@Specialization
		Object get(Object property, OzVar result) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("property"), @NodeChild("value") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		Object put(Object property, Object value) {
			return unimplemented();
		}

	}

}
