package org.mozartoz.truffle.runtime;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

public class OzBacktrace {

	private SourceSection[] backtrace;

	public OzBacktrace(SourceSection[] backtrace) {
		this.backtrace = backtrace;
	}

	public void showUserBacktrace() {
		CompilerAsserts.neverPartOfCompilation();
		for (SourceSection sourceSection : backtrace) {
			final String desc;
			if (sourceSection == null) {
				desc = "<unknown>";
			} else {
				desc = sourceSection.getShortDescription();
			}
			System.err.println("from " + desc);

		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SourceSection sourceSection : backtrace) {
			final String desc;
			if (sourceSection == null) {
				desc = "<unknown>";
			} else {
				desc = sourceSection.getShortDescription();
			}
			builder.append("from " + desc).append('\n');
		}
		return builder.toString();
	}

	public static OzBacktrace capture(Node currentNode) {
		List<SourceSection> backtrace = new ArrayList<>();
		if (currentNode != null) {
			SourceSection sourceSection = currentNode.getEncapsulatingSourceSection();
			backtrace.add(sourceSection);
		}
		Truffle.getRuntime().iterateFrames(frame -> {
			if (frame.getCallNode() != null) {
				SourceSection sourceSection = frame.getCallNode().getEncapsulatingSourceSection();
				backtrace.add(sourceSection);
			}
			return null;
		});

		return new OzBacktrace(backtrace.toArray(new SourceSection[backtrace.size()]));
	}

}
