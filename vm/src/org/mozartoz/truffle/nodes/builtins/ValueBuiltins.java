package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzError;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

public abstract class ValueBuiltins {

	@Builtin(name = "==")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class EqualNode extends OzNode {

		public static EqualNode create() {
			return EqualNodeFactory.create(null, null);
		}

		public abstract boolean executeEqual(Object a, Object b);

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected boolean equal(boolean a, boolean b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(long a, long b) {
			return a == b;
		}

		@Specialization(guards = { "!isLong(b)", "!isBigInteger(b)" })
		protected boolean equal(long a, Object b) {
			return false;
		}

		@Specialization(guards = { "!isLong(a)", "!isBigInteger(a)" })
		protected boolean equal(Object a, long b) {
			return false;
		}

		@Specialization
		protected boolean equal(BigInteger a, BigInteger b) {
			return a.equals(b);
		}

		@Specialization
		protected boolean equal(OzCons a, OzCons b) {
			return executeEqual(a.getHead(), b.getHead()) && executeEqual(a.getTail(), b.getTail());
		}

		@Specialization
		protected boolean equal(OzCons a, String b) {
			return false;
		}

		@Specialization
		protected boolean equal(String a, String b) {
			return a == b;
		}

	}

	@Builtin(name = "\\=")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class NotEqualNode extends OzNode {

		EqualNode equalNode = EqualNode.create();

		@Specialization
		boolean notEqual(Object left, Object right) {
			return !equalNode.executeEqual(left, right);
		}

	}

	@Builtin(name = "<")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class LesserThanNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected boolean lesserThan(long a, long b) {
			return a < b;
		}

	}

	@Builtin(name = "=<")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class LesserThanOrEqualNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected boolean lesserThanOrEqual(long a, long b) {
			return a <= b;
		}

	}

	@Builtin(name = ">")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class GreaterThanNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected boolean greaterThan(long a, long b) {
			return a > b;
		}

	}

	@Builtin(name = ".")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
	public static abstract class DotNode extends OzNode {

		@CreateCast("record")
		protected OzNode derefRecord(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization(guards = "feature == 1")
		protected Object getHead(OzCons cons, long feature) {
			return cons.getHead();
		}

		@Specialization(guards = "feature == 2")
		protected Object getTail(OzCons cons, long feature) {
			return cons.getTail();
		}

		@Specialization(guards = {
				"feature == cachedFeature",
				"record.getShape() == cachedShape"
		})
		protected Object getRecord(DynamicObject record, Object feature,
				@Cached("feature") Object cachedFeature,
				@Cached("record.getShape()") Shape cachedShape,
				@Cached("cachedShape.getProperty(cachedFeature)") Property property) {
			if (property != null) {
				return property.get(record, cachedShape);
			} else {
				CompilerDirectives.transferToInterpreter();
				throw new OzError("record has no feature " + cachedFeature);
			}
		}

		@Specialization
		protected Object getRecord(DynamicObject record, Object feature) {
			Object value = record.get(feature);
			if (value == null) {
				CompilerDirectives.transferToInterpreter();
				throw new OzError("record has no feature " + feature);
			}
			return value;
		}

	}

}
