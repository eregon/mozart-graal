package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ThreadBuiltins {

	@GenerateNodeFactory
	@NodeChild("target")
	public static abstract class CreateNode extends OzNode {

		@Specialization
		protected Object create(Object target) {
			throw new UnsupportedOperationException();
		}

	}

}
