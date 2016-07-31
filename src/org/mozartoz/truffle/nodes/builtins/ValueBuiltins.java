package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ArrayBuiltins.ArrayGetNode;
import org.mozartoz.truffle.nodes.builtins.ArrayBuiltins.ArrayPutNode;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltins.AttrGetNode;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltins.AttrPutNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.CatAccessOONodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.CatAssignOONodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.TypeNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzArray;
import org.mozartoz.truffle.runtime.OzCell;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzDict;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzFailedValue;
import org.mozartoz.truffle.runtime.OzFuture;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzReadOnly;
import org.mozartoz.truffle.runtime.OzThread;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.RecordFactory;
import org.mozartoz.truffle.runtime.Unit;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

public abstract class ValueBuiltins {

	@Builtin(name = "==", tryDeref = { 1, 2 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class EqualNode extends OzNode {

		public static EqualNode create() {
			return EqualNodeFactory.create(null, null);
		}

		public abstract boolean executeEqual(Object a, Object b);

		@Specialization(guards = { "!isBound(a)", "!isBound(b)" })
		protected boolean equal(OzVar a, OzVar b) {
			while (!a.isLinkedTo(b) && !a.isBound() && !b.isBound()) {
				OzThread.getCurrent().yield();
			}
			if (a.isLinkedTo(b)) {
				return true;
			} else {
				throw new OzException(this, "unimplemented");
			}
		}

		@Specialization(guards = { "!isBound(a)", "!isVariable(b)" })
		protected Object equal(OzVar a, Object b) {
			return executeEqual(a.waitValue(this), b);
		}

		@Specialization(guards = { "!isVariable(a)", "!isBound(b)" })
		protected Object equal(Object a, OzVar b) {
			return executeEqual(a, b.waitValue(this));
		}

		@Specialization
		protected boolean equal(boolean a, boolean b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(long a, long b) {
			return a == b;
		}

		@Specialization(guards = { "!isVariable(b)", "!isLong(b)", "!isBigInteger(b)" })
		protected boolean equal(long a, Object b) {
			return false;
		}

		@Specialization(guards = { "!isVariable(a)", "!isLong(a)", "!isBigInteger(a)" })
		protected boolean equal(Object a, long b) {
			return false;
		}

		@Specialization
		protected boolean equal(BigInteger a, BigInteger b) {
			return a.equals(b);
		}

		@Specialization
		protected boolean equal(double a, double b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(Unit a, Unit b) {
			return true;
		}

		@Specialization
		protected boolean equal(String a, String b) {
			return a == b;
		}

		@Specialization(guards = { "!isVariable(b)", "!isAtom(b)" })
		protected boolean equal(String a, Object b) {
			return false;
		}

		@Specialization
		protected boolean equal(OzName a, OzName b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(OzUniqueName a, OzUniqueName b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(OzThread a, OzThread b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(OzChunk a, OzChunk b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(OzObject a, OzObject b) {
			return a == b;
		}

		@Specialization
		protected boolean equal(OzProc a, OzProc b) {
			return a.equals(b);
		}

		@TruffleBoundary
		@Specialization
		protected boolean equal(OzCons a, OzCons b) {
			return executeEqual(a.getHead(), b.getHead()) && executeEqual(a.getTail(), b.getTail());
		}

		@TruffleBoundary
		@Specialization
		protected boolean equal(DynamicObject a, DynamicObject b) {
			if (a.getShape() != b.getShape()) {
				return false;
			}
			for (Property property : a.getShape().getProperties()) {
				Object aValue = property.get(a, a.getShape());
				Object bValue = property.get(b, b.getShape());
				if (!executeEqual(aValue, bValue)) {
					return false;
				}
			}
			return true;
		}

		@Specialization(guards = { "!isVariable(b)", "!isCons(b)", "!isRecord(b)" })
		protected boolean equal(OzCons a, Object b) {
			return false;
		}

		@Specialization(guards = { "!isVariable(b)", "!isRecord(b)" })
		protected boolean equal(DynamicObject record, Object b) {
			return false;
		}

		@Specialization(guards = { "a.getClass() != b.getClass()",
				"!isVariable(a)", "!isLong(a)", "!isBigInteger(a)", "!isAtom(a)", "!isCons(a)", "!isRecord(a)" })
		protected boolean equal(Object a, Object b) {
			return false;
		}

	}

	@Builtin(name = "\\=", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class NotEqualNode extends OzNode {

		@Child EqualNode equalNode = EqualNode.create();

		@Specialization
		boolean notEqual(Object left, Object right) {
			return !equalNode.executeEqual(left, right);
		}

	}

	@Builtin(name = "<", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class LesserThanNode extends OzNode {

		@Specialization
		protected boolean lesserThan(long a, long b) {
			return a < b;
		}

		@Specialization
		protected boolean lesserThan(double a, double b) {
			return a < b;
		}

		@Specialization
		protected boolean lesserThan(String a, String b) {
			return a.compareTo(b) < 0;
		}

	}

	@Builtin(name = "=<", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class LesserThanOrEqualNode extends OzNode {

		@Specialization
		protected boolean lesserThanOrEqual(long a, long b) {
			return a <= b;
		}

		@Specialization
		protected boolean lesserThanOrEqual(double a, double b) {
			return a <= b;
		}

	}

	@Builtin(name = ">=", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class GreaterThanOrEqualNode extends OzNode {

		@Specialization
		protected boolean greaterThanOrEqual(long a, long b) {
			return a >= b;
		}

		@Specialization
		protected boolean greaterThanOrEqual(double a, double b) {
			return a >= b;
		}

	}

	@Builtin(name = ">", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class GreaterThanNode extends OzNode {

		@Specialization
		protected boolean greaterThan(long a, long b) {
			return a > b;
		}

		@Specialization
		protected boolean greaterThan(double a, double b) {
			return a > b;
		}

		@Specialization
		protected boolean greaterThan(BigInteger a, BigInteger b) {
			return a.compareTo(b) > 0;
		}

	}

	@Builtin(name = ".", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
	public static abstract class DotNode extends OzNode {

		public abstract Object executeDot(Object record, Object feature);

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
				throw noFieldError(record, cachedFeature);
			}
		}

		@TruffleBoundary
		@Specialization
		protected Object getRecord(DynamicObject record, Object feature) {
			Object value = record.get(feature);
			if (value == null) {
				CompilerDirectives.transferToInterpreter();
				throw noFieldError(record, feature);
			}
			return value;
		}

		@Specialization
		protected Object getChunk(OzChunk chunk, Object feature) {
			return executeDot(chunk.getUnderlying(), feature);
		}

		@Specialization
		protected Object getObject(OzObject object, Object feature) {
			return executeDot(object.getFeatures(), feature);
		}

		@Specialization
		protected Object getDictionary(OzDict dict, Object feature,
				@Cached("create()") DictionaryBuiltins.GetNode getNode) {
			return getNode.executeGet(dict, feature);
		}

		@Specialization
		protected Object getArray(OzArray array, long feature,
				@Cached("create()") ArrayGetNode getNode) {
			return getNode.executeGet(array, feature);
		}

		private OzException noFieldError(DynamicObject record, Object feature) {
			throw kernelError(".", record, feature);
		}

	}

	@Builtin(proc = true, deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class DotAssignNode extends OzNode {

		@Specialization
		Object dotAssign(OzDict dict, Object feature, Object newValue,
				@Cached("create()") DictionaryBuiltins.PutNode putNode) {
			return putNode.executePut(dict, feature, newValue);
		}

		@Specialization
		Object dotAssign(OzArray array, long feature, Object newValue,
				@Cached("create()") ArrayPutNode putNode) {
			return putNode.executePut(array, feature, newValue);
		}

	}

	@Builtin(deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("feature"), @NodeChild("newValue") })
	public static abstract class DotExchangeNode extends OzNode {

		@Specialization
		Object dotExchange(OzDict dict, Object feature, Object newValue,
				@Cached("create()") DictionaryBuiltins.ExchangeFunNode exchangeFunNode) {
			return exchangeFunNode.executeExchangeFun(dict, feature, newValue);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@ImportStatic(Arity.class)
	@NodeChild("reference")
	public static abstract class CatAccessNode extends OzNode {

		@Specialization
		Object catAccess(OzCell cell) {
			return cell.getValue();
		}

		@Specialization(guards = "PAIR_ARITY.matches(pair)")
		Object catAccess(DynamicObject pair,
				@Cached("create()") DictionaryBuiltins.GetNode getNode) {
			OzDict dict = (OzDict) pair.get(1L);
			Object feature = pair.get(2L);
			return getNode.executeGet(dict, feature);
		}

	}

	@Builtin(proc = true, deref = 1)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatAssignNode extends OzNode {

		@Specialization
		Object catAssign(OzCell cell, Object newValue) {
			cell.setValue(newValue);
			return unit;
		}

	}

	@Builtin(deref = 1, tryDeref = 2)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatExchangeNode extends OzNode {

		@Specialization
		Object catExchange(OzCell cell, Object newValue) {
			return cell.exchange(newValue);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@ImportStatic(Arity.class)
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference") })
	public static abstract class CatAccessOONode extends OzNode {

		public static CatAccessOONode create() {
			return CatAccessOONodeFactory.create(null, null);
		}

		public abstract Object executeCatAccessOO(OzObject self, Object reference);

		@Specialization(guards = "PAIR_ARITY.matches(pair)")
		Object catAccessOO(OzObject self, DynamicObject pair,
				@Cached("create()") DictionaryBuiltins.GetNode getNode) {
			OzDict dict = (OzDict) pair.get(1L);
			Object feature = pair.get(2L);
			return getNode.executeGet(dict, feature);
		}

		@Specialization
		Object catAccessOO(OzObject self, OzCell cell) {
			return cell.getValue();
		}

		@Specialization
		Object catAccessOO(OzObject self, String attr,
				@Cached("create()") AttrGetNode attrGetNode) {
			return attrGetNode.executeAttrGet(self, attr);
		}

		@Specialization
		Object catAccessOO(OzObject self, OzName name,
				@Cached("create()") AttrGetNode attrGetNode) {
			return attrGetNode.executeAttrGet(self, name);
		}

	}

	@Builtin(proc = true, deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@ImportStatic(Arity.class)
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatAssignOONode extends OzNode {

		public static CatAssignOONode create() {
			return CatAssignOONodeFactory.create(null, null, null);
		}

		public abstract Object executeCatAssignOO(OzObject self, Object reference, Object newValue);

		@Specialization(guards = "PAIR_ARITY.matches(pair)")
		Object catAssignOO(OzObject self, DynamicObject pair, Object newValue,
				@Cached("create()") DictionaryBuiltins.PutNode putNode) {
			OzDict dict = (OzDict) pair.get(1L);
			Object feature = pair.get(2L);
			return putNode.executePut(dict, feature, newValue);
		}

		@Specialization
		Object catAssignOO(OzObject self, OzCell cell, Object newValue) {
			cell.setValue(newValue);
			return unit;
		}

		@Specialization
		Object catAssignOO(OzObject self, String attr, Object newValue,
				@Cached("create()") AttrPutNode attrPutNode) {
			return attrPutNode.executeAttrPut(self, attr, newValue);
		}

	}

	@Builtin(deref = { 1, 2 }, tryDeref = 3)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("self"), @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatExchangeOONode extends OzNode {

		@Specialization
		Object catExchangeOO(OzObject self, Object reference, Object newValue,
				@Cached("create()") CatAccessOONode catAccessOONode,
				@Cached("create()") CatAssignOONode catAssignOONode) {
			Object oldValue = catAccessOONode.executeCatAccessOO(self, reference);
			catAssignOONode.executeCatAssignOO(self, reference, newValue);
			return oldValue;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class WaitNode extends OzNode {

		public abstract Object executeWait(Object value);

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		Object wait(Object value) {
			return unit;
		}

		@Specialization(guards = "isBound(var)")
		Object wait(OzVar var) {
			return check(var.getBoundValue(this));
		}

		@Specialization(guards = "!isBound(var)")
		Object waitUnbound(OzVar var) {
			return check(var.waitValue(this));
		}

		@Specialization
		Object wait(OzFailedValue failedValue) {
			return check(failedValue);
		}

		private Object check(Object value) {
			if (value instanceof OzFailedValue) {
				throw new OzException(this, ((OzFailedValue) value).getData());
			}
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class WaitQuietNode extends OzNode {

		public abstract Object executeWaitQuiet(Object value);

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		Object waitQuiet(Object value) {
			return unit;
		}

		@Specialization(guards = "isBound(var)")
		Object waitQuiet(OzVar var) {
			return executeWaitQuiet(var.getBoundValue(this));
		}

		@Specialization(guards = "!isBound(var)")
		Object waitQuietUnbound(OzVar var) {
			return executeWaitQuiet(var.waitValueQuiet(this));
		}

		@Specialization
		Object waitQuiet(OzFailedValue failedValue) {
			return unit;
		}

	}

	@Builtin(proc = true, tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class WaitNeededNode extends OzNode {

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		Object waitNeeded(Object value) {
			return unit;
		}

		@Specialization(guards = "!isBound(var)")
		Object waitNeeded(OzVar var) {
			while (!var.isNeeded()) {
				OzThread.getCurrent().yield();
			}
			return unit;
		}

	}

	@Builtin(proc = true, tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class MakeNeededNode extends OzNode {

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		Object makeNeeded(Object value) {
			return unit;
		}

		@Specialization(guards = "!isBound(var)")
		Object makeNeeded(OzVar var) {
			var.makeNeeded();
			return unit;
		}

	}

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFreeNode extends OzNode {

		@Specialization(guards = "!isBound(var)")
		boolean isFree(OzVar var) {
			return true;
		}

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		boolean isFree(Object value) {
			return false;
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

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsFailedNode extends OzNode {

		@Specialization
		boolean isFailed(OzFailedValue failedValue) {
			return true;
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsDetNode extends OzNode {

		@Specialization
		boolean isDet(Variable var) {
			return var.isBound();
		}

		@Specialization(guards = "!isVariable(value)")
		boolean isDet(Object value) {
			return true;
		}

	}

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class StatusNode extends OzNode {

		static final RecordFactory DET_FACTORY = Arity.build("det", 1L).createFactory();

		@Specialization(guards = { "!isVariable(value)", "!isFailedValue(value)" })
		Object status(Object value,
				@Cached("create()") TypeNode typeNode) {
			return DET_FACTORY.newRecord(typeNode.executeType(value));
		}

		@Specialization(guards = "!isBound(var)")
		Object status(OzVar var) {
			return "free";
		}

		@Specialization
		String status(OzFailedValue failedValue) {
			return "failed";
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class TypeNode extends OzNode {

		public static TypeNode create() {
			return TypeNodeFactory.create(null);
		}

		public abstract String executeType(Object value);

		@Specialization
		String type(long value) {
			return "int";
		}

		@Specialization
		String type(double value) {
			return "float";
		}

		@Specialization
		String type(String atom) {
			return "atom";
		}

		@Specialization
		String type(OzCons cons) {
			return "tuple";
		}

		@Specialization
		String type(DynamicObject record) {
			if (Arity.forRecord(record).isTupleArity()) {
				return "tuple";
			} else {
				return "record";
			}
		}

	}

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsNeededNode extends OzNode {

		@Specialization(guards = "!isVariable(value)")
		boolean isNeeded(Object value) {
			return true;
		}

		@Specialization(guards = "!isBound(var)")
		boolean isNeeded(OzVar var) {
			return var.isNeeded();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature") })
	public static abstract class HasFeatureNode extends OzNode {

		@Specialization
		boolean hasFeature(String atom, Object feature) {
			return false;
		}

		@Specialization
		boolean hasFeature(Unit unit, Object feature) {
			return false;
		}

		@Specialization
		boolean hasFeature(OzCons cons, long feature) {
			return feature == 1 || feature == 2;
		}

		@Specialization(guards = "!isLong(feature)")
		boolean hasFeature(OzCons cons, Object feature) {
			return false;
		}

		@Specialization
		boolean hasFeature(DynamicObject record, Object feature) {
			return record.getShape().hasProperty(feature);
		}

		@Specialization
		boolean hasFeature(OzChunk chunk, Object feature) {
			return chunk.getUnderlying().getShape().hasProperty(feature);
		}

		@Specialization
		boolean hasFeature(OzObject object, Object feature) {
			return object.getFeatures().getShape().hasProperty(feature);
		}

	}

	@Builtin(deref = { 1, 2 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("record"), @NodeChild("feature"), @NodeChild("def") })
	public static abstract class CondSelectNode extends OzNode {

		@Specialization
		Object condSelect(boolean bool, Object feature, Object def) {
			return def;
		}

		@Specialization
		Object condSelect(DynamicObject record, Object feature, Object def) {
			return record.get(feature, def);
		}

		@Specialization
		Object condSelect(OzObject object, Object feature, Object def) {
			return object.getFeatures().get(feature, def);
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("exception")
	public static abstract class FailedValueNode extends OzNode {

		@Specialization
		OzFailedValue failedValue(Object data) {
			return new OzFailedValue(data);
		}

	}

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("variable")
	public static abstract class ReadOnlyNode extends OzNode {

		@Specialization
		OzReadOnly readOnly(OzVar variable) {
			return new OzReadOnly(variable);
		}

		@Specialization
		Object readOnly(OzFailedValue failedValue) {
			return failedValue;
		}

	}

	@GenerateNodeFactory
	public static abstract class NewReadOnlyNode extends OzNode {

		@Specialization
		OzFuture newReadOnly() {
			return new OzFuture();
		}

	}

	@Builtin(proc = true, tryDeref = 2)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("readOnly"), @NodeChild("value") })
	public static abstract class BindReadOnlyNode extends OzNode {

		@Specialization(guards = "!isBound(future)")
		Object bindReadOnly(OzFuture future, Object value) {
			future.bind(value);
			return unit;
		}

		@Specialization(guards = "linkedToFuture(var)")
		Object bindReadOnly(OzVar var, Object value) {
			var.findFuture().bind(value);
			return unit;
		}

		boolean linkedToFuture(OzVar var) {
			return var.findFuture() != null;
		}

	}

}
