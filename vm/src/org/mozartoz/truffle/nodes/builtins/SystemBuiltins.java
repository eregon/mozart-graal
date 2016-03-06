package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.io.PrintStream;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltins.ToAtomNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class SystemBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ShowNode extends OzNode {

		@Specialization
		protected Object show(Object value) {
			System.out.println(value);
			return unit;
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("toStdErr") })
	public static abstract class PrintReprNode extends OzNode {

		@Specialization
		Object printRepr(Object value, Object toStdErr) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("depth"), @NodeChild("width") })
	public static abstract class GetReprNode extends OzNode {

		@Specialization
		String getRepr(Object value, long depth, long width) {
			return value.toString().intern();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class PrintNameNode extends OzNode {

		@Specialization
		Object printName(Object value) {
			return unimplemented();
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("toStdErr"), @NodeChild("newLine") })
	public static abstract class PrintVSNode extends OzNode {

		@Child ToAtomNode toAtomNode = ToAtomNodeFactory.create(null);

		@Specialization
		Object printVS(Object value, boolean toStdErr, boolean newLine) {
			String buffer = toAtomNode.executeToAtom(value);
			@SuppressWarnings("resource")
			PrintStream stream = toStdErr ? System.err : System.out;
			if (newLine) {
				stream.println(buffer);
			} else {
				stream.print(buffer);
				stream.flush();
			}
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	public static abstract class GcDoNode extends OzNode {

		@Specialization
		Object gcDo() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("lhs"), @NodeChild("rhs") })
	public static abstract class EqNode extends OzNode {

		@Specialization
		Object eq(Object lhs, Object rhs) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class OnToplevelNode extends OzNode {

		@Specialization
		Object onToplevel() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("exitCode")
	public static abstract class ExitNode extends OzNode {

		@Specialization
		Object exit(Object exitCode) {
			return unimplemented();
		}

	}

}
