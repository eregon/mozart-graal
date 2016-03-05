package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltinsFactory.AttrGetNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltinsFactory.AttrPutNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;

public abstract class ObjectBuiltins {

	@Builtin(name = "new", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("clazz")
	public static abstract class NewObjectNode extends OzNode {

		static final OzUniqueName ooAttr = OzUniqueName.get("ooAttr");
		static final OzUniqueName ooFeat = OzUniqueName.get("ooFeat");
		static final OzUniqueName ooFreeFlag = OzUniqueName.get("ooFreeFlag");

		@Child DerefNode derefNode = DerefNode.create();

		@Specialization
		OzObject newObject(OzChunk clazz) {
			DynamicObject classDesc = clazz.getUnderlying();
			Object feat = classDesc.get(ooFeat);
			Object attr = classDesc.get(ooAttr);

			final DynamicObject features;
			if (feat instanceof String) {
				features = null;
			} else if (feat instanceof DynamicObject) {
				DynamicObject featRecord = (DynamicObject) feat;
				Arity featArity = OzRecord.getArity(featRecord);
				Object[] values = new Object[featArity.getWidth()];
				for (int i = 0; i < values.length; i++) {
					values[i] = new OzVar();
				}
				features = OzRecord.buildRecord(featArity, values);
				for (Property property : featRecord.getShape().getProperties()) {
					property.get(featRecord, featRecord.getShape());
				}
			} else {
				throw new Error();
			}

			final DynamicObject attributes;
			if (attr instanceof String) {
				attributes = null;
			} else if (attr instanceof DynamicObject) {
				DynamicObject attrRecord = (DynamicObject) attr;
				attributes = attrRecord.copy(attrRecord.getShape());
				for (Property property : attrRecord.getShape().getProperties()) {
					Object defaultValue = property.get(attrRecord, attrRecord.getShape());

					assert defaultValue == ooFreeFlag;
					property.setInternal(attributes, new OzVar());
				}
			} else {
				throw new Error();
			}

			return new OzObject(classDesc, features, attributes);
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("attribute") })
	public static abstract class AttrGetNode extends OzNode {

		public static AttrGetNode create() {
			return AttrGetNodeFactory.create(null, null);
		}

		public abstract Object executeAttrGet(OzObject object, Object attribute);

		@Specialization
		Object attrGet(OzObject object, Object attribute) {
			DynamicObject attributes = object.getAttributes();
			assert attributes.containsKey(attribute);
			return attributes.get(attribute);
		}

	}

	@Builtin(proc = true, deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("object"), @NodeChild("attribute"), @NodeChild("newValue") })
	public static abstract class AttrPutNode extends OzNode {

		public static AttrPutNode create() {
			return AttrPutNodeFactory.create(null, null, null);
		}

		public abstract Object executeAttrPut(OzObject object, Object attribute, Object newValue);

		@Specialization
		Object attrPut(OzObject object, Object attr, Object newValue) {
			//assert !(newValue instanceof OzVar) || ((OzVar) newValue).isBound();
			DynamicObject attributes = object.getAttributes();
			assert attributes.containsKey(attr);
			attributes.set(attr, newValue);
			return unit;
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
