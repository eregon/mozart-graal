package org.mozartoz.truffle.runtime;

import java.io.IOException;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.instrument.WrapperNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

public class OzLanguage extends TruffleLanguage<Object> {

	@Override
	protected Object createContext(com.oracle.truffle.api.TruffleLanguage.Env env) {
		return null;
	}

	@Override
	protected CallTarget parse(Source code, Node context, String... argumentNames) throws IOException {
		return null;
	}

	@Override
	protected Object findExportedSymbol(Object context, String globalName, boolean onlyExplicit) {
		return null;
	}

	@Override
	protected Object getLanguageGlobal(Object context) {
		return null;
	}

	@Override
	protected boolean isObjectOfLanguage(Object object) {
		return false;
	}

	@Override
	protected Visualizer getVisualizer() {
		return null;
	}

	@Override
	protected boolean isInstrumentable(Node node) {
		return false;
	}

	@Override
	protected WrapperNode createWrapperNode(Node node) {
		return null;
	}

	@Override
	protected Object evalInContext(Source source, Node node, MaterializedFrame mFrame) throws IOException {
		return null;
	}

}
