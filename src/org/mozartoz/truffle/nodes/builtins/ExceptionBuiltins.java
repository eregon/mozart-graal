package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseErrorNodeFactory;
import org.mozartoz.truffle.runtime.OzException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ExceptionBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	public static abstract class FailNode extends OzNode {

		@Specialization
		Object fail() {
			return unimplemented();
		}

	}

	@Builtin(proc = true, deref = ALL)
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

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseErrorNode extends OzNode {

		public abstract Object executeRaiseError(Object value);

		@Specialization
		@TruffleBoundary
		protected Object raiseError(DynamicObject record) {
			String message = record.toString();
			throw new OzException(this, message);
		}

		@Specialization
		@TruffleBoundary
		protected Object raiseError(OzException exception) {
			throw exception;
		}

	}

}
