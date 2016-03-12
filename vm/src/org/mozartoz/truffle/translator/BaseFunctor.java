package org.mozartoz.truffle.translator;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.runtime.OzVar;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.SourceSection;

public abstract class BaseFunctor {

	public static OzRootNode apply(Object baseFunctor) {
		Object imports = BuiltinsManager.getBootModulesRecord();

		OzVar result = new OzVar();
		OzNode apply = CallProcNodeGen.create(
				new OzNode[] { new LiteralNode(imports), new LiteralNode(result) },
				DotNodeFactory.create(new LiteralNode(baseFunctor), new LiteralNode("apply")));

		OzNode node = SequenceNode.sequence(apply, DerefNodeGen.create(new LiteralNode(result)));
		SourceSection sourceSection = SourceSection.createUnavailable("main", "Base.apply");
		return new OzRootNode(sourceSection, new FrameDescriptor(), node, 0);
	}

}
