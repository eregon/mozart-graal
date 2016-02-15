package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("cons")
public abstract class HeadNode extends OzNode {

	@CreateCast("cons")
	protected OzNode derefCons(OzNode cons) {
		return DerefNodeGen.create(cons);
	}

	@Specialization
	protected Object head(OzCons cons) {
		return cons.getHead();
	}

}
