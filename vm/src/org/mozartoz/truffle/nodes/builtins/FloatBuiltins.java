package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

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

	@Builtin(name = "/", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class FloatDivNode extends OzNode {

		@Specialization
		double div(double left, double right) {
			return left / right;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ToIntNode extends OzNode {

		// round-to-even
		@Specialization
		long toInt(double value,
				@Cached("createBinaryProfile()") ConditionProfile positive,
				@Cached("createBinaryProfile()") ConditionProfile roundPosUp,
				@Cached("createBinaryProfile()") ConditionProfile roundPosDown,
				@Cached("createBinaryProfile()") ConditionProfile roundPosEven,
				@Cached("createBinaryProfile()") ConditionProfile roundNegUp,
				@Cached("createBinaryProfile()") ConditionProfile roundNegDown,
				@Cached("createBinaryProfile()") ConditionProfile roundNegEven) {
			long floor = (long) value; // rounds towards 0: -3.9 => -3
			double error = value - floor;
			assert error > -1.0 && error < 1.0;

			if (positive.profile(value >= 0.0)) {
				if (roundPosDown.profile(error < 0.5)) {
					return floor;
				} else if (roundPosUp.profile(error > 0.5)) {
					return floor + 1;
				} else {
					if (roundPosEven.profile((floor & 1) == 0)) {
						return floor;
					} else {
						return floor + 1;
					}
				}
			} else {
				if (roundNegUp.profile(error > -0.5)) {
					return floor;
				} else if (roundNegDown.profile(error < -0.5)) {
					return floor - 1;
				} else {
					if (roundNegEven.profile((floor & 1) == 0)) {
						return floor;
					} else {
						return floor - 1;
					}
				}
			}
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class FloorNode extends OzNode {

		@Specialization
		double floor(double value) {
			return Math.floor(value);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class LogNode extends OzNode {

		@Specialization
		double log(double value) {
			return Math.log(value);
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SinNode extends OzNode {

		@Specialization
		double sin(double value) {
			return Math.sin(value);
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

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class SqrtNode extends OzNode {

		@Specialization
		double sqrt(double value) {
			return Math.sqrt(value);
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
