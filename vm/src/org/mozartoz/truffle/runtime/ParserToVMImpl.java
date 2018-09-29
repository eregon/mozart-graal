package org.mozartoz.truffle.runtime;

import org.mozartoz.bootcompiler.ParserToVM;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.source.Source;

public class ParserToVMImpl implements ParserToVM {

	public static final ParserToVM INSTANCE = new ParserToVMImpl();

	private ParserToVMImpl() {
	}

	@Override
	public Source createSource(String path) {
		return Loader.createSource(OzContext.getInstance().getEnv(), path);
	}

}
