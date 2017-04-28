package org.mozartoz.truffle.runtime;

import java.io.IOException;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.RunCallTargetNode;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Registration;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

@Registration(name = "Oz", version = "0.1", mimeType = OzLanguage.MIME_TYPE)
@ProvidedTags({ RootTag.class })
public class OzLanguage extends TruffleLanguage<Object> {

	public static final String MIME_TYPE = "application/x-oz";
	public static final boolean ON_GRAAL = Truffle.getRuntime().getName().startsWith("Graal");

	public static OzLanguage SINGLETON;

	public OzLanguage() {
		super();
		SINGLETON = this;
	}

	@Override
	protected Object createContext(Env env) {
		return null;
	}

	@Override
	protected CallTarget parse(ParsingRequest parsingRequest) {
		final RootCallTarget ozTarget = Loader.getInstance().parseFunctor(parsingRequest.getSource());
		RunCallTargetNode runOzTarget = new RunCallTargetNode(ozTarget);
		String name = ozTarget.getRootNode().getName();
		FrameDescriptor frameDescriptor = new FrameDescriptor();
		OzRootNode rootNode = new OzRootNode(Loader.MAIN_SOURCE_SECTION, name, frameDescriptor, runOzTarget, -OzArguments.IMPLICIT_ARGUMENTS, false);
		return rootNode.toCallTarget();
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
	protected Object evalInContext(Source source, Node node, MaterializedFrame parentFrame) throws IOException {
		return null;
	}

}
