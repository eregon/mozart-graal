package org.mozartoz.truffle.nodes;

import org.mozartoz.truffle.runtime.OzVar;

public class OzGuards {

	public static boolean isVar(Object value) {
		return value instanceof OzVar;
	}

}
