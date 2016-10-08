package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Variable;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
public abstract class BindVarValueNode extends OzNode {

	public static BindVarValueNode create() {
		return BindVarValueNodeGen.create(null, null);
	}

	public abstract Object executeBindVarValue(OzVar var, Object value);

	@Specialization(guards = "var.getNext() == var")
	Object bind1(Variable var, Object value) {
		var.setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext() == var" })
	Object bind2(Variable var, Object value) {
		var.setInternalValue(value, var);
		var.getNext().setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext().getNext() == var" })
	Object bind3(Variable var, Object value) {
		var.setInternalValue(value, var);
		Variable next = var.getNext();
		next.setInternalValue(value, var);
		next.getNext().setInternalValue(value, var);
		return value;
	}

	@Specialization(contains = { "bind1", "bind2", "bind3" })
	Object bindLeft(OzVar var, Object value) {
		var.bind(value);
		return value;
	}

}
