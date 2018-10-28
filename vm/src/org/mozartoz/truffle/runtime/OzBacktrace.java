package org.mozartoz.truffle.runtime;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

public class OzBacktrace {

	private Node[] backtrace;

	private OzBacktrace(Node[] backtrace) {
		this.backtrace = backtrace;
	}

	@TruffleBoundary
	public void showUserBacktrace() {
		for (Node node : backtrace) {
			System.out.println(formatFrom(node));

		}
	}

	public String getFirst() {
		if (backtrace.length == 0) {
			return "";
		}
		return formatFrom(backtrace[0]);
	}

	private static String formatFrom(Node node) {
		return "from " + formatNode(node);
	}

	@TruffleBoundary
	public static String formatNode(Node node) {
		SourceSection section = node.getEncapsulatingSourceSection();
		String identifier = node.getRootNode().getName();
		if (section == null) {
			return "<unknown>";
		} else if (!section.isAvailable()) {
			return identifier;
		} else {
			if (!identifier.isEmpty()) {
				return String.format("%s in %s:%d", identifier, section.getSource().getName(), section.getStartLine());
			} else {
				return String.format("%s:%d", section.getSource().getName(), section.getStartLine());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Node node : backtrace) {
			builder.append(formatFrom(node));
			builder.append('\n');
		}
		return builder.toString();
	}

	@TruffleBoundary
	public static OzBacktrace capture(Node currentNode) {
		List<Node> backtrace = new ArrayList<>();
		if (currentNode != null) {
			backtrace.add(currentNode);
		}
		Truffle.getRuntime().iterateFrames(frame -> {
			if (frame.getCallNode() != null) {
				backtrace.add(frame.getCallNode());
			}
			return null;
		});

		return new OzBacktrace(backtrace.toArray(new Node[backtrace.size()]));
	}

}
