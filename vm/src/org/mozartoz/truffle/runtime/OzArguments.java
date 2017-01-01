package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class OzArguments {
	private static final int PARENT_FRAME_INDEX = 0;
	private static final int ARGUMENTS_INDEX = 1;
	public static final int IMPLICIT_ARGUMENTS = ARGUMENTS_INDEX;

	public static Object[] pack(MaterializedFrame parentFrame, Object[] arguments) {
		Object[] frameArguments = new Object[ARGUMENTS_INDEX + arguments.length];
		frameArguments[PARENT_FRAME_INDEX] = parentFrame;
		System.arraycopy(arguments, 0, frameArguments, ARGUMENTS_INDEX, arguments.length);
		return frameArguments;
	}

	public static MaterializedFrame getParentFrame(Frame frame) {
		return (MaterializedFrame) frame.getArguments()[PARENT_FRAME_INDEX];
	}

	public static Object getArgument(Frame frame, int index) {
		return frame.getArguments()[ARGUMENTS_INDEX + index];
	}

	public static void setArgument(Frame frame, int index, Object value) {
		frame.getArguments()[ARGUMENTS_INDEX + index] = value;
	}

	public static int getArgumentCount(Frame frame) {
		return frame.getArguments().length - ARGUMENTS_INDEX;
	}

	@ExplodeLoop
	public static Frame getParentFrame(VirtualFrame topFrame, int depth) {
		Frame frame = topFrame;
		for (int i = 0; i < depth; i++) {
			frame = OzArguments.getParentFrame(frame);
		}
		return frame;
	}

}
