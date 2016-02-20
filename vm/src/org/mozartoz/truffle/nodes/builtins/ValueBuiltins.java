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

	@Builtin(name = ">=")
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class GreaterThanOrEqualNode extends OzNode {

		@CreateCast("left")
		protected OzNode derefLeft(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@CreateCast("right")
		protected OzNode derefRight(OzNode var) {
			return DerefNodeGen.create(var);
		}

		@Specialization
		protected boolean greaterThanOrEqual(long a, long b) {
			return a >= b;
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

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("feature") })
	public static abstract class DotAssignNode extends OzNode {

		@Specialization
		Object dotAssign(Object value, Object feature) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class DotExchangeNode extends OzNode {

		@Specialization
		Object dotExchange(Object value, Object feature, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("reference")
	public static abstract class CatAccessNode extends OzNode {

		@Specialization
		Object catAccess(Object reference) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("reference")
	public static abstract class CatAssignNode extends OzNode {

		@Specialization
		Object catAssign(Object reference) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatExchangeNode extends OzNode {

		@Specialization
		Object catExchange(Object reference, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference") })
	public static abstract class CatAccessOONode extends OzNode {

		@Specialization
		Object catAccessOO(Object self, Object reference) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference") })
	public static abstract class CatAssignOONode extends OzNode {

		@Specialization
		Object catAssignOO(Object self, Object reference) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatExchangeOONode extends OzNode {

		@Specialization
		Object catExchangeOO(Object self, Object reference, Object newValue) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class WaitNode extends OzNode {

		@Specialization
		Object doWait() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class WaitQuietNode extends OzNode {

		@Specialization
		Object waitQuiet() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class WaitNeededNode extends OzNode {

		@Specialization
		Object waitNeeded() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class MakeNeededNode extends OzNode {

		@Specialization
		Object makeNeeded() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFreeNode extends OzNode {

		@Specialization
		Object isFree(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsKindedNode extends OzNode {

		@Specialization
		Object isKinded(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFutureNode extends OzNode {

		@Specialization
		Object isFuture(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFailedNode extends OzNode {

		@Specialization
		Object isFailed(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsDetNode extends OzNode {

		@Specialization
		Object isDet(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class StatusNode extends OzNode {

		@Specialization
		Object status(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class TypeNode extends OzNode {

		@Specialization
		Object type(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsNeededNode extends OzNode {

		@Specialization
		Object isNeeded(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
	public static abstract class HasFeatureNode extends OzNode {

		@Specialization
		Object hasFeature(Object record, Object feature) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature"), @NodeChild("def") })
	public static abstract class CondSelectNode extends OzNode {

		@Specialization
		Object condSelect(Object record, Object feature, Object def) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("exception")
	public static abstract class FailedValueNode extends OzNode {

		@Specialization
		Object failedValue(Object exception) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("variable")
	public static abstract class ReadOnlyNode extends OzNode {

		@Specialization
		Object readOnly(Object variable) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class NewReadOnlyNode extends OzNode {

		@Specialization
		Object newReadOnly() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("readOnly")
	public static abstract class BindReadOnlyNode extends OzNode {

		@Specialization
		Object bindReadOnly(Object readOnly) {
			return unimplemented();
		}

	}

}
