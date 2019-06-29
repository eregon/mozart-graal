package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
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

	private static Object getDebugValue(Object data) {
		if (data instanceof DynamicObject && ((DynamicObject) data).containsKey("debug")) {
			return ((DynamicObject) data).get("debug");
		} else {
			return null;
		}
	}

	public static OzException getExceptionFromObject(Object data) {
		Object debug = getDebugValue(data);
		if (debug instanceof OzException) {
			return (OzException) debug;
		} else {
			return null;
		}
	}

	private final Node currentNode;
	private final Object data;

	public OzException(Node currentNode, String message) {
		this(currentNode, newError(message.intern()));
	}

	public OzException(Node currentNode, Object data) {
		super();
		assert currentNode != null;
		this.currentNode = currentNode;

		Object storedData = data;
		Object debug = getDebugValue(data);
		if (debug == Unit.INSTANCE) {
			DynamicObject dataRecord = (DynamicObject) data;
			DynamicObject dataWithDebug = dataRecord.copy(dataRecord.getShape());
			dataWithDebug.getShape().getProperty("debug").setInternal(dataWithDebug, this);
			storedData = dataWithDebug;
		} else {
			assert getExceptionFromObject(data) == null || getExceptionFromObject(data) == this;
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

	public OzBacktrace getBacktrace() {
		return new OzBacktrace(TruffleStackTrace.getStackTrace(this));
	}

	@SuppressWarnings("sync-override")
	@Override
	public final Throwable fillInStackTrace() {
		return this;
	}

	@Override
	public String toString() {
		return "<OzException>";
	}
}
