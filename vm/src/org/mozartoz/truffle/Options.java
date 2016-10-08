package org.mozartoz.truffle;

public abstract class Options {

	public static final boolean MEASURE_STARTUP = System.getProperty("oz.measure.startup") != null;

	public static final String SHOW_PROC_AST = System.getProperty("oz.show.ast");

}
