package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.util.LinkedList;
import java.util.List;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzThread;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class DebugBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("thread")
	public static abstract class GetRaiseOnBlockNode extends OzNode {

		@Specialization
		boolean getRaiseOnBlock(OzThread thread) {
			return thread.getRaiseOnBlock();
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("value") })
	public static abstract class SetRaiseOnBlockNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object setRaiseOnBlock(OzThread thread, boolean value) {
			if (value) {
				// TODO
				System.err.println("dummy implementation for Debug.setRaiseOnBlock");
			}
			thread.setRaiseOnBlock(value);
			return unit;
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("thread"), @NodeChild("id") })
	public static abstract class SetIdNode extends OzNode {

		@Specialization
		Object setId(Object thread, Object id) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	public static abstract class BreakpointNode extends OzNode {

		@Specialization
		Object breakpoint() {
			return unimplemented();
		}

	}

	@TruffleBoundary
	private static FrameSlot[] getSlots(List<? extends FrameSlot> slots, String name) {
		LinkedList<FrameSlot> ret = new LinkedList<>();
		for (FrameSlot slot : slots) {
			String id = (String) slot.getIdentifier();
			if (id.startsWith(name)) {
				ret.add(slot);
			}
		}
		assert ret.size() > 0 : name + " does not match any slot";
		return ret.toArray(new FrameSlot[ret.size()]);
	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class OnStackNode extends OzNode {

		@Specialization
		protected long isOnStack(VirtualFrame frame, String name) {
			Frame f = Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY);
			FrameSlot[] slots = getSlots(f.getFrameDescriptor().getSlots(), name);
			long onStack = slots.length;
			for (FrameSlot slot : slots) {
				if (f.getValue(slot) instanceof Variable) {
					onStack--;
				}
			}
			return onStack;
		}
	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class ClearedNode extends OzNode {

		@Specialization
		protected long isCleared(VirtualFrame frame, String name) {
			Frame f = Truffle.getRuntime().getCallerFrame().getFrame(FrameAccess.READ_ONLY);
			FrameSlot[] slots = getSlots(f.getFrameDescriptor().getSlots(), name);
			long cleared = slots.length;
			for (FrameSlot slot : slots) {
				if (f.getValue(slot) != null) {
					cleared--;
				}
			}
			return cleared;
		}
	}

}
