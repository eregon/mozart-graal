package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.PropertyRegistry;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class TopLevelHandlerNode extends OzNode {

	private static final int MAX_STACKTRACE_ENTRIES = 8;

	private final BranchProfile errorProfile = BranchProfile.create();

	@Child OzNode body;

	public TopLevelHandlerNode(OzNode body) {
		this.body = body;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		try {
			return body.execute(frame);
		} catch (OzException ozException) {
			errorProfile.enter();
			handleOzException(ozException);
		} catch (Throwable exception) {
			errorProfile.enter();
			handleJavaException(exception);
		}
		Loader.getInstance().shutdown(1);
		return unit;
	}

	@TruffleBoundary
	private void handleOzException(OzException ozException) {
		Object errorHandler = PropertyRegistry.INSTANCE.get("errors.handler");
		if (errorHandler instanceof OzProc && ((OzProc) errorHandler).arity == 1) {
			((OzProc) errorHandler).rootCall("error handler", ozException.getData());
		} else {
			System.err.println(ozException.getMessage());
		}

		OzBacktrace backtrace = ozException.getBacktrace();
		if (backtrace != null) {
			backtrace.showUserBacktrace();
		}
	}

	@TruffleBoundary
	private void handleJavaException(Throwable exception) {
		System.err.println(exception.getClass().getName() + " " + exception.getMessage());
		StackTraceElement[] stackTrace = exception.getStackTrace();
		if (stackTrace.length > 0) {
			for (int i = 0; i < stackTrace.length && i < MAX_STACKTRACE_ENTRIES; i++) {
				System.err.println(stackTrace[i]);
			}
			if (stackTrace.length > MAX_STACKTRACE_ENTRIES) {
				System.err.println("...");
			}
		}
	}

}
