package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

@ImportStatic(Options.class)
public abstract class ReadCapturedVariableNode extends OzNode implements FrameSlotNode {

	final FrameSlot slot;
	final int depth;

	public ReadCapturedVariableNode(FrameSlot slot, int depth) {
		this.slot = slot;
		this.depth = depth;
	}

	public FrameSlot getSlot() {
		return this.slot;
	}

	@Specialization(guards = "CACHE_READ")
	protected Object readWithCache(VirtualFrame frame,
			@Cached("createCachingReadNode(getSlot())") CachingReadFrameSlotNode readNode) {
		Frame parentFrame = OzArguments.getParentFrame(frame, depth);
		return readNode.executeRead(parentFrame);
	}

	@Specialization(guards = "!CACHE_READ")
	protected Object readWithoutCache(VirtualFrame frame,
			@Cached("createReadNode(getSlot())") ReadFrameSlotNode readNode) {
		Frame parentFrame = OzArguments.getParentFrame(frame, depth);
		return readNode.executeRead(parentFrame);
	}

	protected ReadFrameSlotNode createReadNode(FrameSlot slot) {
		return ReadFrameSlotNodeGen.create(slot);
	}

	protected CachingReadFrameSlotNode createCachingReadNode(FrameSlot slot) {
		return CachingReadFrameSlotNodeGen.create(slot);
	}

}
