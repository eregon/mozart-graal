package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class CompactStringBuiltins {

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsCompactStringNode extends OzNode {

		@Specialization
		Object isCompactString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsCompactByteStringNode extends OzNode {

		@Specialization
		Object isCompactByteString(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("index") })
	public static abstract class CharAtNode extends OzNode {

		@Specialization
		Object charAt(Object value, Object index) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("left"), @NodeChild("right") })
	public static abstract class AppendNode extends OzNode {

		@Specialization
		Object append(Object left, Object right) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("from"), @NodeChild("to") })
	public static abstract class SliceNode extends OzNode {

		@Specialization
		Object slice(Object value, Object from, Object to) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("from"), @NodeChild("needle"), @NodeChild("begin") })
	public static abstract class SearchNode extends OzNode {

		@Specialization
		Object search(Object value, Object from, Object needle, OzVar begin) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("string"), @NodeChild("prefix") })
	public static abstract class HasPrefixNode extends OzNode {

		@Specialization
		Object hasPrefix(Object string, Object prefix) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("string"), @NodeChild("suffix") })
	public static abstract class HasSuffixNode extends OzNode {

		@Specialization
		Object hasSuffix(Object string, Object suffix) {
			return unimplemented();
		}

	}

}
