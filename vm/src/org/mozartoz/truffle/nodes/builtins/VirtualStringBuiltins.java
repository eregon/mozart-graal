package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
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

		@Child DerefNode derefNode = DerefNode.create();

		public abstract Object executeToCharList(Object value, Object tail);

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
			Object head = cons.getHead();
			assert head instanceof Long;
			Object consTail = deref(cons.getTail());
			if (consTail == "nil") {
				return new OzCons(head, tail);
			} else {
				return new OzCons(head, executeToCharList(consTail, tail));
			}
		}

		@Specialization
		Object toCharList(DynamicObject record, Object tail) {
			assert OzRecord.getLabel(record) == "#";
			Object list = tail;
			for (Property property : record.getShape().getPropertyListInternal(false)) {
				if (!property.isHidden()) {
					Object value = property.get(record, record.getShape());
					list = executeToCharList(deref(value), list);
				}
			}
			return list;
		}

		private Object deref(Object value) {
			return derefNode.executeDeref(value);
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
				if (!property.isHidden()) {
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToFloatNode extends OzNode {

		@CreateCast("value")
		protected OzNode castValue(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@Specialization
		double toFloat(String atom) {
			return Double.valueOf(atom.replace('~', '-'));
		}

	}

}
