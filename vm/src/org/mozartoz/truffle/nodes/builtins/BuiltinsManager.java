package org.mozartoz.truffle.nodes.builtins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzRecord;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public abstract class BuiltinsManager {

	private static final String[] MODULES = {
			// Base.oz
			"Value",
			"Literal",
			"Cell",
			"Port",
			"Atom",
			"Name",
			"Int",
			"Float",
			"Number",
			"Tuple",
			"Procedure",
			"Dictionary",
			"Record",
			"Chunk",
			"VirtualString",
			"VirtualByteString",
			"Coders",
			"Array",
			"Object",
			"Thread",
			"Exception",
			"Time",
			"ForeignPointer",
			"CompactString",
			"System",
			"Property",
			"WeakReference",
			// Init.oz
			"Boot",
			"OS",
	};

	private static final Map<String, OzProc> BUILTINS = new HashMap<>();
	private static final Map<String, DynamicObject> BOOT_MODULES = new HashMap<>();
	private static DynamicObject BOOT_MODULES_RECORD;

	public static OzProc getBuiltin(String moduleName, String builtinName) {
		String name = moduleName + "." + builtinName;
		OzProc fun = BUILTINS.get(name);
		if (fun == null) {
			throw new Error("No builtin " + name);
		}
		// Create a new node tree for every call site
		RootNode rootNode = NodeUtil.cloneNode(((RootCallTarget) fun.callTarget).getRootNode());
		return new OzProc(Truffle.getRuntime().createCallTarget(rootNode), null);
	}

	public static DynamicObject getBootModule(String name) {
		assert BOOT_MODULES.containsKey(name);
		return BOOT_MODULES.get(name);
	}

	public static DynamicObject getBootModulesRecord() {
		return BOOT_MODULES_RECORD;
	}

	public static void defineBuiltins() {
		assert BUILTINS.isEmpty();
		String pkg = BuiltinsManager.class.getPackage().getName();
		for (String module : MODULES) {
			try {
				Class<?> moduleFactory = Class.forName(pkg + "." + module + "BuiltinsFactory");
				Object untypedFactories = moduleFactory.getMethod("getFactories").invoke(null);
				@SuppressWarnings("unchecked")
				List<NodeFactory<? extends OzNode>> factories = (List<NodeFactory<? extends OzNode>>) untypedFactories;
				installBuiltins(module, factories);
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}
		BOOT_MODULES.put("Boot_WeakRef", BOOT_MODULES.get("Boot_WeakReference")); // TODO: hack
		BOOT_MODULES_RECORD = OzRecord.buildRecord("bootModules", BOOT_MODULES);
	}

	private static void installBuiltins(String module, List<NodeFactory<? extends OzNode>> factories) {
		// The builtins of this module only, indexed by the builtin name
		Map<String, OzProc> builtins = new HashMap<>(factories.size());

		for (NodeFactory<? extends OzNode> factory : factories) {
			Builtin builtin = factory.getNodeClass().getAnnotation(Builtin.class);
			if (builtin == null) {
				builtin = Builtin.DEFAULT;
			}

			final String builtinName;
			if (!builtin.name().isEmpty()) {
				builtinName = builtin.name();
			} else {
				String nodeName = factory.getNodeClass().getSimpleName();
				builtinName = Character.toLowerCase(nodeName.charAt(0)) + nodeName.substring(1, nodeName.lastIndexOf("Node"));
			}
			String name = module + "." + builtinName;
			SourceSection sourceSection = SourceSection.createUnavailable("builtin", name);

			int arity = factory.getNodeSignatures().get(0).size();
			Object[] readArguments = new OzNode[arity];
			for (int i = 0; i < readArguments.length; i++) {
				readArguments[i] = readArgumentNode(builtin, i);
			}
			OzNode node = factory.createNode(readArguments);
			if (!builtin.proc()) {
				node = BindNodeGen.create(null, new ReadArgumentNode(arity), node);
			}

			OzRootNode rootNode = new OzRootNode(sourceSection, new FrameDescriptor(), node);
			CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
			OzProc function = new OzProc(callTarget, null);
			assert !BUILTINS.containsKey(name) : name;
			BUILTINS.put(name, function);
			builtins.put(builtinName.intern(), function);
		}

		String label = module.toLowerCase().intern();
		BOOT_MODULES.put(("Boot_" + module).intern(), OzRecord.buildRecord(label, builtins));
	}

	private static OzNode readArgumentNode(Builtin builtin, int i) {
		ReadArgumentNode argumentNode = new ReadArgumentNode(i);
		if (derefInclude(builtin.deref(), i + 1)) {
			return DerefNodeGen.create(argumentNode);
		} else {
			return argumentNode;
		}
	}

	private static boolean derefInclude(int[] deref, int i) {
		for (int e : deref) {
			if (e == Builtin.ALL || e == i) {
				return true;
			}
		}
		return false;
	}

}