package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseErrorNodeFactory;
import org.mozartoz.truffle.runtime.OzError;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ExceptionBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseErrorNode extends OzNode {

		public abstract Object executeRaiseError(Object value);

		@CreateCast("value")
		protected OzNode derefValue(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		@TruffleBoundary
		protected Object raiseError(DynamicObject record) {
			String message = record.toString();
			throw new OzError(this, message);
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseNode extends OzNode {

		@Child RaiseErrorNode raiseErrorNode = RaiseErrorNodeFactory.create(null);

		@Specialization
		Object raise(Object value) {
			// TODO: should wrap in error(Value)
			return raiseErrorNode.executeRaiseError(value);
		}

	}

}
