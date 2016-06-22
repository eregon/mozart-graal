package org.mozartoz.truffle.nodes.literal;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzGuards;
import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class EnsureOzLiteralNode extends OzNode {

	@Child OzNode child;

	public EnsureOzLiteralNode(OzNode child) {
		this.child = DerefNode.create(child);
	}

	@Override
	public Object execute(VirtualFrame frame) {
		Object value = child.execute(frame);
		assert OzGuards.isLiteral(value);
		return value;
	}

}
