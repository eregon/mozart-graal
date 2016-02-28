package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.object.DynamicObject;

@SuppressWarnings("serial")
public class OzException extends RuntimeException {

	static final Arity ERROR_ARITY = Arity.build("error", 1L, "debug");
	static final Arity SYSTEM_ARITY = Arity.build("system", 1L, "debug");
	static final Arity FAILURE_ARITY = Arity.build("failure", 1L, "debug");

	public static DynamicObject newError(Object error) {
		return OzRecord.buildRecord(ERROR_ARITY, error, Unit.INSTANCE);
	}

	private final DynamicObject data;

	public OzException(OzNode currentNode, String message) {
		this(currentNode, newError(message.intern()));
	}

	public OzException(OzNode currentNode, DynamicObject data) {
		super(data.toString());
		OzBacktrace backtrace = OzBacktrace.capture(currentNode);

		boolean hasDebug = data.containsKey("debug");

		DynamicObject dataWithDebug;
		if (hasDebug && data.get("debug") == Unit.INSTANCE) {
			dataWithDebug = data.copy(data.getShape());
			dataWithDebug.getShape().getProperty("debug").setInternal(dataWithDebug, backtrace);
		} else {
			dataWithDebug = data;
		}

		this.data = dataWithDebug;
	}

	public DynamicObject getData() {
		return data;
	}

	public OzBacktrace getBacktrace() {
		Object debug = data.get("debug");
		return (OzBacktrace) debug;
	}

}
