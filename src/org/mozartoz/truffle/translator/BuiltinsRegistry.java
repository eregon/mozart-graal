package org.mozartoz.truffle.translator;

import org.mozartoz.bootcompiler.symtab.Builtin;
import org.mozartoz.bootcompiler.symtab.Builtins;

public class BuiltinsRegistry {

	private static final Builtins BUILTINS = new Builtins();

	public static void register(String moduleName, String builtinName, int arity) {
		BUILTINS.register(Builtin.create(moduleName, builtinName, arity));
	}

	public static Builtins getBuiltins() {
		return BUILTINS;
	}

}
