package org.mozartoz.truffle.nodes.list;

import java.util.ArrayList;
import java.util.List;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;

@NodeChild("list")
public abstract class OzListToObjectArrayNode extends OzNode {

	@CreateCast("list")
	protected OzNode derefList(OzNode var) {
		return DerefNode.create(var);
	}

	public abstract Object[] executeToObjectArray(Object list);

	@Child OzListLengthNode listLengthNode = OzListLengthNodeGen.create(null);
	@Child DerefNode derefConsNode = DerefNode.create();

	@Specialization(guards = "isNil(nil)")
	protected Object[] emptyList(String nil) {
		return new Object[0];
	}

	@ExplodeLoop
	@Specialization(guards = "listLengthNode.executeLength(list) == cachedLength", limit = "5")
	protected Object[] fixedLengthList(OzCons list,
			@Cached("listLengthNode.executeLength(list)") int cachedLength) {
		Object[] array = new Object[cachedLength];
		Object current = list;
		for (int i = 0; i < cachedLength; i++) {
			OzCons cons = (OzCons) current;
			array[i] = cons.getHead();
			current = derefConsNode.executeDeref(cons.getTail());
		}
		return array;
	}

	@Specialization(contains = "fixedLengthList")
	protected Object[] uncachedWithArrayList(OzCons list) {
		List<Object> array = new ArrayList<>();
		list.forEach(derefConsNode, e -> array.add(e));
		return array.toArray(new Object[array.size()]);
	}

}
