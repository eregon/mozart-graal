package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

public class Unit implements TruffleObject {

	public static final Unit INSTANCE = new Unit();

	private Unit() {
	}

	@Override
	public String toString() {
		return "unit";
	}

	@Override
	public ForeignAccess getForeignAccess() {
		return null;
	}

}
