package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzUniqueName;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class NameBuiltins {

	@Builtin(name = "new")
	@GenerateNodeFactory
	public static abstract class NewNameNode extends OzNode {

		@Specialization
		OzName newName() {
			return new OzName();
		}

	}

	@GenerateNodeFactory
	@NodeChild("uuid")
	public static abstract class NewWithUUIDNode extends OzNode {

		@Specialization
		Object newWithUUID(Object uuid) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("atom")
	public static abstract class NewUniqueNode extends OzNode {

		@Specialization
		OzUniqueName newUnique(String atom) {
			return OzUniqueName.get(atom);
		}

	}

	@GenerateNodeFactory
	@NodeChild("printName")
	public static abstract class NewNamedNode extends OzNode {

		@Specialization
		Object newNamed(Object printName) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("printName"), @NodeChild("uuid") })
	public static abstract class NewNamedWithUUIDNode extends OzNode {

		@Specialization
		Object newNamedWithUUID(Object printName, Object uuid) {
			return unimplemented();
		}

	}

	@Builtin(name = "is")
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class IsNameNode extends OzNode {

		@Specialization
		Object isName(Object value) {
			return unimplemented();
		}

	}

}
