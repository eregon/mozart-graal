package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

@SuppressWarnings("serial")
public final class OzException extends RuntimeException implements TruffleException {

	static final RecordFactory ERROR_ARITY = Arity.build("error", 1L, "debug").createFactory();
	static final RecordFactory SYSTEM_ARITY = Arity.build("system", 1L, "debug").createFactory();
	static final RecordFactory FAILURE_ARITY = Arity.build("failure", "debug").createFactory();

	public static DynamicObject newError(Object error) {
		return ERROR_ARITY.newRecord(error, Unit.INSTANCE);
	}

	public static DynamicObject newSystemError(Object error) {
		return SYSTEM_ARITY.newRecord(error, Unit.INSTANCE);
	}

	public static DynamicObject newFailure() {
		return FAILURE_ARITY.newRecord(Unit.INSTANCE);
	}

	private final Node currentNode;
	private final Object data;

	public OzException(Node currentNode, String message) {
		this(currentNode, newError(message.intern()));
	}

	public OzException(Node currentNode, Object data) {
		super();
		this.currentNode = currentNode;


		Object storedData = data;
		if (data instanceof DynamicObject) {
			DynamicObject dataRecord = (DynamicObject) data;
			boolean hasDebug = dataRecord.containsKey("debug");
			if (hasDebug && dataRecord.get("debug") == Unit.INSTANCE) {
				DynamicObject dataWithDebug = dataRecord.copy(dataRecord.getShape());
				OzBacktrace backtrace = OzBacktrace.capture(this);
				dataWithDebug.getShape().getProperty("debug").setInternal(dataWithDebug, backtrace);
				storedData = dataWithDebug;
			}
		}

		this.data = storedData;
	}

	@Override
	public Node getLocation() {
		return currentNode;
	}

	@Override
	public Object getExceptionObject() {
		return data;
	}

	@Override
	public String getMessage() {
		return data.toString();
	}

	public OzBacktrace getBacktrace() {
		if (data instanceof DynamicObject) {
			Object debug = ((DynamicObject) data).get("debug");
			return (OzBacktrace) debug;
		} else {
			return null;
		}
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return this;
	}

}
