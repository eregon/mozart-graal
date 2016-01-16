package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ConsLiteralNode extends OzNode {

	@Child OzNode head;
	@Child OzNode tail;

	public ConsLiteralNode(OzNode head, OzNode tail) {
		this.head = head;
		this.tail = tail;
	}

	public Object execute(VirtualFrame frame) {
		return new OzCons(head.execute(frame), tail.execute(frame));
	}

}
