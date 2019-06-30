package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltins.ToAtomNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.nodes.local.BindNode;
import org.mozartoz.truffle.runtime.*;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public abstract class OSBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("url")
	public static abstract class BootURLLoadNode extends OzNode {

		private static final String[] SYSTEM_LOAD_PATH = new String[]{
				"lib/main/sys",
				"lib/main/support",
				"lib/main/sp",
				"lib/main/op",
				"lib/main/cp",
				"lib/main/ap",
				"lib/main/wp",
				"lib/tools/panel",
				"lib/tools/browser",
				"lib/compiler",
				"stdlib/wp/qtk",
		};

		@CreateCast("url")
		protected OzNode castURL(OzNode url) {
			return ToAtomNodeFactory.create(url);
		}

		@TruffleBoundary
		@Specialization
		Object bootURLLoad(String url) {
			// Remove final "f"
			if (url.endsWith(".ozf")) {
				url = url.substring(0, url.length() - 1);
			}

			assert url.endsWith(".oz") : url;

			String path = null;
			if (url.startsWith("x-oz://system/")) {
				String name = url.substring(url.lastIndexOf('/') + 1);
				path = findSystemFunctor(url, name);
			} else {
				path = url;
			}

			if (new File(path).exists()) {
				OzContext context = OzLanguage.getContext();
				Source source = Loader.createSource(context.getEnv(), path);
				return context.execute(context.getTranslatorDriver().parseFunctor(source, context.isLoadingMain()));
			} else {
				throw notFound(url);
			}
		}

		private String findSystemFunctor(String url, String name) {
			String home = OzLanguage.getContext().getHome();
			for (String loadPath : SYSTEM_LOAD_PATH) {
				File file = new File(home + "/" + loadPath, name);
				if (file.exists()) {
					return file.getPath();
				}
			}
			throw notFound(url);
		}

		private OzException notFound(String url) {
			return Errors.OSError(this, "os", "bootURLLoad " + url, 2L, "No such file or directory");
		}

	}

	private static final long RAND_MIN = 0L;
	private static final long RAND_MAX = 1L << 32;

	@Builtin
	@GenerateNodeFactory
	public static abstract class RandNode extends OzNode {

		@Specialization
		long rand() {
			long random = ThreadLocalRandom.current().nextInt();
			long value = random - (long) Integer.MIN_VALUE;
			return value;
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("seed")
	public static abstract class SrandNode extends OzNode {

		@Specialization
		Object srand(long seed) {
			ThreadLocalRandom.current().setSeed(seed);
			return unit;
		}

	}

	@Builtin
	@GenerateNodeFactory
	@NodeChild("min")
	public static abstract class RandLimitsNode extends OzNode {

		@Specialization
		long randLimits(OzVar min) {
			min.bind(RAND_MIN);
			return RAND_MAX;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("var")
	public static abstract class GetEnvNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object getEnv(String var) {
			String value = System.getenv(var);
			if (value != null) {
				return value.intern();
			} else {
				return false;
			}
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("var"), @NodeChild("value") })
	public static abstract class PutEnvNode extends OzNode {

		@Specialization
		Object putEnv(Object var, Object value) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("directory")
	public static abstract class GetDirNode extends OzNode {

		@Specialization
		Object getDir(Object directory) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class GetCWDNode extends OzNode {

		@TruffleBoundary
		@Specialization
		String getCWD() {
			return System.getProperty("user.dir").intern();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("dir")
	public static abstract class ChDirNode extends OzNode {

		@Specialization
		Object chDir(Object dir) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class TmpnamNode extends OzNode {

		@Specialization
		Object tmpnam() {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileName"), @NodeChild("mode") })
	public static abstract class FopenNode extends OzNode {

		@CreateCast("fileName")
		protected OzNode castFileName(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@CreateCast("mode")
		protected OzNode castMode(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@TruffleBoundary
		@Specialization
		Object fopen(String fileName, String mode) {
			try {
				switch (mode) {
				case "rb":
					return new FileInputStream(new File(fileName));
				case "wb":
					return new FileOutputStream(new File(fileName));
				default:
					DynamicObject error = Errors.OS_ERROR_FACTORY4.newRecord("os", "fopen", 1L, "Opening mode not implemented");
					throw new OzException(this, OzException.newSystemError(error));
				}
			} catch (FileNotFoundException e) {
				DynamicObject error = Errors.OS_ERROR_FACTORY4.newRecord("os", "fopen", 2L, "No such file or directory");
				throw new OzException(this, OzException.newSystemError(error));
			}
		}
	}

	@Builtin(deref = { 1, 2 }, tryDeref = { 3, 4 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("file"), @NodeChild("count"), @NodeChild("tail"), @NodeChild("actualCount") })
	public static abstract class FreadNode extends OzNode {

		private static final int MAX_BUFFER_SIZE = 1024 * 1024;

		@Child BindNode bindNode = BindNode.create();

		@TruffleBoundary
		@Specialization
		Object fread(FileInputStream file, long count, Object tail, Object actualCount) {
			if (count <= 0) {
				bindNode.executeBind(actualCount, 0L);
				return tail;
			}

			int bufferSize = (int) Math.min(count, MAX_BUFFER_SIZE);
			byte[] buffer = new byte[bufferSize];
			int bytes;
			try {
				bytes = file.read(buffer);
			} catch (IOException e) {
				throw new Error(e);
			}

			if (bytes == -1) {
				bindNode.executeBind(actualCount, 0L);
				return tail;
			}

			Object list = tail;
			for (int i = bytes - 1; i >= 0; i--) {
				long ch = buffer[i];
				list = new OzCons(ch, list);
			}
			bindNode.executeBind(actualCount, (long) bytes);
			return list;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileNode"), @NodeChild("data") })
	public static abstract class FwriteNode extends OzNode {

		@TruffleBoundary
		@Specialization
		long fwrite(FileOutputStream file, byte[] data) {
			try {
				file.write(data);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return data.length;
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileNode"), @NodeChild("offset"), @NodeChild("whence") })
	public static abstract class FseekNode extends OzNode {

		@Specialization
		Object fseek(Object fileNode, Object offset, Object whence) {
			return unimplemented();
		}

	}

	@Builtin(proc = true, deref = ALL)
	@GenerateNodeFactory
	@NodeChild("file")
	public static abstract class FcloseNode extends OzNode {

		@TruffleBoundary
		@Specialization
		Object fclose(FileInputStream file) {
			try {
				file.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return unit;
		}

		@TruffleBoundary
		@Specialization
		Object fclose(FileOutputStream file) {
			try {
				file.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return unit;
		}

	}

	@GenerateNodeFactory
	public static abstract class StdinNode extends OzNode {

		private static final OzIO IN = new OzIO(System.in);

		@Specialization
		Object stdin() {
			return IN;
		}

	}

	@GenerateNodeFactory
	public static abstract class StdoutNode extends OzNode {

		private static final OzIO OUT = new OzIO(System.out);

		@Specialization
		Object stdout() {
			return OUT;
		}

	}

	@GenerateNodeFactory
	public static abstract class StderrNode extends OzNode {

		private static final OzIO ERR = new OzIO(System.err);

		@Specialization
		Object stderr() {
			return ERR;
		}

	}

	@GenerateNodeFactory
	@NodeChild("cmd")
	public static abstract class SystemNode extends OzNode {

		@Specialization
		Object system(Object cmd) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("ipVersion"), @NodeChild("port") })
	public static abstract class TcpAcceptorCreateNode extends OzNode {

		@Specialization
		Object tcpAcceptorCreate(Object ipVersion, Object port) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("acceptor")
	public static abstract class TcpAcceptNode extends OzNode {

		@Specialization
		Object tcpAccept(Object acceptor) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("acceptor")
	public static abstract class TcpCancelAcceptNode extends OzNode {

		@Specialization
		Object tcpCancelAccept(Object acceptor) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("acceptor")
	public static abstract class TcpAcceptorCloseNode extends OzNode {

		@Specialization
		Object tcpAcceptorClose(Object acceptor) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("host"), @NodeChild("service") })
	public static abstract class TcpConnectNode extends OzNode {

		@Specialization
		Object tcpConnect(Object host, Object service) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("count"), @NodeChild("tail") })
	public static abstract class TcpConnectionReadNode extends OzNode {

		@Specialization
		Object tcpConnectionRead(Object connection, Object count, Object tail) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("data") })
	public static abstract class TcpConnectionWriteNode extends OzNode {

		@Specialization
		Object tcpConnectionWrite(Object connection, Object data) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("what") })
	public static abstract class TcpConnectionShutdownNode extends OzNode {

		@Specialization
		Object tcpConnectionShutdown(Object connection, Object what) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("connection")
	public static abstract class TcpConnectionCloseNode extends OzNode {

		@Specialization
		Object tcpConnectionClose(Object connection) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("inExecutable"), @NodeChild("inArgv"), @NodeChild("inDoKill") })
	public static abstract class ExecNode extends OzNode {

		@Specialization
		Object exec(Object inExecutable, Object inArgv, Object inDoKill) {
			return unimplemented();
		}

	}

	@Builtin(deref = { 1, 2 })
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("executable"), @NodeChild("argv"), @NodeChild("outPid") })
	public static abstract class PipeNode extends OzNode {

		@Child ToAtomNode toAtomNode = ToAtomNode.create();
		@Child DerefNode derefConsNode = DerefNode.create();
		@Child DerefNode derefNode = DerefNode.create();

		@CreateCast("executable")
		protected OzNode castExecutable(OzNode value) {
			return ToAtomNodeFactory.create(value);
		}

		@TruffleBoundary
		@Specialization
		Object pipe(String executable, OzCons argv, OzVar outPid) {
			if (executable.endsWith("/ozwish")) {
				executable = OzLanguage.getContext().getHome() + "/wish/ozwish";
				argv = new OzCons(executable, argv.getTail());
			}

			List<String> command = new ArrayList<>();
			command.add(executable);
			argv.forEach(derefConsNode, e -> {
				command.add(toAtomNode.executeToAtom(derefNode.executeDeref(e)));
			});
			ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
			try {
				Process process = builder.start();
				outPid.bind(process);
				OzLanguage.getContext().registerChildProcess(process);
				InputStream input = process.getInputStream();
				OutputStream output = process.getOutputStream();
				return new OzCons(input, output);
			} catch (IOException ioException) {
				throw new RuntimeException(ioException);
			}
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("count"), @NodeChild("tail") })
	public static abstract class PipeConnectionReadNode extends OzNode {

		static final RecordFactory READ_RESULT_FACTORY = Arity.build("succeeded", 1L, 2L).createFactory();

		@TruffleBoundary
		@Specialization
		Object pipeConnectionRead(OzCons connection, long count, Object tail) {
			InputStream inputStream = (InputStream) connection.getHead();
			try {
				while (inputStream.available() == 0) {
					OzThread.getCurrent().yield(this);
				}
				byte[] buffer = new byte[(int) count];
				int bytesRead = inputStream.read(buffer);
				Object list = byteArrayToOzList(buffer, bytesRead, tail);
				return READ_RESULT_FACTORY.newRecord((long) bytesRead, list);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Object byteArrayToOzList(byte[] buffer, int n, Object tail) {
			Object list = tail;
			for (int i = n - 1; i >= 0; i--) {
				list = new OzCons((long) buffer[i], list);
			}
			return list;
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("data") })
	public static abstract class PipeConnectionWriteNode extends OzNode {

		@TruffleBoundary
		@Specialization
		long pipeConnectionWrite(OzCons connection, byte[] data) {
			if (data.length == 0) {
				unimplemented();
				return 0L;
			}
			OutputStream outputStream = (OutputStream) connection.getTail();
			try {
				outputStream.write(data);
				outputStream.flush();
				return data.length;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("what") })
	public static abstract class PipeConnectionShutdownNode extends OzNode {

		@Specialization
		Object pipeConnectionShutdown(Object connection, Object what) {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("connection")
	public static abstract class PipeConnectionCloseNode extends OzNode {

		@Specialization
		Object pipeConnectionClose(Object connection) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class GetPIDNode extends OzNode {

		@Specialization
		Object getPID() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("name")
	public static abstract class GetHostByNameNode extends OzNode {

		@Specialization
		Object getHostByName(Object name) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class UNameNode extends OzNode {

		@Specialization
		Object uName() {
			return unimplemented();
		}

	}

}
