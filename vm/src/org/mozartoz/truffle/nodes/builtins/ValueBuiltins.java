package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltins.AttrGetNode;
import org.mozartoz.truffle.nodes.builtins.ObjectBuiltins.AttrPutNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.CatAccessOONodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.CatAssignOONodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCell;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzDict;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzFuture;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzThread;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

public abstract class ValueBuiltins {

	@Builtin(name = "==", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class EqualNode extends OzNode {

		public static EqualNode create() {
			return EqualNodeFactory.create(null, null);
		}

		public abstract boolean executeEqual(Object a, Object b);

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
		protected boolean equal(Unit a, Unit b) {
			return true;
		}

		@Specialization
		protected boolean equal(String a, String b) {
			return a == b;
		}

		@Specialization(guards = "!isAtom(b)")
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

		@Specialization(guards = { "!isCons(b)", "!isRecord(b)" })
		protected boolean equal(OzCons a, Object b) {
			return false;
		}

		@Specialization(guards = "!isRecord(b)")
		protected boolean equal(DynamicObject record, Object b) {
			return false;
		}

		@Specialization(guards = { "a.getClass() != b.getClass()",
				"!isLong(a)", "!isBigInteger(a)", "!isAtom(a)", "!isCons(a)", "!isRecord(a)" })
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

	}

	@Builtin(name = "=<", deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class LesserThanOrEqualNode extends OzNode {

		@Specialization
		protected boolean lesserThanOrEqual(long a, long b) {
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

		static final DynamicObjectFactory KERNEL_ERROR_FACTORY = Arity.build("kernel", 1L, 2L, 3L).createFactory();

		private OzException noFieldError(DynamicObject record, Object feature) {
			DynamicObject info = KERNEL_ERROR_FACTORY.newInstance("kernel", ".", record, feature);
			return new OzException(this, OzException.newError(info));
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
	@NodeChild("reference")
	public static abstract class CatAccessNode extends OzNode {

		@Specialization
		Object catAccess(OzCell cell) {
			return cell.getValue();
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

	@Builtin(deref = 1)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("reference"), @NodeChild("newValue") })
	public static abstract class CatExchangeNode extends OzNode {

		@Specialization
		Object catExchange(OzCell cell, Object newValue) {
			Object oldValue = cell.getValue();
			cell.setValue(newValue);
			return oldValue;
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

		@Specialization(guards = "!isVariable(value)")
		Object wait(Object value) {
			return unit;
		}

		@Specialization(guards = "isBound(var)")
		Object wait(OzVar var) {
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class WaitQuietNode extends OzNode {

		@Specialization(guards = "!isVariable(value)")
		Object wait(Object value) {
			return unit;
		}

		@Specialization(guards = "isBound(var)")
		Object wait(OzVar var) {
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class WaitNeededNode extends OzNode {

		@Specialization
		Object waitNeeded(Object value) {
			// FIXME: actually wait
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class MakeNeededNode extends OzNode {

		@Specialization
		Object makeNeeded(Object value) {
			// FIXME
			return unit;
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
		boolean isDet(OzVar var) {
			return var.isBound();
		}

		@Specialization(guards = "!isVariable(value)")
		boolean isDet(Object value) {
			return true;
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

		@Specialization
		boolean hasFeature(DynamicObject record, Object feature) {
			return record.getShape().hasProperty(feature);
		}

		@Specialization
		boolean hasFeature(OzChunk chunk, Object feature) {
			return chunk.getUnderlying().getShape().hasProperty(feature);
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

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("exception")
	public static abstract class FailedValueNode extends OzNode {

		@Specialization
		Object failedValue(DynamicObject data) {
			// FIXME
			throw new OzException(this, data);
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
