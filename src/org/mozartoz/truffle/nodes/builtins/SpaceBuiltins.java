package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class SpaceBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("target")
	public static abstract class NewSpaceNode extends OzNode {

		@Specialization
		Object newSpace(Object target) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsSpaceNode extends OzNode {

		@Specialization
		Object isSpace(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("space")
	public static abstract class AskNode extends OzNode {

		@Specialization
		Object ask(Object space) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("space")
	public static abstract class AskVerboseNode extends OzNode {

		@Specialization
		Object askVerbose(Object space) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("space")
	public static abstract class MergeNode extends OzNode {

		@Specialization
		Object merge(Object space) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("space")
	public static abstract class CloneNode extends OzNode {

		@Specialization
		Object clone(Object space) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("space"), @NodeChild("value") })
	public static abstract class CommitNode extends OzNode {

		@Specialization
		Object commit(Object space, Object value) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("space")
	public static abstract class KillNode extends OzNode {

		@Specialization
		Object kill(Object space) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("alts")
	public static abstract class ChooseNode extends OzNode {

		@Specialization
		Object choose(Object alts) {
			return unimplemented();
		}

	}

}
