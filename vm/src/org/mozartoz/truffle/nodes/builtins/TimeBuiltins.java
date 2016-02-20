package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class TimeBuiltins {

	@GenerateNodeFactory
	@NodeChild("delay")
	public static abstract class AlarmNode extends OzNode {

		@Specialization
		Object alarm(Object delay) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class GetReferenceTimeNode extends OzNode {

		@Specialization
		Object getReferenceTime() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class GetMonotonicTimeNode extends OzNode {

		@Specialization
		Object getMonotonicTime() {
			return unimplemented();
		}

	}

}
