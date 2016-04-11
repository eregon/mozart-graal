package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzString;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;

public abstract class VirtualStringBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsVirtualStringNode extends OzNode {

		public abstract boolean executeIsVirtualString(Object value);

		@Specialization
		boolean isVirtualString(long value) {
			return true;
		}

		@Specialization
		boolean isVirtualString(double value) {
			return true;
		}

		@Specialization
		boolean isVirtualString(String atom) {
			return true;
		}

		@Specialization
		boolean isVirtualString(OzCons cons) {
			Object list = cons;
			while (list instanceof OzCons) {
				Object head = ((OzCons) list).getHead();
				assert head instanceof Long;
				list = ((OzCons) list).getTail();
			}
			assert list == "nil";
			return true;
		}

		@Specialization
		boolean isVirtualString(DynamicObject tuple) {
			Arity arity = OzRecord.getArity(tuple);
			if (arity.isTupleArity() && arity.getLabel() == "#") {
				for (long i = 1L; i <= arity.getWidth(); i++) {
					Object value = tuple.get(i);
					if (!executeIsVirtualString(value)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Specialization
		boolean isVirtualString(OzObject object) {
			return false;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToCompactStringNode extends OzNode {

		@Child ToAtomNode toAtomNode = ToAtomNode.create();

		@Specialization
		OzString toCompactString(Object value) {
			String chars = toAtomNode.executeToAtom(value);
			return new OzString(chars);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("tail") })
	public static abstract class ToCharListNode extends OzNode {

		@Child DerefNode derefNode = DerefNode.create();

		public abstract Object executeToCharList(Object value, Object tail);

		@Specialization
		Object toCharList(long number, Object tail) {
			String str = Long.toString(number).intern();
			return executeToCharList(str, tail);
		}

		@Specialization
		Object toCharList(double number, Object tail) {
			String str = Double.toString(number).intern();
			return executeToCharList(str, tail);
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
			// We want to traverse in reverse order here
			for (Property property : record.getShape().getPropertyListInternal(false)) {
				if (!property.isHidden()) {
					Object value = deref(property.get(record, record.getShape()));
					if (value != "nil") {
						list = executeToCharList(value, list);
					}
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

		public static ToAtomNode create() {
			return ToAtomNodeFactory.create(null);
		}

		@Child DerefNode derefNode = DerefNode.create();

		public abstract String executeToAtom(Object value);

		@Specialization
		String toAtom(long number) {
			return Long.toString(number).intern();
		}

		@Specialization
		String toAtom(String atom) {
			assert OzGuards.isAtom(atom);
			assert atom != "nil";
			return atom;
		}

		@Specialization
		String toAtom(OzString string) {
			return string.getChars().intern();
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
				Object value = deref(property.get(record, record.getShape()));
				if (value != "nil") {
					builder.append(executeToAtom(value));
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
			try {
				return Double.valueOf(atom.replace('~', '-'));
			} catch (Exception e) {
				throw kernelError("stringNoFloat", atom);
			}
		}

	}

}
