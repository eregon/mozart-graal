package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.runtime.OzVar;

public abstract class BaseFunctor {

	public static OzRootNode apply(Object baseFunctor) {
		Object imports = BuiltinsManager.getBootModulesRecord();

		OzVar result = new OzVar();
		OzNode apply = CallNode.create(
				DotNodeFactory.create(new LiteralNode(baseFunctor), new LiteralNode("apply")),
				new LiteralNode(new Object[] { imports, result }));

		OzNode node = SequenceNode.sequence(apply, DerefNode.create(new LiteralNode(result)));
		return OzRootNode.createTopRootNode("Base.apply", node);
	}

}
