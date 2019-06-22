package org.mozartoz.truffle.runtime;

import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.RunMainNode;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Registration;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.source.Source;

@Registration(name = "Oz", id = "oz", version = "0.1", characterMimeTypes = OzLanguage.MIME_TYPE)
@ProvidedTags({ RootTag.class })
public class OzLanguage extends TruffleLanguage<OzContext> {

	public static final String MIME_TYPE = "application/x-oz";
	public static final boolean ON_GRAAL = Truffle.getRuntime().getName().startsWith("Graal");

	public static OzContext getContext() {
		return getCurrentContext(OzLanguage.class);
	}

	public static OzLanguage getLanguage() {
		return getCurrentLanguage(OzLanguage.class);
	}

	public String getHome() {
		return getLanguageHome();
	}

	@Override
	protected OzContext createContext(Env env) {
		return new OzContext(this, env);
	}

	@Override
	protected void initializeContext(OzContext context) {
		context.initialize();
	}

	@Override
	protected void finalizeContext(OzContext context) {
		context.finalizeContext();
	}

	@Override
	protected boolean patchContext(OzContext context, Env newEnv) {
		return context.patchContext(newEnv);
	}

	@Override
	protected boolean isThreadAccessAllowed(Thread thread, boolean singleThreaded) {
		return true;
	}

	@Override
	protected CallTarget parse(ParsingRequest parsingRequest) {
		Source source = parsingRequest.getSource();
		RunMainNode runMain = new RunMainNode(source);
		OzRootNode rootNode = new OzRootNode(this,
				Loader.MAIN_SOURCE_SECTION,
				source.getName(),
				new FrameDescriptor(),
				runMain,
				-OzArguments.IMPLICIT_ARGUMENTS,
				false);
		return rootNode.toCallTarget();
	}

	@Override
	protected boolean isObjectOfLanguage(Object object) {
		return true;
	}

}
