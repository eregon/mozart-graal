package org.mozartoz.truffle.runtime;

import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

public class OzBacktrace {

	private final List<TruffleStackTraceElement> stackTrace;

	public OzBacktrace(List<TruffleStackTraceElement> stackTrace) {
		this.stackTrace = stackTrace;
	}

	@TruffleBoundary
	public void showUserBacktrace() {
		for (TruffleStackTraceElement element : stackTrace) {
			System.out.println(formatFrom(element));

		}
	}

	public String getFirst() {
		if (stackTrace.isEmpty()) {
			return "";
		}
		return formatFrom(stackTrace.get(0));
	}

	private static String formatFrom(TruffleStackTraceElement element) {
		return "from " + formatNode(element.getLocation());
	}

	@TruffleBoundary
	public static String formatNode(Node node) {
		if (node == null) {
			return "<null node>";
		}

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
		for (TruffleStackTraceElement element : stackTrace) {
			builder.append(formatFrom(element));
			builder.append('\n');
		}
		return builder.toString();
	}

}
