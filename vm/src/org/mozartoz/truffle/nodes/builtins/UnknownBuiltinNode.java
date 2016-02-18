package org.mozartoz.truffle.nodes.builtins;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class UnknownBuiltinNode extends OzNode {

	String builtinName;

	public UnknownBuiltinNode(String builtinName) {
		this.builtinName = builtinName;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		throw new RuntimeException("Unimplemented builtin " + builtinName);
	}

}
