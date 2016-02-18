package org.mozartoz.truffle.nodes.builtins;

import java.util.function.Consumer;
import java.util.function.Function;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseErrorNodeFactory;
import org.mozartoz.truffle.runtime.OzError;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public abstract class ExceptionBuiltins {

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseErrorNode extends OzNode {

		public abstract Object executeRaiseError(Object value);

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

		private static final Function<SourceSection, String> SHOW_SECTION = sourceSection -> {
			String desc;
			if (sourceSection == null) {
				desc = "<unknown>";
			} else {
				desc = sourceSection.getShortDescription();
			}
			return "from " + desc + "\n";
		};

		@TruffleBoundary
		public static String getUserBacktrace(Node currentNode) {
			StringBuilder builder = new StringBuilder();
			if (currentNode != null) {
				builder.append(SHOW_SECTION.apply(currentNode.getEncapsulatingSourceSection()));
			}
			Truffle.getRuntime().iterateFrames(frame -> {
				builder.append(SHOW_SECTION.apply(frame.getCallNode().getEncapsulatingSourceSection()));
				return null;
			});
			return builder.toString() + "<END>";
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("value")
	public static abstract class RaiseNode extends OzNode {

		@Child RaiseErrorNode raiseErrorNode = RaiseErrorNodeFactory.create(null);

		@Specialization
		Object raise(Object value) {
			// TODO: should wrap in error(Value)
			return raiseErrorNode.executeRaiseError(value);
		}

	}

}
