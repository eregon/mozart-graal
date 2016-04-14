package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class VirtualByteStringBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsVirtualByteStringNode extends OzNode {

		public abstract boolean executeIsVirtualByteString(Object value);

		@Specialization
		boolean isVirtualByteString(String atom) {
			return atom == "nil";
		}

		@Specialization
		boolean isVirtualByteString(OzCons cons) {
			Object list = cons;
			while (list instanceof OzCons) {
				Object head = ((OzCons) list).getHead();
				assert head instanceof Long;
				list = ((OzCons) list).getTail();
			}
			assert list == "nil";
			return true;
		}

		@TruffleBoundary
		@Specialization
		boolean isVirtualByteString(DynamicObject tuple) {
			Arity arity = OzRecord.getArity(tuple);
			if (arity.isTupleArity() && arity.getLabel() == "#") {
				for (long i = 1L; i <= arity.getWidth(); i++) {
					Object value = tuple.get(i);
					if (!executeIsVirtualByteString(value)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToCompactByteStringNode extends OzNode {

		@Specialization
		Object toCompactByteString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("tail") })
	public static abstract class ToByteListNode extends OzNode {

		@Specialization
		Object toByteList(Object value, Object tail) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class LengthNode extends OzNode {

		@Specialization
		Object length(Object value) {
			return unimplemented();
		}

	}

}
