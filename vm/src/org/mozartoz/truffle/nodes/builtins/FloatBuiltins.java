package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class FloatBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFloatNode extends OzNode {

		@Specialization
		boolean isFloat(double value) {
			return true;
		}

		@Specialization(guards = "!isFloat(value)")
		boolean isFloat(Object value) {
			return false;
		}

	}

	@Builtin(name = "/")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class FloatDivNode extends OzNode {

		@Specialization
		Object div(Object left, Object right) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class PowNode extends OzNode {

		@Specialization
		Object pow(Object left, Object right) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToIntNode extends OzNode {

		@Specialization
		long toInt(double value) {
			return Math.round(value);
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AcosNode extends OzNode {

		@Specialization
		Object acos(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AcoshNode extends OzNode {

		@Specialization
		Object acosh(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AsinNode extends OzNode {

		@Specialization
		Object asin(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AsinhNode extends OzNode {

		@Specialization
		Object asinh(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AtanNode extends OzNode {

		@Specialization
		Object atan(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class AtanhNode extends OzNode {

		@Specialization
		Object atanh(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class Atan2Node extends OzNode {

		@Specialization
		Object atan2(Object left, Object right) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class CeilNode extends OzNode {

		@Specialization
		Object ceil(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class CosNode extends OzNode {

		@Specialization
		Object cos(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class CoshNode extends OzNode {

		@Specialization
		Object cosh(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ExpNode extends OzNode {

		@Specialization
		Object exp(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class FloorNode extends OzNode {

		@Specialization
		Object floor(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class LogNode extends OzNode {

		@Specialization
		Object log(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class FModNode extends OzNode {

		@Specialization
		Object fMod(Object left, Object right) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RoundNode extends OzNode {

		@Specialization
		double round(double value) {
			return Math.floor(value + 0.5);
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SinNode extends OzNode {

		@Specialization
		Object sin(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SinhNode extends OzNode {

		@Specialization
		Object sinh(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SqrtNode extends OzNode {

		@Specialization
		Object sqrt(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class TanNode extends OzNode {

		@Specialization
		Object tan(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class TanhNode extends OzNode {

		@Specialization
		Object tanh(Object value) {
			return unimplemented();
		}

	}

}
