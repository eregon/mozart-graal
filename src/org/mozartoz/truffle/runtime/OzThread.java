package org.mozartoz.truffle.runtime;

import com.oracle.truffle.coro.Coroutine;
import com.oracle.truffle.coro.CoroutineLocal;

public class OzThread implements Runnable {

	private static final CoroutineLocal<OzThread> CURRENT_OZ_THREAD = new CoroutineLocal<>();

	public static final OzThread MAIN_THREAD = new OzThread();

	public static OzThread getCurrent() {
		return CURRENT_OZ_THREAD.get();
	}

	private final Coroutine coroutine;
	private final OzProc proc;

	private OzThread() {
		coroutine = (Coroutine) Coroutine.current();
		proc = null;
		setInitialOzThread();
	}

	private void setInitialOzThread() {
		CURRENT_OZ_THREAD.set(this);
	}

	public OzThread(OzProc proc) {
		this.proc = proc;
		this.coroutine = new Coroutine(this, 1024 * 1024); // 256 seems OK if we parse outside the coro
	}

	public Coroutine getCoroutine() {
		return coroutine;
	}

	@Override
	public void run() {
		setInitialOzThread();
		proc.callTarget.call(OzArguments.pack(proc.declarationFrame, new Object[0]));
	}

}
