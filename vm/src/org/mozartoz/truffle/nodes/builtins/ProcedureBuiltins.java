package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.call.CallableNode;
import org.mozartoz.truffle.nodes.list.OzListToObjectArrayNode;
import org.mozartoz.truffle.nodes.list.OzListToObjectArrayNodeGen;
import org.mozartoz.truffle.runtime.OzProc;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ProcedureBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsProcedureNode extends OzNode {

		@Specialization
		boolean isProcedure(OzProc value) {
			return true;
		}

		@Specialization(guards = "!isProc(value)")
		boolean isProcedure(Object value) {
			return false;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("procedure")
	public static abstract class ArityNode extends OzNode {

		@Specialization
		long arity(OzProc proc) {
			return proc.arity;
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("procedure"), @NodeChild("args") })
	public static abstract class ApplyNode extends OzNode {

		@Child DerefNode derefConsNode = DerefNode.create();
		@Child OzListToObjectArrayNode listToObjectArrayNode = OzListToObjectArrayNodeGen.create(null);

		@Specialization
		Object apply(VirtualFrame frame, Object receiver, Object args,
				@Cached("createCallNode()") CallableNode callNode) {
			Object[] arguments = listToObjectArrayNode.executeToObjectArray(args);
			callNode.executeCall(frame, receiver, arguments);
			return unit;
		}

		protected CallableNode createCallNode() {
			return CallNode.create();
		}

	}

}
