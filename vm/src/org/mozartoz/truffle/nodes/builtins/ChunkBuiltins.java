package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzChunk;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ChunkBuiltins {

	@Builtin(name = "new", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("underlying")
	public static abstract class NewChunkNode extends OzNode {

		@Specialization
		OzChunk newChunk(DynamicObject underlying) {
			return new OzChunk(underlying);
		}

	}

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsChunkNode extends OzNode {

		@Specialization
		boolean isChunk(OzChunk value) {
			return true;
		}

	}

}
