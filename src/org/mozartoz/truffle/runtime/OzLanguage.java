package org.mozartoz.truffle.runtime;

import java.io.IOException;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.translator.Translator;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Registration;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.Visualizer;
import com.oracle.truffle.api.instrument.WrapperNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

@Registration(name = "Oz", version = "0.1", mimeType = OzLanguage.MIME_TYPE)
public class OzLanguage extends TruffleLanguage<Object> {

	public static final String MIME_TYPE = "application/x-oz";

	public static final OzLanguage INSTANCE = new OzLanguage();

	@Override
	protected Object createContext(Env env) {
		return null;
	}

	@Override
	protected CallTarget parse(Source source, Node context, String... argumentNames) throws IOException {
		OzRootNode rootNode = new Translator().parseAndTranslate(source);
		return Truffle.getRuntime().createCallTarget(rootNode);
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
	protected Object evalInContext(Source source, Node node, MaterializedFrame parentFrame) throws IOException {
		return null;
	}

}
