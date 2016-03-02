package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.object.Property;

public abstract class VirtualStringBuiltins {

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsVirtualStringNode extends OzNode {

		@Specialization
		Object isVirtualString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToCompactStringNode extends OzNode {

		@Specialization
		Object toCompactString(Object value) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("tail") })
	public static abstract class ToCharListNode extends OzNode {

		public abstract Object executeToCharList(Object value, Object tail);

		@Specialization
		Object toCharList(long codePoint, Object tail) {
			return new OzCons(codePoint, tail);
		}

		@Specialization
		Object toCharList(String atom, Object tail) {
			Object list = tail;
			for (int i = atom.length() - 1; i >= 0; i--) {
				list = new OzCons((long) atom.charAt(i), list);
			}
			return list;
		}

		@Specialization
		Object toCharList(OzCons cons, Object tail) {
			if (cons.getTail() == "nil") {
				return executeToCharList(cons.getHead(), tail);
			} else {
				return executeToCharList(cons.getHead(),
						executeToCharList(cons.getTail(), tail));
			}
		}

		@Specialization
		Object toCharList(DynamicObject record, Object tail) {
			assert OzRecord.getLabel(record) == "#";
			Object list = tail;
			for (Property property : record.getShape().getPropertyListInternal(false)) {
				if (!(property.getKey() instanceof HiddenKey)) {
					Object value = property.get(record, record.getShape());
					list = executeToCharList(value, list);
				}
			}
			return list;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToAtomNode extends OzNode {

		@Child DerefNode derefNode = DerefNode.create();

		public abstract String executeToAtom(Object value);

		@Specialization
		String toAtom(String atom) {
			assert OzGuards.isAtom(atom);
			return atom;
		}

		@Specialization
		String toAtom(OzCons cons) {
			Object head = cons.getHead();
			long longHead = (long) head;
			char c = (char) longHead;
			Object tail = deref(cons.getTail());
			if (tail == "nil") {
				return ("" + c).intern();
			} else {
				return (c + executeToAtom(tail)).intern();
			}
		}

		@Specialization
		String toAtom(DynamicObject record) {
			assert OzRecord.getLabel(record) == "#";
			StringBuilder builder = new StringBuilder();
			for (Property property : record.getShape().getProperties()) {
				if (!(property.getKey() instanceof HiddenKey)) {
					Object value = property.get(record, record.getShape());
					builder.append(executeToAtom(deref(value)));
				}
			}
			return builder.toString().intern();
		}

		private Object deref(Object value) {
			return derefNode.executeDeref(value);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class LengthNode extends OzNode {

		@Specialization
		long length(String atom) {
			return atom.length();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToFloatNode extends OzNode {

		@Specialization
		Object toFloat(Object value) {
			return unimplemented();
		}

	}

}
