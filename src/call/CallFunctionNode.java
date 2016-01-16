package call;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzFunction;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@NodeChildren({ @NodeChild("function") })
public abstract class CallFunctionNode extends OzNode {

	@Children final OzNode[] argumentNodes;

	public CallFunctionNode(OzNode[] argumentNodes) {
		this.argumentNodes = argumentNodes;
	}

	@CreateCast("function")
	protected OzNode derefFunction(OzNode var) {
		return DerefNodeGen.create(var);
	}

	@Specialization(guards = "function.callTarget == cachedCallTarget")
	protected Object call(VirtualFrame frame, OzFunction function,
			@Cached("function.callTarget") CallTarget cachedCallTarget,
			@Cached("create(cachedCallTarget)") DirectCallNode callNode) {
		final Object[] arguments = evaluateArguments(frame);
		return callNode.call(frame, OzArguments.pack(function.declarationFrame, arguments));
	}

	@ExplodeLoop
	private Object[] evaluateArguments(VirtualFrame frame) {
		Object[] arguments = new Object[argumentNodes.length];
		for (int i = 0; i < argumentNodes.length; i++) {
			arguments[i] = argumentNodes[i].execute(frame);
		}
		return arguments;
	}
}
