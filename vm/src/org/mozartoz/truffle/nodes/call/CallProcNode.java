package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzUniqueName;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChildren({ @NodeChild("function") })
public abstract class CallProcNode extends OzNode {

	@Child ExecuteValuesNode executeArgumentsNode;

	public CallProcNode(OzNode[] argumentNodes) {
		this.executeArgumentsNode = new ExecuteValuesNode(argumentNodes);
	}

	@CreateCast("function")
	protected OzNode derefFunction(OzNode var) {
		return DerefNodeGen.create(var);
	}

	@Specialization(guards = "function.callTarget == cachedCallTarget")
	protected Object callDirect(VirtualFrame frame, OzProc function,
			@Cached("function.callTarget") CallTarget cachedCallTarget,
			@Cached("create(cachedCallTarget)") DirectCallNode callNode) {
		final Object[] arguments = executeArgumentsNode.executeValues(frame);
		return callNode.call(frame, OzArguments.pack(function.declarationFrame, arguments));
	}

	@Specialization
	protected Object callIndirect(VirtualFrame frame, OzProc function,
			@Cached("create()") IndirectCallNode callNode) {
		final Object[] arguments = executeArgumentsNode.executeValues(frame);
		return callNode.call(frame, function.callTarget, OzArguments.pack(function.declarationFrame, arguments));
	}

	static final OzUniqueName ooMeth = OzUniqueName.get("ooMeth");

	@Specialization
	protected Object callObject(VirtualFrame frame, OzObject self,
			@Cached("create()") IndirectCallNode callNode,
			@Cached("create()") DerefNode derefNode) {
		final Object[] messages = executeArgumentsNode.executeValues(frame);
		assert messages.length == 1;
		Object message = derefNode.executeDeref(messages[0]);

		String name;
		if (message instanceof String) {
			name = (String) message;
		} else {
			name = (String) OzRecord.getArity((DynamicObject) message).getLabel();
		}

		DynamicObject methods = (DynamicObject) self.getClazz().get(ooMeth);
		OzProc method = (OzProc) methods.get(name);
		Object[] arguments = new Object[] { self, message };
		return callNode.call(frame, method.callTarget, OzArguments.pack(method.declarationFrame, arguments));
	}

}
