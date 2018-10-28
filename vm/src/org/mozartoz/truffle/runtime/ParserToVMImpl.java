package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.source.SourceSection;
import org.mozartoz.bootcompiler.ParserToVM;
import org.mozartoz.bootcompiler.SourceInterface;
import org.mozartoz.bootcompiler.ast.Node.Pos;
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
	public String sectionFileLine(Pos pos) {
		final SourceSection sourceSection = ((SourceWrapper) pos.source()).source.createSection(pos.charIndex(), pos.length());
		return sourceSection.getSource().getPath() + ":" + sourceSection.getStartLine();
	}

}
