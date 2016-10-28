package org.mozartoz.truffle.runtime;

import java.util.Map.Entry;

public class StacktraceThread extends Thread {
	@Override
	public void run() {
		int byNeedFutures = 0;
		for (Entry<OzThread, OzBacktrace> entry : OzThread.BACKTRACES.entrySet()) {
			OzThread thread = entry.getKey();
			if (isByNeedFuture(thread)) {
				byNeedFutures++;
			} else {
				System.out.println(thread.getProc());
				entry.getValue().showUserBacktrace();
				System.out.println();
			}
		}
		System.out.println(byNeedFutures + " threads in ByNeedFuture");
	}

	private boolean isByNeedFuture(OzThread thread) {
		return thread.getProc() != null && thread.getProc().toString().contains("thread in ByNeedFuture in Base.oz");
	}
}
