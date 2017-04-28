package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

@MessageResolution(receiverType = RecordObjectType.class)
public class RecordMessageResolution {

	@CanResolve
	public abstract static class Check extends Node {

		protected static boolean test(TruffleObject obj) {
			return obj instanceof DynamicObject && ((DynamicObject) obj).getShape().getObjectType() == RecordObjectType.INSTANCE;
		}
	}

}
