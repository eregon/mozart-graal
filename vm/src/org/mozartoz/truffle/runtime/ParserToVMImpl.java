package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.source.SourceSection;
import org.mozartoz.bootcompiler.ParserToVM;
import org.mozartoz.bootcompiler.SourceInterface;
import org.mozartoz.truffle.translator.Loader;

public class ParserToVMImpl implements ParserToVM {

	public static final ParserToVM INSTANCE = new ParserToVMImpl();

	private ParserToVMImpl() {
	}

	@Override
	public SourceInterface createSource(String path) {
		return new SourceWrapper(Loader.createSource(OzContext.getInstance().getEnv(), path));
	}

	@Override
	public String sectionFileLine(SourceSection section) {
		return section.getSource().getPath() + ":" + section.getStartLine();
	}

	@Override
	public SourceSection extendSection(SourceSection leftObject, SourceSection rightObject) {
		SourceSection left = (SourceSection) leftObject;
		SourceSection right = (SourceSection) rightObject;
		if (left.getSource() == right.getSource()) {
			int len = right.getCharEndIndex() - left.getCharIndex();
			return left.getSource().createSection(left.getCharIndex(), len);
		} else {
			return left;
		}
	}

}
