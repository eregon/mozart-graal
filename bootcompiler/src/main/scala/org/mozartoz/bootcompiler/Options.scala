package org.mozartoz.bootcompiler

object Options {

  val SELF_TAIL_CALLS = System.getProperty("oz.tail.selfcalls", "true") == "true";

  val FRAME_FILTERING = System.getProperty("oz.vars.filtering", "true") == "true";

}
