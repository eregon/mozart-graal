package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.io.PrintStream;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.SystemBuiltinsFactory.GetReprNodeFactory;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.CreateCast;
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

	@Builtin(proc = true, tryDeref = 1, deref = { 2, 3 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("toStdErr"), @NodeChild("newLine") })
	public static abstract class PrintReprNode extends OzNode {

		@Child GetReprNode getReprNode = GetReprNodeFactory.create(null, null, null);

		@Specialization(guards = "!isVariable(value)")
		Object printRepr(Object value, boolean toStdErr, boolean newLine) {
			String repr = getReprNode.executeGetRepr(value, 10, 1000);

			@SuppressWarnings("resource")
			PrintStream stream = toStdErr ? System.err : System.out;
			if (newLine) {
				stream.println(repr);
			} else {
				stream.print(repr);
				stream.flush();
			}
			return unit;
		}

		@Specialization(guards = "!isBound(var)")
		Object printRepr(OzVar var, boolean toStdErr, boolean newLine) {
			return printRepr(var.toString(), toStdErr, newLine);
		}

	}

	@Builtin(tryDeref = 1, deref = { 2, 3 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("depth"), @NodeChild("width") })
	public static abstract class GetReprNode extends OzNode {

		public abstract String executeGetRepr(Object value, long depth, long width);

		@Specialization(guards = "!isVariable(value)")
		String getRepr(Object value, long depth, long width) {
			return value.toString().intern();
		}

		@Specialization(guards = "!isBound(var)")
		String getRepr(OzVar var, long depth, long width) {
			return var.toString().intern();
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

		@CreateCast("value")
		protected OzNode castValue(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@Specialization
		Object printVS(String value, boolean toStdErr, boolean newLine) {
			@SuppressWarnings("resource")
			PrintStream stream = toStdErr ? System.err : System.out;
			if (newLine) {
				stream.println(value);
			} else {
				stream.print(value);
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
