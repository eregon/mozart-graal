package org.mozartoz.truffle.nodes.local;

import org.mozartoz.truffle.Options;
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
		printNLinks(var);
		var.setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext() == var" })
	Object bind2(Variable var, Object value) {
		printNLinks(var);
		Variable next = var.getNext();
		var.setInternalValue(value, var);
		next.setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext().getNext() == var" })
	Object bind3(Variable var, Object value) {
		printNLinks(var);
		Variable next1 = var.getNext();
		var.setInternalValue(value, var);
		Variable next2 = next1.getNext();
		next1.setInternalValue(value, var);
		next2.setInternalValue(value, var);
		return value;
	}

	@Specialization(contains = { "bind1", "bind2", "bind3" })
	Object bindLeft(OzVar var, Object value) {
		printNLinks(var);
		var.bind(value);
		return value;
	}

	public static void printNLinks(Variable var) {
		if (Options.PRINT_NLINKS) {
			System.out.println("nlinks --- " + var.countLinks());
		}
	}

}
