package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.util.ArrayList;
import java.util.List;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.call.CallableNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzObject;
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

		@Specialization
		Object apply(VirtualFrame frame, OzProc proc, OzCons args,
				@Cached("createCallNode()") CallableNode callNode) {
			List<Object> list = new ArrayList<>();
			args.forEach(derefConsNode, e -> list.add(e));
			Object[] arguments = list.toArray(new Object[list.size()]);
			callNode.executeCall(frame, proc, arguments);
			return unit;
		}

		@Specialization
		Object apply(VirtualFrame frame, OzObject object, OzCons args) {
			return unimplemented();
		}

		protected CallableNode createCallNode() {
			return CallNode.create();
		}

	}

}
