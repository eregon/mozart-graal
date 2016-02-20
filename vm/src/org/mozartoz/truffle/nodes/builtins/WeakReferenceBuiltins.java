package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class WeakReferenceBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	@NodeChild("underlying")
	public static abstract class NewWeakReferenceNode extends OzNode {

		@Specialization
		Object newWeakReference(Object underlying) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsWeakReferenceNode extends OzNode {

		@Specialization
		Object isWeakReference(Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("weakRef")
	public static abstract class GetNode extends OzNode {

		@Specialization
		Object get(Object weakRef) {
			return unimplemented();
		}

	}

}
