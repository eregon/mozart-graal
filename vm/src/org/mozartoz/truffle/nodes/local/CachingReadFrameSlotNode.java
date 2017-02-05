package org.mozartoz.truffle.nodes.local;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.nodes.Node;

@ImportStatic(FrameUtil.class)
public abstract class CachingReadFrameSlotNode extends Node implements FrameSlotNode {

	@Child ReadFrameSlotNode readFrameSlotNode;

	public CachingReadFrameSlotNode(FrameSlot slot) {
		this.readFrameSlotNode = ReadFrameSlotNodeGen.create(slot);
	}

	public FrameSlot getSlot() {
		return readFrameSlotNode.getSlot();
	}

	public abstract Object executeRead(Frame frame);

	@Specialization(guards = "frame == cachedFrame", limit = "1")
	protected Object readConstant(Frame frame,
			@Cached("frame") Frame cachedFrame,
			@Cached("readFrameSlotNode.executeRead(cachedFrame)") Object cachedValue) {
		return cachedValue;
	}

	@Specialization(contains = "readConstant")
	protected Object readNotConstant(Frame frame) {
		return readFrameSlotNode.executeRead(frame);
	}

	@Override
	public String toString() {
		return readFrameSlotNode.toString();
	}

}
