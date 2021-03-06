package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.coro.CoroutineExitException;
import org.mozartoz.truffle.runtime.ExitException;
import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.PropertyRegistry;

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
			OzBacktrace backtrace = new OzBacktrace(TruffleStackTrace.getStackTrace(ozException));
			handleOzException(ozException, backtrace);
			OzLanguage.getContext().exit(this, 1);
		} catch (CoroutineExitException | ExitException exitException) {
			errorProfile.enter();
			throw exitException;
		} catch (Throwable exception) {
			errorProfile.enter();
			handleJavaException(exception);
			OzLanguage.getContext().exit(this, 1);
		}

		return unit;
	}

	@TruffleBoundary
	private void handleOzException(OzException ozException, OzBacktrace backtrace) {
		Object errorHandler = PropertyRegistry.INSTANCE.get("errors.handler");
		if (errorHandler instanceof OzProc && ((OzProc) errorHandler).arity == 1) {
			((OzProc) errorHandler).rootCall("error handler", ozException.getExceptionObject());
		} else {
			System.err.println(ozException.getMessage());
		}

		backtrace.showUserBacktrace();
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
