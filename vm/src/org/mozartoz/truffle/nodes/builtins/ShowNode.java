package org.mozartoz.truffle.nodes.builtins;

import java.math.BigInteger;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class ShowNode extends OzNode {

	@Specialization
	protected Object show(long value) {
		System.out.println(value);
		return unit;
	}

	@Specialization
	protected Object show(BigInteger value) {
		System.out.println(value);
		return unit;
	}

	@Specialization
	protected Object show(OzCons list) {
		System.out.println(list);
		return unit;
	}

	@Specialization
	protected Object show(OzVar var) {
		System.out.println(var);
		return unit;
	}

	@Specialization(guards = "undefined == null")
	protected Object show(Object undefined) {
		System.out.println("null");
		return unit;
	}

}
