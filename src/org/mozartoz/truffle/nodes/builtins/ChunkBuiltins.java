package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ChunkBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("underlying")
	public static abstract class NewChunkNode extends OzNode {

		@Specialization
		Object newChunk(Object underlying) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsChunkNode extends OzNode {

		@Specialization
		Object isChunk(Object value) {
			return unimplemented();
		}

	}

}
