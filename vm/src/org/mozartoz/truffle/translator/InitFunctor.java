package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.TopLevelHandlerNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.call.CallNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.FrameDescriptor;

public abstract class InitFunctor {

	public static OzRootNode apply(Object initFunctor) {
		Object imports = OzRecord.buildRecord(
				Arity.build("import", "Boot"),
				BuiltinsManager.getBootModule("Boot_Boot"));

		OzNode node = CallNode.create(
				DotNodeFactory.create(new LiteralNode(initFunctor), new LiteralNode("apply")),
				new LiteralNode(new Object[] { imports, new OzVar() }));

		return new OzRootNode(Loader.MAIN_SOURCE_SECTION, "Init.apply", new FrameDescriptor(), new TopLevelHandlerNode(node), 0);
	}

}
