package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class OzArguments {
	private static final int PARENT_FRAME_INDEX = 0;
	private static final int ARGUMENTS_INDEX = 1;

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
}
