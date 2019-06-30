package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

public class Errors {

	public static final RecordFactory OS_ERROR_FACTORY4 = Arity.build("os", 1L, 2L, 3L, 4L).createFactory();

	private static final RecordFactory KERNEL_ERROR_FACTORY2 = Arity.build("kernel", 1L, 2L).createFactory();
	private static final RecordFactory KERNEL_ERROR_FACTORY3 = Arity.build("kernel", 1L, 2L, 3L).createFactory();
	private static final RecordFactory KERNEL_ERROR_FACTORY4 = Arity.build("kernel", 1L, 2L, 3L, 4L).createFactory();
	private static final RecordFactory KERNEL_ERROR_FACTORY6 = Arity.build("kernel", 1L, 2L, 3L, 4L, 5L, 6L).createFactory();

	public static OzException OSError(Node node, String kind, Object arg1, Object arg2, Object arg3) {
		DynamicObject error = Errors.OS_ERROR_FACTORY4.newRecord("os", arg1, arg2, arg3);
		return new OzException(node, OzException.newSystemError(error));
	}

	public static OzException kernelError(Node node, String kind, Object arg) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY2.newRecord(kind, arg));
		return new OzException(node, data);
	}

	public static OzException kernelError(Node node, String kind, Object arg1, Object arg2) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY3.newRecord(kind, arg1, arg2));
		return new OzException(node, data);
	}

	public static OzException kernelError(Node node, String kind, Object arg1, Object arg2, Object arg3) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY4.newRecord(kind, arg1, arg2, arg3));
		return new OzException(node, data);
	}

	public static OzException kernelError(Node node, String kind, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		DynamicObject data = OzException.newError(KERNEL_ERROR_FACTORY6.newRecord(kind, arg1, arg2, arg3, arg4, arg5));
		return new OzException(node, data);
	}

	public static OzException noFieldError(Node node, Object record, Object feature) {
		return kernelError(node, ".", record, feature);
	}

}
