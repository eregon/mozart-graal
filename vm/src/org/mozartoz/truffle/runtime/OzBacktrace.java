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
			System.out.println(formatSection(sourceSection));

		}
	}

	public String formatSection(SourceSection section) {
		final String desc;
		if (section == null) {
			desc = "<unknown>";
		} else if (section.getSource() == null) {
			desc = section.getShortDescription();
		} else {
			if (!section.getIdentifier().isEmpty()) {
				desc = String.format("%s in %s:%d", section.getIdentifier(), section.getSource().getName(), section.getStartLine());
			} else {
				desc = String.format("%s:%d", section.getSource().getName(), section.getStartLine());
			}
		}
		return "from " + desc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SourceSection sourceSection : backtrace) {
			builder.append(formatSection(sourceSection));
			builder.append('\n');
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
