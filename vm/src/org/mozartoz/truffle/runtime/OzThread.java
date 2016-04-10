package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.TopLevelHandlerNode;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.literal.LiteralNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.coro.Coroutine;
import com.oracle.truffle.coro.CoroutineLocal;

public class OzThread implements Runnable {

	private static final CoroutineLocal<OzThread> CURRENT_OZ_THREAD = new CoroutineLocal<>();

	public static final OzThread MAIN_THREAD = new OzThread();

	public static OzThread getCurrent() {
		return CURRENT_OZ_THREAD.get();
	}

	private final Coroutine coroutine;
	private final CallTarget target;

	private OzThread() {
		coroutine = (Coroutine) Coroutine.current();
		target = null;
		setInitialOzThread();
	}

	private void setInitialOzThread() {
		CURRENT_OZ_THREAD.set(this);
	}

	public OzThread(OzProc proc) {
		this.target = wrap(proc);
		this.coroutine = new Coroutine(this, 1024 * 1024); // 256 seems OK if we parse outside the coro
	}

	public Coroutine getCoroutine() {
		return coroutine;
	}

	@Override
	public void run() {
		setInitialOzThread();
		Object[] arguments = OzArguments.pack(null, new Object[0]);
		target.call(arguments);
	}

	private static CallTarget wrap(OzProc proc) {
		OzNode callNode = CallProcNodeGen.create(new OzNode[] {}, new LiteralNode(proc));
		SourceSection sourceSection = SourceSection.createUnavailable("main", "Thread.create");
		FrameDescriptor frameDescriptor = new FrameDescriptor();
		TopLevelHandlerNode topLevelHandler = new TopLevelHandlerNode(callNode);
		OzRootNode rootNode = new OzRootNode(sourceSection, frameDescriptor, topLevelHandler, 0);
		return Truffle.getRuntime().createCallTarget(rootNode);
	}

}
