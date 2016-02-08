package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ListLiteralNode extends OzNode {

	@Children final OzNode[] elements;

	public ListLiteralNode(OzNode... elements) {
		this.elements = elements;
	}

	public Object execute(VirtualFrame frame) {
		Object[] elementValues = new Object[elements.length];
		for (int i = 0; i < elements.length; i++) {
			elementValues[i] = elements[i].execute(frame);
		}

		Object list = "nil";

		for (int i = elementValues.length - 1; i >= 0; i--) {
			list = new OzCons(elementValues[i], list);
		}

		return list;
	}

}
