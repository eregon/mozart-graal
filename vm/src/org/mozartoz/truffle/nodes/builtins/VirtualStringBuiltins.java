package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.Errors;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzString;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
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

		@Child DerefNode derefNode = DerefNode.create();

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
				OzCons xs = (OzCons) list;
				Object head = deref(xs.getHead());
				assert head instanceof Long;
				list = deref(xs.getTail());
			}
			assert list == "nil";
			return true;
		}

		@TruffleBoundary
		@Specialization
		boolean isVirtualString(DynamicObject tuple) {
			Arity arity = OzRecord.getArity(tuple);
			if (arity.isTupleArity() && arity.getLabel() == "#") {
				for (long i = 1L; i <= arity.getWidth(); i++) {
					Object value = deref(tuple.get(i));
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

		private Object deref(Object value) {
			return derefNode.executeDeref(value);
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

		@TruffleBoundary
		@Specialization
		Object toCharList(long number, Object tail) {
			String str = Long.toString(number).intern();
			return executeToCharList(str, tail);
		}

		@TruffleBoundary
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

		@TruffleBoundary
		@Specialization
		Object toCharList(OzCons cons, Object tail) {
			Object head = deref(cons.getHead());
			assert head instanceof Long;
			Object consTail = deref(cons.getTail());
			if (consTail == "nil") {
				return new OzCons(head, tail);
			} else {
				return new OzCons(head, executeToCharList(consTail, tail));
			}
		}

		@TruffleBoundary
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

		@TruffleBoundary
		@Specialization
		String toAtom(long number) {
			return Long.toString(number).intern();
		}

		@Specialization
		String toAtom(String atom) {
			assert OzGuards.isAtom(atom);
			return atom;
		}

		@Specialization
		String toAtom(OzString string) {
			return string.getChars().intern();
		}

		@TruffleBoundary
		@Specialization
		String toAtom(OzCons cons) {
			StringBuilder builder = new StringBuilder();
			Object list = cons;
			while (list instanceof OzCons) {
				OzCons xs = (OzCons) list;
				Object head = deref(xs.getHead());
				assert head instanceof Long;
				long longHead = (long) head;
				char c = (char) longHead;
				builder.append(c);
				list = deref(xs.getTail());
			}
			assert list == "nil";
			return builder.toString().intern();
		}

		@TruffleBoundary
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

		@Child DerefNode derefNode = DerefNode.create();

		public abstract long executeLength(Object value);

		@Specialization
		long length(long number) {
			return Long.toString(number).length();
		}

		@Specialization
		long length(String atom) {
			return atom.length();
		}

		@TruffleBoundary
		@Specialization
		long length(OzCons cons) {
			long length = 0;
			Object list = cons;
			while (list instanceof OzCons) {
				OzCons xs = (OzCons) list;
				Object head = deref(xs.getHead());
				assert head instanceof Long;
				length += 1;
				list = deref(xs.getTail());
			}
			assert list == "nil";
			return length;
		}

		@TruffleBoundary
		@Specialization
		long length(DynamicObject record) {
			assert OzRecord.getLabel(record) == "#";
			long length = 0;
			for (Property property : record.getShape().getProperties()) {
				Object value = deref(property.get(record, record.getShape()));
				if (value != "nil") {
					length += executeLength(value);
				}
			}
			return length;
		}

		private Object deref(Object value) {
			return derefNode.executeDeref(value);
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

		@TruffleBoundary
		@Specialization
		double toFloat(String atom) {
			try {
				return Double.valueOf(atom.replace('~', '-'));
			} catch (Exception e) {
				throw Errors.kernelError(this, "stringNoFloat", atom);
			}
		}

	}

}
