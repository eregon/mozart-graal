package org.mozartoz.truffle.nodes.builtins;

import static org.mozartoz.truffle.nodes.builtins.Builtin.ALL;

import java.io.File;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltins.ToAtomNode;
import org.mozartoz.truffle.nodes.builtins.VirtualStringBuiltinsFactory.ToAtomNodeFactory;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.translator.Loader;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;

public abstract class OSBuiltins {

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("url")
	public static abstract class BootURLLoadNode extends OzNode {

		@Child ToAtomNode toAtomNode = ToAtomNodeFactory.create(null);

		@Specialization
		Object bootURLLoad(Object urlVS) {
			String url = toAtomNode.executeToAtom(urlVS);
			assert url.startsWith("x-oz://system/");
			assert url.endsWith(".ozf");
			String name = url.substring("x-oz://system/".length(), url.length() - 1);
			String path = Loader.MAIN_LIB_DIR + "/sys/" + name;
			assert new File(path).exists();
			Source source = Loader.createSource(path);
			Loader loader = Loader.getInstance();
			return loader.execute(loader.parseFunctor(source));
		}

	}

	@GenerateNodeFactory
	public static abstract class RandNode extends OzNode {

		@Specialization
		Object rand() {
			return unimplemented();
		}

	}

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("seed")
	public static abstract class SrandNode extends OzNode {

		@Specialization
		Object srand(Object seed) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChild("min")
	public static abstract class RandLimitsNode extends OzNode {

		@Specialization
		Object randLimits(OzVar min) {
			return unimplemented();
		}

	}

	@Builtin(deref = ALL)
	@GenerateNodeFactory
	@NodeChild("var")
	public static abstract class GetEnvNode extends OzNode {

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

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileName"), @NodeChild("mode") })
	public static abstract class FopenNode extends OzNode {

		@Specialization
		Object fopen(Object fileName, Object mode) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileNode"), @NodeChild("count"), @NodeChild("end"), @NodeChild("actualCount") })
	public static abstract class FreadNode extends OzNode {

		@Specialization
		Object fread(Object fileNode, Object count, Object end, OzVar actualCount) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("fileNode"), @NodeChild("data") })
	public static abstract class FwriteNode extends OzNode {

		@Specialization
		Object fwrite(Object fileNode, Object data) {
			return unimplemented();
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

	@Builtin(proc = true)
	@GenerateNodeFactory
	@NodeChild("fileNode")
	public static abstract class FcloseNode extends OzNode {

		@Specialization
		Object fclose(Object fileNode) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class StdinNode extends OzNode {

		@Specialization
		Object stdin() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class StdoutNode extends OzNode {

		@Specialization
		Object stdout() {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	public static abstract class StderrNode extends OzNode {

		@Specialization
		Object stderr() {
			return unimplemented();
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

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("inExecutable"), @NodeChild("inArgv"), @NodeChild("outPid") })
	public static abstract class PipeNode extends OzNode {

		@Specialization
		Object pipe(Object inExecutable, Object inArgv, OzVar outPid) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("count"), @NodeChild("tail") })
	public static abstract class PipeConnectionReadNode extends OzNode {

		@Specialization
		Object pipeConnectionRead(Object connection, Object count, Object tail) {
			return unimplemented();
		}

	}

	@GenerateNodeFactory
	@NodeChildren({ @NodeChild("connection"), @NodeChild("data") })
	public static abstract class PipeConnectionWriteNode extends OzNode {

		@Specialization
		Object pipeConnectionWrite(Object connection, Object data) {
			return unimplemented();
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
