package org.mozartoz.truffle.nodes.call;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.SelfTailCallException;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;

public class SelfTailCallCatcherNode extends OzNode {

	OzNode body; // For serialization compliance
	@Child LoopNode loopNode;

	public SelfTailCallCatcherNode(OzNode body) {
		this.body = body;
		this.loopNode = Truffle.getRuntime().createLoopNode(new SelfTailCallLoopNode(body));
	}

	public static OzNode create(OzNode body) {
		if (!Options.SELF_TAIL_CALLS_OSR) {
			return new SelfTailCallCatcherNode.SelfTailCallCatcherNoOSRNode(body);
		}
		return new SelfTailCallCatcherNode(body);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		loopNode.executeLoop(frame);
		return unit;
	}

	public static class SelfTailCallLoopNode extends OzNode implements RepeatingNode {

		@Child OzNode body;

		public SelfTailCallLoopNode(OzNode body) {
			this.body = body;
		}

		@Override
		public boolean executeRepeating(VirtualFrame frame) {
			try {
				body.execute(frame);
				return false;
			} catch (SelfTailCallException e) {
				return true;
			}
		}

		@Override
		public Object execute(VirtualFrame frame) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return OzBacktrace.formatNode(this);
		}

	}

	public static class SelfTailCallCatcherNoOSRNode extends OzNode {

		@Child OzNode body;

		public SelfTailCallCatcherNoOSRNode(OzNode body) {
			this.body = body;
		}

		@Override
		public Object execute(VirtualFrame frame) {
			while (true) {
				try {
					return body.execute(frame);
				} catch (SelfTailCallException tailCall) {
				}
			}
		}

	}

}
