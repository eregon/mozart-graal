package org.mozartoz.truffle.runtime;

import java.util.ArrayList;
import java.util.List;

import org.mozartoz.truffle.nodes.OzNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.Truffle;
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

	public static OzBacktrace capture(OzNode currentNode) {
		List<SourceSection> backtrace = new ArrayList<>();
		if (currentNode != null) {
			SourceSection sourceSection = currentNode.getEncapsulatingSourceSection();
			backtrace.add(sourceSection);
		}
		Truffle.getRuntime().iterateFrames(frame -> {
			SourceSection sourceSection = frame.getCallNode().getEncapsulatingSourceSection();
			backtrace.add(sourceSection);
			return null;
		});

		return new OzBacktrace(backtrace.toArray(new SourceSection[backtrace.size()]));
	}

}
