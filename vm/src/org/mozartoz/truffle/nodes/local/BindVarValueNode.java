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
		print_nlinks(var);
		var.setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext() == var" })
	Object bind2(Variable var, Object value) {
		print_nlinks(var);
		Variable next = var.getNext();
		var.setInternalValue(value, var);
		next.setInternalValue(value, var);
		return value;
	}

	@Specialization(guards = { "var.getNext() != var", "var.getNext().getNext().getNext() == var" })
	Object bind3(Variable var, Object value) {
		print_nlinks(var);
		Variable next1 = var.getNext(), next2 = next1.getNext();
		var.setInternalValue(value, var);
		next1.setInternalValue(value, var);
		next2.setInternalValue(value, var);
		return value;
	}

	@Specialization(contains = { "bind1", "bind2", "bind3" })
	Object bindLeft(OzVar var, Object value) {
		print_nlinks(var);
		var.bind(value);
		return value;
	}

	public static void print_nlinks(Variable var) {
		if (Options.PRINT_NLINKS != null) {
			int count = 1;
			Variable current = var.getNext();
			while (current != var) {
				count += 1;
				current = current.getNext();
			}
			System.out.println(Options.PRINT_NLINKS + " --- " + count);
		}
	}

}
