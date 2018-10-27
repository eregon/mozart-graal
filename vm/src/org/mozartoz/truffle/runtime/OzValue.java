package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

public class OzValue implements TruffleObject {

	@Override
	public ForeignAccess getForeignAccess() {
		return null;
	}

}
