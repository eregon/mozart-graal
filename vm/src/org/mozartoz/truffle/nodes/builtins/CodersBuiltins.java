package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.OzCons;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class CodersBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("string"), @NodeChild("encoding"), @NodeChild("variant") })
	public static abstract class EncodeNode extends OzNode {

		@CreateCast("string")
		protected OzNode castString(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@TruffleBoundary
		@Specialization
		byte[] encode(String string, String encoding, String variant) {
			assert variant == "nil";
			Charset charset = Charset.forName(encoding);
			ByteBuffer buffer = charset.encode(string);
			return buffer.array();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("value"), @NodeChild("encoding"), @NodeChild("variant") })
	public static abstract class DecodeNode extends OzNode {

		@Child DerefNode derefConsNode = DerefNode.create();
		@Child DerefNode derefNode = DerefNode.create();

		@TruffleBoundary
		@Specialization
		String decode(byte[] value, String encoding, String variant) {
			assert variant == "nil";
			Charset charset = Charset.forName(encoding);
			return charset.decode(ByteBuffer.wrap(value)).toString().intern();
		}

		@TruffleBoundary
		@Specialization
		String decode(OzCons cons, String encoding, String variant) {
			List<Byte> list = new ArrayList<Byte>();
			cons.forEach(derefConsNode, e -> list.add((byte) (long) derefNode.executeDeref(e)));
			byte[] array = new byte[list.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = list.get(i);
			}
			return decode(array, encoding, variant);
		}

	}

}
