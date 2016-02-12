package org.mozartoz.truffle.nodes.builtins;

import java.util.function.Consumer;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzError;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@NodeChild("value")
public abstract class RaiseErrorNode extends OzNode {

	@CreateCast("value")
	protected OzNode derefValue(OzNode value) {
		return DerefNodeGen.create(value);
	}

	@Specialization
	@TruffleBoundary
	protected Object raiseError(DynamicObject record) {
		String message = record.toString();
		System.err.println(message);
		showUserBacktrace(this);
		throw new OzError(message);
	}

	private static final Consumer<SourceSection> PRINT_SECTION = sourceSection -> {
		String desc;
		if (sourceSection == null) {
			desc = "<unknown>";
		} else {
			desc = sourceSection.getShortDescription();
		}
		System.err.println("from " + desc);
	};

	@TruffleBoundary
	public static void showUserBacktrace(Node currentNode) {
		if (currentNode != null) {
			PRINT_SECTION.accept(currentNode.getEncapsulatingSourceSection());
		}
		Truffle.getRuntime().iterateFrames(frame -> {
			PRINT_SECTION.accept(frame.getCallNode().getEncapsulatingSourceSection());
			return null;
		});
	}

}
