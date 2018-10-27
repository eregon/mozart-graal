package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

public class Unit extends OzValue {

	public static final Unit INSTANCE = new Unit();

	private Unit() {
	}

	@Override
	public String toString() {
		return "unit";
	}

}
