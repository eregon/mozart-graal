package org.mozartoz.truffle.nodes.list;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@NodeChild("list")
public abstract class OzListLengthNode extends OzNode {

	@CreateCast("list")
	protected OzNode derefList(OzNode var) {
		return DerefNode.create(var);
	}

	public abstract int executeLength(Object list);

	@Child DerefNode derefConsNode = DerefNode.create();

	@Specialization(guards = "isNil(nil)")
	protected int emptyList(String nil) {
		return 0;
	}

	@Specialization(guards = "lengthCheck(list, cachedLength)", limit = "1")
	protected int fixedLengthList(OzCons list,
			@Cached("list.length(derefConsNode)") int cachedLength) {
		return cachedLength;
	}

	@Specialization(contains = "fixedLengthList")
	protected int uncachedLength(OzCons list) {
		return list.length(derefConsNode);
	}

	@ExplodeLoop
	protected boolean lengthCheck(OzCons list, int expectedLength) {
		Object current = list;
		for (int i = 0; i < expectedLength; i++) {
			if (!(current instanceof OzCons)) {
				CompilerDirectives.transferToInterpreterAndInvalidate();
				return false;
			}
			OzCons cons = (OzCons) current;
			current = derefConsNode.executeDeref(cons.getTail());
		}
		return current == "nil";
	}

}
