package call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzFunction;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;

@NodeChildren({ @NodeChild("function"), @NodeChild("argument") })
public abstract class CallFunctionNode extends OzNode {

	@Specialization(guards = "function.callTarget == cachedCallTarget")
	protected Object call(VirtualFrame frame, OzFunction function, Object argument,
			@Cached("function.callTarget") CallTarget cachedCallTarget,
			@Cached("create(cachedCallTarget)") DirectCallNode callNode) {
		return callNode.call(frame, OzArguments.pack(function.declarationFrame, argument));
	}
}
