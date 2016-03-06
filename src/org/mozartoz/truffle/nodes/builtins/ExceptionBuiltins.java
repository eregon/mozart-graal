package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ExceptionBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	public static abstract class FailNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object fail() {
			throw new OzException(this, OzException.newFailure());
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object raise(Object data) {
			throw new OzException(this, data);
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseErrorNode extends OzNode {

		@TruffleBoundary
		@Specialization
		protected Object raiseError(Object error) {
			throw new OzException(this, OzException.newError(error));
		}

	}

}
