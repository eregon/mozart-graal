package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.mozartoz.bootcompiler.SourceInterface;

public class SourceWrapper implements SourceInterface {

	private final Source source;

	public SourceWrapper(Source source) {
		this.source = source;
	}

	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public String getPath() {
		return source.getPath();
	}

	@Override
	public String getCode() {
		return source.getCharacters().toString();
	}

	@Override
	public SourceSection createSection(int charIndex, int length) {
		return source.createSection(charIndex, length);
	}
}
