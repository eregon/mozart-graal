package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzChunk;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ChunkBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("underlying")
	public static abstract class NewChunkNode extends OzNode {

		@CreateCast("underlying")
		protected OzNode derefUnderlying(OzNode value) {
			return DerefNodeGen.create(value);
		}

		@Specialization
		OzChunk newChunk(DynamicObject underlying) {
			return new OzChunk(underlying);
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
