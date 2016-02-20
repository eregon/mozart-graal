package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class SystemBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ShowNode extends OzNode {

		@Specialization
		protected Object show(long value) {
			System.out.println(value);
			return unit;
		}

		@Specialization
		protected Object show(BigInteger value) {
			System.out.println(value);
			return unit;
		}

		@Specialization
		protected Object show(OzCons list) {
			System.out.println(list);
			return unit;
		}

		@Specialization
		protected Object show(OzVar var) {
			System.out.println(var);
			return unit;
		}

		@Specialization
		protected Object show(DynamicObject record) {
			System.out.println(record);
			return unit;
		}

		@Specialization
		protected Object show(Unit unit) {
			System.out.println(unit);
			return unit;
		}

		@Specialization
		protected Object show(String atom) {
			System.out.println(atom);
			return unit;
		}

		@Specialization(guards = "undefined == null")
		protected Object show(Object undefined) {
			System.out.println("null");
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

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("depth"), @NodeChild("width") })
	public static abstract class GetReprNode extends OzNode {

		@Specialization
		Object getRepr(Object value, Object depth, Object width) {
			return unimplemented();
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

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("toStdErr") })
	public static abstract class PrintVSNode extends OzNode {

		@Specialization
		Object printVS(Object value, Object toStdErr) {
			return unimplemented();
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
