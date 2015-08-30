package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class OzArguments {
	private static final int PARENT_FRAME_INDEX = 0;
	private static final int ARGUMENT_INDEX = 1;

	public static Object[] pack(MaterializedFrame parentFrame, Object argument) {
		return new Object[] { parentFrame, argument };
	}

	public static MaterializedFrame getParentFrame(Frame frame) {
		return (MaterializedFrame) frame.getArguments()[PARENT_FRAME_INDEX];
	}

	public static Object getArgument(Frame frame) {
		return frame.getArguments()[ARGUMENT_INDEX];
	}
}
