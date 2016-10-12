package org.mozartoz.truffle.runtime;

import java.util.Map.Entry;

public class StacktraceThread extends Thread {
	@Override
	public void run() {
		for (Entry<OzThread, OzBacktrace> entry : OzThread.BACKTRACES.entrySet()) {
			System.out.println(entry.getKey().getProc());
			entry.getValue().showUserBacktrace();
			System.out.println();
		}
	}
}
