package org.mozartoz.truffle.nodes;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class ShowNode extends OzNode {

	@Specialization
	protected Object show(long value) {
		System.out.println(value);
		return "";
	}

}
