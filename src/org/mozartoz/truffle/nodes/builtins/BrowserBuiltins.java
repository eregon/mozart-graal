package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class BrowserBuiltins {

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsRecordCVarNode extends OzNode {

		@Specialization
		Object isRecordCVar(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ChunkWidthNode extends OzNode {

		@Specialization
		Object chunkWidth(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class ChunkArityNode extends OzNode {

		@Specialization
		Object chunkArity(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("longName")
	public static abstract class ShortNameNode extends OzNode {

		@Specialization
		Object shortName(Object longName) {
			return unimplemented();
		}

	}

	@Builtin(tryDeref = 1)
	@GenerateNodeFactory
	@NodeChild("variable")
	public static abstract class GetsBoundBNode extends OzNode {

		@Specialization
		OzVar getsBoundB(OzVar variable) {
			OzVar var = new OzVar();
			variable.link(var);
			return var;
		}

	}

	@GenerateNodeFactory
	@NodeChild("variable")
	public static abstract class VarSpaceNode extends OzNode {

		@Specialization
		Object varSpace(Object variable) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("procedure"), @NodeChild("file"), @NodeChild("line") })
	public static abstract class ProcLocNode extends OzNode {

		@Specialization
		Object procLoc(Object procedure, OzVar file, OzVar line) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("entity")
	public static abstract class AddrNode extends OzNode {

		@Specialization
		Object addr(Object entity) {
			return unimplemented();
		}

	}

}
