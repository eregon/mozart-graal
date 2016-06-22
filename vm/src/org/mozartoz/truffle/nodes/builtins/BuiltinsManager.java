package org.mozartoz.truffle.nodes.builtins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzRecord;
import org.mozartoz.truffle.translator.BuiltinsRegistry;

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
			"Debug",
			"Reflection",
			"Space",
			"Browser",
	};

	private static final Map<String, OzProc> BUILTINS = new HashMap<>();
	private static final Map<String, DynamicObject> BOOT_MODULES = new HashMap<>();
	private static DynamicObject BOOT_MODULES_RECORD;

	public static OzProc getBuiltin(String moduleName, String builtinName) {
		return getBuiltin(moduleName + "." + builtinName);
	}

	public static OzProc getBuiltin(String name) {
		OzProc fun = BUILTINS.get(name);
		if (fun == null) {
			throw new Error("No builtin " + name);
		}
		// Create a new node tree for every call site
		RootNode rootNode = NodeUtil.cloneNode(fun.callTarget.getRootNode());
		return new OzProc(Truffle.getRuntime().createCallTarget(rootNode), null, fun.arity);
	}

	public static DynamicObject getBootModule(String name) {
		assert BOOT_MODULES.containsKey(name) : name;
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
				node = BindNodeGen.create(new ReadArgumentNode(arity), node);
				arity++;
			}

			OzRootNode rootNode = new OzRootNode(sourceSection, new FrameDescriptor(), node, arity);
			RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
			OzProc function = new OzProc(callTarget, null, arity);

			assert !BUILTINS.containsKey(name) : name;
			BUILTINS.put(name, function);
			builtins.put(builtinName.intern(), function);
			BuiltinsRegistry.register(module, builtinName, arity);
		}

		String label = module.toLowerCase().intern();
		BOOT_MODULES.put(("Boot_" + module).intern(), OzRecord.buildRecord(label, builtins));
	}

	private static OzNode readArgumentNode(Builtin builtin, int i) {
		ReadArgumentNode argumentNode = new ReadArgumentNode(i);
		if (annoArrayInclude(builtin.deref(), i + 1)) {
			return DerefNode.create(argumentNode);
		} else if (annoArrayInclude(builtin.tryDeref(), i + 1)) {
			return DerefIfBoundNode.create(argumentNode);
		} else {
			return argumentNode;
		}
	}

	private static boolean annoArrayInclude(int[] array, int i) {
		for (int e : array) {
			if (e == Builtin.ALL || e == i) {
				return true;
			}
		}
		return false;
	}

}
