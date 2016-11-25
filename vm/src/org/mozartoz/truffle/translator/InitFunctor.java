package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.RootCallTarget;

public abstract class InitFunctor {

	public static RootCallTarget apply(Object initFunctor) {
		Object imports = OzRecord.buildRecord(
				Arity.build("import", "Boot"),
				BuiltinsManager.getBootModule("Boot_Boot"));

		OzVar result = new OzVar();
		OzNode apply = CallNode.create(
				DotNodeFactory.create(new LiteralNode(initFunctor), new LiteralNode("apply")),
				new LiteralNode(new Object[] { imports, result }));

		OzNode node = SequenceNode.sequence(apply, DerefNode.create(new LiteralNode(result)));
		return OzRootNode.createTopRootNode("Init.apply", node).toCallTarget();
	}

}
