package org.mozartoz.truffle.runtime;

import java.util.Map.Entry;

public class StacktraceThread extends Thread {
	@Override
	public void run() {
		int waitNeeded = 0;
		for (Entry<OzThread, OzBacktrace> entry : OzThread.BACKTRACES.entrySet()) {
			final OzThread thread = entry.getKey();
			final OzBacktrace backtrace = entry.getValue();

			if (isWaitNeeded(backtrace)) {
				waitNeeded++;
			} else {
				System.out.println(thread.getProc());
				backtrace.showUserBacktrace();
				System.out.println();
			}
		}
		System.out.println(waitNeeded + " threads in WaitNeeded");
	}

	private boolean isWaitNeeded(OzBacktrace backtrace) {
		return backtrace.getFirst().equals("from Value.waitNeeded");
	}
}
