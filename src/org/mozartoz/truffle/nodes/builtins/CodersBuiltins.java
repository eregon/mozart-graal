package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class CodersBuiltins {

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("string"), @NodeChild("encodingNode"), @NodeChild("variantNode") })
	public static abstract class EncodeNode extends OzNode {

		@Specialization
		Object encode(Object string, Object encodingNode, Object variantNode) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("encodingNode"), @NodeChild("variantNode") })
	public static abstract class DecodeNode extends OzNode {

		@Specialization
		Object decode(Object value, Object encodingNode, Object variantNode) {
			return unimplemented();
		}

	}

}
