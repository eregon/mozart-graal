package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzBacktrace;
import org.mozartoz.truffle.runtime.OzException;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.PropertyRegistry;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.frame.VirtualFrame;

public class TopLevelHandlerNode extends OzNode {

	@Child OzNode body;

	public TopLevelHandlerNode(OzNode body) {
		this.body = body;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		try {
			return body.execute(frame);
		} catch (OzException ozException) {
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
			Loader.getInstance().shutdown(1);
		} catch (Exception exception) {
			System.err.println(exception.getClass().getName() + " " + exception.getMessage());
			StackTraceElement[] stackTrace = exception.getStackTrace();
			if (stackTrace.length > 0) {
				System.err.println(stackTrace[0]);
			}
			Loader.getInstance().shutdown(1);
		}
		return unit;
	}

}
