package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import com.oracle.truffle.api.library.CachedLibrary;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.mozartoz.truffle.runtime.RecordLibrary;

public abstract class LiteralBuiltins {

	@Builtin(name = "is", deref = ALL)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsLiteralNode extends OzNode {

		@Specialization(limit = "RECORDS_LIMIT")
		boolean isLiteral(Object value,
				@CachedLibrary("value") RecordLibrary records) {
			return records.isLiteral(value);
		}

	}

}
