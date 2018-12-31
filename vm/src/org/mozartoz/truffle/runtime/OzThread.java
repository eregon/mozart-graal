package org.mozartoz.truffle.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.TruffleLanguage.Env;
import org.mozartoz.truffle.Options;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.coro.Coroutine;
import com.oracle.truffle.coro.CoroutineLocal;

public class OzThread extends OzValue implements Runnable {

	private static final CoroutineLocal<OzThread> CURRENT_OZ_THREAD = new CoroutineLocal<>();

	public static final Map<OzThread, OzBacktrace> BACKTRACES = Options.STACKTRACE_ON_INTERRUPT ? new ConcurrentHashMap<>() : null;

	private static long threadsCreated = 1L;
	private static long threadsRunnable = 1L;

	public static long getNumberOfThreadsRunnable() {
		return threadsRunnable;
	}

	public static long getNumberOfThreadsCreated() {
		return threadsCreated;
	}

	@TruffleBoundary
	public static OzThread getCurrent() {
		return CURRENT_OZ_THREAD.get();
	}

	private final Env env;
	private final Coroutine original;
	private final Coroutine coroutine;
	private OzProc proc;
	private final String procLocation;
	private final CallTarget initialCall;

	private String status = "runnable";
	private boolean raiseOnBlock = false;

	public OzThread() {
		env = null;
		original = null;
		coroutine = (Coroutine) Coroutine.current();
		proc = null;
		procLocation = null;
		initialCall = null;
		setCurrentOzThread(this);
	}

	public OzThread(OzProc proc, CallTarget initialCall, Env env) {
		this.env = env;
		this.proc = proc;
		this.procLocation = proc.toString();
		this.initialCall = initialCall;
		this.original = (Coroutine) Coroutine.current();
		this.coroutine = new Coroutine(this, 1024 * 1024); // 256 seems OK if we parse outside the coro
		threadsCreated++;
		Coroutine.yieldTo(coroutine);
	}

	public static void setCurrentOzThread(OzThread thread) {
		CURRENT_OZ_THREAD.set(thread);
	}

	public OzProc getAndClearInitialProc() {
		OzProc proc = this.proc;
		this.proc = null;
		return proc;
	}

	public String getInitialProcLocation() {
		return procLocation;
	}

	public String getStatus() {
		return status;
	}

	public boolean getRaiseOnBlock() {
		return raiseOnBlock;
	}

	public void setRaiseOnBlock(boolean raiseOnBlock) {
		this.raiseOnBlock = raiseOnBlock;
	}

	@Override
	public void run() {
		try {
			runThread();
		} catch (Error | RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void runThread() {
		assert original != null;
		Coroutine.yieldTo(original);

		final TruffleContext truffleContext = env.getContext();
		setCurrentOzThread(this);
		threadsRunnable++;
		final Object prev = truffleContext.enter();
		try {
			initialCall.call(OzArguments.pack(null, ArrayUtils.EMPTY));
		} finally {
			truffleContext.leave(prev);
			threadsRunnable--;
			status = "terminated";
			if (Options.STACKTRACE_ON_INTERRUPT) {
				BACKTRACES.remove(this);
			}
		}
	}

	@TruffleBoundary
	public void yield(Node currentNode) {
		status = "blocked";
		if (Options.STACKTRACE_ON_INTERRUPT) {
			BACKTRACES.put(this, OzBacktrace.capture(currentNode));
		}
		Coroutine.yield();
		status = "runnable";
	}

	public void suspend(Node currentNode) {
		threadsRunnable--;
		yield(currentNode);
		threadsRunnable++;
	}

}
