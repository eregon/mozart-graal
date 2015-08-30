package org.mozartoz.truffle.nodes;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;

@TypeSystem({ long.class, BigInteger.class })
public abstract class OzTypes {

	@ImplicitCast
	@TruffleBoundary
	public static BigInteger castBigInteger(long value) {
		return BigInteger.valueOf(value);
	}

}
