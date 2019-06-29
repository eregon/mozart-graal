package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.nodes.Node;

public final class OzFailedValue extends OzValue {

	private final Object data;

	public OzFailedValue(Object data) {
		this.data = data;
	}

	public OzException getException(Node currentNode) {
		OzException exception = OzException.getExceptionFromObject(data);
		if (exception != null) {
			return exception;
		} else {
			// No backtrace attached
			return new OzException(currentNode, data);
		}
	}

	@Override
	public String toString() {
		return "<Failed " + data + ">";
	}

}
