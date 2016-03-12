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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("dict"), @NodeChild("feature") })
	public static abstract class MemberNode extends OzNode {

		@TruffleBoundary
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

		@TruffleBoundary
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

		@TruffleBoundary
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

		@TruffleBoundary
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

		@TruffleBoundary
		@Specialization
		Object exchangeFun(OzDict dict, Object feature, Object newValue) {
			Object oldValue = dict.put(feature, newValue);
			assert oldValue != null;
			return oldValue;
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

		@TruffleBoundary
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

		static final DynamicObjectFactory PAIR_FACTORY = Arity.build("#", 1L, 2L).createFactory();

		@TruffleBoundary
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
