package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.util.Map.Entry;

import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.DictionaryBuiltinsFactory.ExchangeFunNodeFactory;
import org.mozartoz.truffle.nodes.builtins.DictionaryBuiltinsFactory.GetNodeFactory;
import org.mozartoz.truffle.nodes.builtins.DictionaryBuiltinsFactory.PutNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzDict;
import org.mozartoz.truffle.runtime.OzException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class MemberNode extends OzNode {

		@Specialization
		boolean member(OzDict dict, Object feature) {
			return dict.containsKey(feature);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class GetNode extends OzNode {

		public static GetNode create() {
			return GetNodeFactory.create(null, null);
		}

		public abstract Object executeGet(OzDict dict, Object feature);

		@Specialization
		Object get(OzDict dict, Object feature) {
			Object value = dict.get(feature);
			if (value == null) {
				throw new OzException(this, "Key not found");
			} else {
				return value;
			}
		}

	}

	@Builtin(deref = { 1, 2 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("defaultValue") })
	public static abstract class CondGetNode extends OzNode {

		@Specialization
		Object condGet(OzDict dict, Object feature, Object defaultValue) {
			Object value = dict.get(feature);
			if (value == null) {
				return defaultValue;
			} else {
				return value;
			}
		}

	}

	@Builtin(proc = true, deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class PutNode extends OzNode {

		public static PutNode create() {
			return PutNodeFactory.create(null, null, null);
		}

		public abstract Object executePut(OzDict dict, Object feature, Object newValue);

		@Specialization
		Object put(OzDict dict, Object feature, Object newValue) {
			assert OzGuards.isFeature(feature);
			dict.put(feature, newValue);
			return unit;
		}

	}

	@Builtin(deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class ExchangeFunNode extends OzNode {

		public static ExchangeFunNode create() {
			return ExchangeFunNodeFactory.create(null, null, null);
		}

		public abstract Object executeExchangeFun(OzDict dict, Object feature, Object newValue);

		@Specialization
		Object exchangeFun(OzDict dict, Object feature, Object newValue) {
			Object oldValue = dict.put(feature, newValue);
			assert oldValue != null;
			return oldValue;
		}

	}

	@Builtin(deref = { 1, 2 }, tryDeref = { 3, 4 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature"), @NodeChild("defaultValue"), @NodeChild("newValue") })
	public static abstract class CondExchangeFunNode extends OzNode {

		@Specialization
		Object condExchangeFun(OzDict dict, Object feature, Object defaultValue, Object newValue) {
			Object oldValue = dict.put(feature, newValue);
			if (oldValue == null) {
				return defaultValue;
			} else {
				return oldValue;
			}
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

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class RemoveAllNode extends OzNode {

		@Specialization
		Object removeAll(OzDict dict) {
			dict.clear();
			return unit;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class KeysNode extends OzNode {

		@TruffleBoundary
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

		@TruffleBoundary
		@Specialization
		Object entries(OzDict dict) {
			Object entries = "nil";
			for (Entry<Object, Object> entry : dict.entrySet()) {
				Object pair = Arity.PAIR_FACTORY.newRecord(entry.getKey(), entry.getValue());
				entries = new OzCons(pair, entries);
			}
			return entries;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("dict")
	public static abstract class ItemsNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object items(OzDict dict) {
			Object list = "nil";
			for (Object value : dict.values()) {
				list = new OzCons(value, list);
			}
			return list;
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
