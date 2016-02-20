package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class DictionaryBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	public static abstract class NewDictionaryNode extends OzNode {

		@Specialization
		Object newDictionary() {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsDictionaryNode extends OzNode {

		@Specialization
		Object isDictionary(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class IsEmptyNode extends OzNode {

		@Specialization
		Object isEmpty(Object dict) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class MemberNode extends OzNode {

		@Specialization
		Object member(Object dict, Object feature) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class GetNode extends OzNode {

		@Specialization
		Object get(Object dict, Object feature) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("defaultValue") })
	public static abstract class CondGetNode extends OzNode {

		@Specialization
		Object condGet(Object dict, Object feature, Object defaultValue) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		Object put(Object dict, Object feature, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class ExchangeFunNode extends OzNode {

		@Specialization
		Object exchangeFun(Object dict, Object feature, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("defaultValue"), @NodeChild("newValue") })
	public static abstract class CondExchangeFunNode extends OzNode {

		@Specialization
		Object condExchangeFun(Object dict, Object feature, Object defaultValue, Object newValue) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class RemoveNode extends OzNode {

		@Specialization
		Object remove(Object dict, Object feature) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class RemoveAllNode extends OzNode {

		@Specialization
		Object removeAll(Object dict) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class KeysNode extends OzNode {

		@Specialization
		Object keys(Object dict) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class EntriesNode extends OzNode {

		@Specialization
		Object entries(Object dict) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class ItemsNode extends OzNode {

		@Specialization
		Object items(Object dict) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class CloneNode extends OzNode {

		@Specialization
		Object clone(Object dict) {
			return unimplemented();
		}

	}

}
