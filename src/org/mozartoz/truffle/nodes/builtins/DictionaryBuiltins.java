package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.util.Map.Entry;

import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzDict;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public abstract class DictionaryBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	public static abstract class NewDictionaryNode extends OzNode {

		@Specialization
		OzDict newDictionary() {
			return new OzDict();
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
		boolean isEmpty(OzDict dict) {
			return dict.isEmpty();
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

	@Builtin(proc = true, deref = { 1, 2 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class PutNode extends OzNode {

		@Specialization
		Object put(OzDict dict, Object feature, Object newValue) {
			assert OzGuards.isFeature(feature);
			dict.put(feature, newValue);
			return unit;
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

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class RemoveNode extends OzNode {

		@Specialization
		Object remove(OzDict dict, Object feature) {
			dict.remove(feature);
			return unit;
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class KeysNode extends OzNode {

		@Specialization
		Object keys(OzDict dict) {
			Object keys = "nil";
			for (Object key : dict.keySet()) {
				keys = new OzCons(key, keys);
			}
			return keys;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class EntriesNode extends OzNode {

		static final DynamicObjectFactory PAIR_FACTORY = Arity.build("#", 1L, 2L).createFactory();

		@Specialization
		Object entries(OzDict dict) {
			Object entries = "nil";
			for (Entry<Object, Object> entry : dict.entrySet()) {
				Object pair = PAIR_FACTORY.newInstance("#", entry.getKey(), entry.getValue());
				entries = new OzCons(pair, entries);
			}
			return entries;
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
