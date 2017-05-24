package org.mozartoz.truffle.nodes.local;

import java.lang.ref.WeakReference;

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

	@Specialization(guards = "frame == getFrame(cachedFrame)", limit = "1")
	protected Object readConstant(Frame frame,
			@Cached("newWeakFrame(frame)") WeakReference<Frame> cachedFrame,
			@Cached("newWeakRef(readFrameSlotNode.executeRead(frame))") WeakReference<Object> cachedValue) {
		final Object value = cachedValue.get();
		assert value != null; // If one can read a variable, then it has a strong ref to it via parent frame(s) + slots
		return value;
	}

	@Specialization(replaces = "readConstant")
	protected Object readNotConstant(Frame frame) {
		return readFrameSlotNode.executeRead(frame);
	}

	@Override
	public String toString() {
		return readFrameSlotNode.toString();
	}

	protected static WeakReference<Object> newWeakRef(Object value) {
		return new WeakReference<>(value);
	}

	protected static WeakReference<Frame> newWeakFrame(Frame frame) {
		return new WeakReference<>(frame);
	}

	protected static Frame getFrame(WeakReference<Frame> weakRef) {
		return weakRef.get();
	}

}
