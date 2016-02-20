package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ObjectBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("clazz")
	public static abstract class NewObjectNode extends OzNode {

		@Specialization
		Object newObject(Object clazz) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsObjectNode extends OzNode {

		@Specialization
		Object isObject(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("object")
	public static abstract class GetClassNode extends OzNode {

		@Specialization
		Object getClass(Object object) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("attribute") })
	public static abstract class AttrGetNode extends OzNode {

		@Specialization
		Object attrGet(Object object, Object attribute) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("attribute"), @NodeChild("newValue") })
	public static abstract class AttrPutNode extends OzNode {

		@Specialization
		Object attrPut(Object object, Object attribute, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("attribute"), @NodeChild("newValue") })
	public static abstract class AttrExchangeFunNode extends OzNode {

		@Specialization
		Object attrExchangeFun(Object object, Object attribute, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("cellOrAttr") })
	public static abstract class CellOrAttrGetNode extends OzNode {

		@Specialization
		Object cellOrAttrGet(Object object, Object cellOrAttr) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("cellOrAttr"), @NodeChild("newValue") })
	public static abstract class CellOrAttrPutNode extends OzNode {

		@Specialization
		Object cellOrAttrPut(Object object, Object cellOrAttr, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("cellOrAttr"), @NodeChild("newValue") })
	public static abstract class CellOrAttrExchangeFunNode extends OzNode {

		@Specialization
		Object cellOrAttrExchangeFun(Object object, Object cellOrAttr, Object newValue) {
			return unimplemented();
		}

	}

}
