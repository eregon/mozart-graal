package org.mozartoz.truffle.translator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.FailNodeFactory.FailNodeGen;
import org.mozartoz.truffle.nodes.builtins.ExceptionBuiltinsFactory.RaiseNodeFactory.RaiseNodeGen;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.HeadNodeGen;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.TailNodeGen;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory.DotNodeGen;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory.EqualNodeGen;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.control.AndNode;
import org.mozartoz.truffle.nodes.control.IfNode;
import org.mozartoz.truffle.nodes.control.NoElseNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.control.SkipNode;
import org.mozartoz.truffle.nodes.control.TryNode;
import org.mozartoz.truffle.nodes.literal.BooleanLiteralNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNodeGen;
import org.mozartoz.truffle.nodes.literal.ListLiteralNode;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.literal.MakeDynamicRecordNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.literal.UnboundLiteralNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.nodes.local.FrameSlotNode;
import org.mozartoz.truffle.nodes.local.InitializeArgNode;
import org.mozartoz.truffle.nodes.local.InitializeTmpNode;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchOpenRecordNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzCell;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzDict;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzObject;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class OzSerializer {

	private static class StringSerializer extends Serializer<String> {
		public void write(Kryo kryo, Output output, String str) {
			output.writeString(str);
		}

		public String read(Kryo kryo, Input input, Class<String> type) {
			return input.readString().intern();
		}
	}

	private static class OzUniqueNameSerializer extends Serializer<OzUniqueName> {
		public void write(Kryo kryo, Output output, OzUniqueName uniqueName) {
			output.writeString(uniqueName.getName());
		}

		public OzUniqueName read(Kryo kryo, Input input, Class<OzUniqueName> type) {
			String name = input.readString().intern();
			return OzUniqueName.get(name);
		}
	}

	private static class ShapeSerializer extends Serializer<Shape> {
		public void write(Kryo kryo, Output output, Shape shape) {
			List<Property> propertyList = shape.getPropertyListInternal(true);

			Property firstProperty = propertyList.get(0);
			if (firstProperty != Arity.LABEL_PROPERTY) {
				throw new Error("first property was " + firstProperty);
			}

			int n = propertyList.size() - 1;
			output.writeInt(n);

			for (int i = 1; i < propertyList.size(); i++) {
				Property property = propertyList.get(i);
				Object key = property.getKey();
				kryo.writeClassAndObject(output, key);
			}
		}

		public Shape read(Kryo kryo, Input input, Class<Shape> type) {
			int n = input.readInt();
			Object[] features = new Object[n];
			for (int i = 0; i < n; i++) {
				features[i] = kryo.readClassAndObject(input);
			}
			return Arity.build("deserialize", features).getShape();
		}
	}

	private static class DynamicObjectSerializer extends Serializer<DynamicObject> {
		public void write(Kryo kryo, Output output, DynamicObject object) {
			Shape shape = object.getShape();
			kryo.writeObject(output, shape);

			List<Property> propertyList = shape.getPropertyListInternal(true);
			output.writeInt(propertyList.size());
			for (Property property : propertyList) {
				Object value = property.get(object, shape);
				kryo.writeClassAndObject(output, value);
			}
		}

		public DynamicObject read(Kryo kryo, Input input, Class<DynamicObject> type) {
			Shape shape = (Shape) kryo.readObject(input, SHAPE);
			DynamicObject dynamicObject = shape.newInstance();
			kryo.reference(dynamicObject);

			int n = input.readInt();
			List<Property> propertyList = shape.getPropertyListInternal(true);
			assert propertyList.get(0) == Arity.LABEL_PROPERTY;
			assert n == propertyList.size();

			for (int i = 0; i < n; i++) {
				Property property = propertyList.get(i);
				Object value = kryo.readClassAndObject(input);
				property.setInternal(dynamicObject, value);
			}
			return dynamicObject;
		}
	}

	private static class OzProcSerializer extends Serializer<OzProc> {
		public void write(Kryo kryo, Output output, OzProc proc) {
			RootNode rootNode = proc.callTarget.getRootNode();
			SourceSection section = rootNode.getSourceSection();
			boolean isBuiltin = section.getShortDescription().startsWith("builtin:");
			output.writeBoolean(isBuiltin);
			if (isBuiltin) {
				output.writeString(section.getIdentifier());
			} else {
				output.writeInt(proc.arity);
				kryo.writeObject(output, proc.callTarget);
				kryo.writeObject(output, proc.declarationFrame);
			}
		}

		public OzProc read(Kryo kryo, Input input, Class<OzProc> type) {
			boolean isBuiltin = input.readBoolean();
			final OzProc ozProc;
			if (isBuiltin) {
				String identifier = input.readString();
				ozProc = BuiltinsManager.getBuiltin(identifier);
			} else {
				int arity = input.readInt();
				ozProc = new OzProc(null, null, arity);
				kryo.reference(ozProc);
				ozProc.callTarget = kryo.readObject(input, ROOT_CALL_TARGET);
				ozProc.declarationFrame = kryo.readObject(input, MATERIALIZED_FRAME);
			}
			return ozProc;
		}
	}

	private static class RootCallTargetSerializer extends Serializer<RootCallTarget> {
		public void write(Kryo kryo, Output output, RootCallTarget callTarget) {
			kryo.writeObject(output, callTarget.getRootNode());
		}

		public RootCallTarget read(Kryo kryo, Input input, Class<RootCallTarget> type) {
			OzRootNode rootNode = kryo.readObject(input, OzRootNode.class);
			RootCallTarget callTarget = TRUFFLE.createCallTarget(rootNode);
			assert NodeUtil.verify(rootNode);
			return callTarget;
		}
	}

	private static class OzRootNodeSerializer extends Serializer<OzRootNode> {
		public void write(Kryo kryo, Output output, OzRootNode rootNode) {
			kryo.writeClassAndObject(output, rootNode.getSourceSection());
			kryo.writeObject(output, rootNode.getFrameDescriptor());
			kryo.writeClassAndObject(output, rootNode.getBody());
			output.writeInt(rootNode.getArity());
		}

		public OzRootNode read(Kryo kryo, Input input, Class<OzRootNode> type) {
			SourceSection sourceSection = (SourceSection) kryo.readClassAndObject(input);
			FrameDescriptor frameDescriptor = kryo.readObject(input, FrameDescriptor.class);
			OzNode body = (OzNode) kryo.readClassAndObject(input);
			int arity = input.readInt();
			return new OzRootNode(sourceSection, frameDescriptor, body, arity);
		}
	}

	private static class FrameSlotSerializer extends Serializer<FrameSlot> {
		public void write(Kryo kryo, Output output, FrameSlot frameSlot) {
			kryo.writeObject(output, frameSlot.getFrameDescriptor());
			output.writeString((String) frameSlot.getIdentifier());
		}

		public FrameSlot read(Kryo kryo, Input input, Class<FrameSlot> type) {
			FrameDescriptor frameDescriptor = kryo.readObject(input, FrameDescriptor.class);
			String identifier = input.readString().intern();
			return frameDescriptor.findOrAddFrameSlot(identifier);
		}
	}

	private static class FrameDescriptorSerializer extends Serializer<FrameDescriptor> {
		public void write(Kryo kryo, Output output, FrameDescriptor frameDescriptor) {
			assert frameDescriptor.getDefaultValue() == null;
			List<? extends FrameSlot> slots = frameDescriptor.getSlots();
			output.writeInt(slots.size());
			for (FrameSlot frameSlot : slots) {
				String identifier = (String) frameSlot.getIdentifier();
				output.writeString(identifier);
			}
		}

		public FrameDescriptor read(Kryo kryo, Input input, Class<FrameDescriptor> type) {
			FrameDescriptor frameDescriptor = new FrameDescriptor(null);
			kryo.reference(frameDescriptor);
			int n = input.readInt();
			for (int i = 0; i < n; i++) {
				String identifier = input.readString().intern();
				frameDescriptor.addFrameSlot(identifier);
			}
			return frameDescriptor;
		}
	}

	private static class FrameSerializer extends Serializer<Frame> {

		static final Object[] FAKE_ARGUMENTS = new Object[0];
		Field argumentsField;
		Field wrappedField;

		public FrameSerializer() {
			try {
				argumentsField = MATERIALIZED_FRAME.getDeclaredField("arguments");
				argumentsField.setAccessible(true);
			} catch (NoSuchFieldException nsfe) {
				try {
					wrappedField = MATERIALIZED_FRAME.getDeclaredField("wrapped");
					wrappedField.setAccessible(true);
					argumentsField = wrappedField.get(FRAME).getClass().getDeclaredField("arguments");
					argumentsField.setAccessible(true);
				} catch (ReflectiveOperationException e) {
					throw new Error(e);
				}
			}
		}

		public void write(Kryo kryo, Output output, Frame frame) {
			FrameDescriptor frameDescriptor = frame.getFrameDescriptor();
			kryo.writeObject(output, frameDescriptor);
			kryo.writeObject(output, frame.getArguments());
			for (FrameSlot slot : frameDescriptor.getSlots()) {
				Object value = frame.getValue(slot);
				kryo.writeClassAndObject(output, value);
			}
		}

		public Frame read(Kryo kryo, Input input, Class<Frame> type) {
			FrameDescriptor frameDescriptor = kryo.readObject(input, FrameDescriptor.class);
			Frame frame = TRUFFLE.createMaterializedFrame(FAKE_ARGUMENTS, frameDescriptor);
			kryo.reference(frame);
			setFrameArguments(frame, kryo.readObject(input, Object[].class));
			for (FrameSlot slot : frameDescriptor.getSlots()) {
				Object value = kryo.readClassAndObject(input);
				frame.setObject(slot, value);
			}
			return frame;
		}

		private void setFrameArguments(Frame frame, Object[] arguments) {
			try {
				if (wrappedField == null) {
					argumentsField.set(frame, arguments);
				} else {
					argumentsField.set(wrappedField.get(frame), arguments);
				}
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}
	}

	private static class OzVarSerializer extends Serializer<OzVar> {
		public void write(Kryo kryo, Output output, OzVar var) {
			assert var.isBound();
			kryo.writeClassAndObject(output, var.getBoundValue(null));
		}

		public OzVar read(Kryo kryo, Input input, Class<OzVar> type) {
			OzVar var = new OzVar();
			kryo.reference(var);
			Object value = kryo.readClassAndObject(input);
			var.bind(value);
			return var;
		}
	}

	private static class FileSourceSerializer extends Serializer<Source> {
		public void write(Kryo kryo, Output output, Source source) {
			output.writeString(source.getName());
		}

		public Source read(Kryo kryo, Input input, Class<Source> type) {
			return sourceFromPath(input.readString());
		}
	}

	private static Object underef(Object value) {
		if (value instanceof DerefNode) {
			return ((DerefNode) value).getValue();
		} else if (value instanceof DerefIfBoundNode) {
			return ((DerefIfBoundNode) value).getValue();
		} else {
			return value;
		}
	}

	private static class NodeSerializer extends Serializer<Node> {
		private final Field[] fields;
		private final Constructor<?> constructor;

		public NodeSerializer(Class<? extends Node> klass) {
			Constructor<?>[] constructors = klass.getDeclaredConstructors();
			assert constructors.length == 1 : klass;
			constructor = constructors[0];
			constructor.setAccessible(true);
			Class<?>[] parameterTypes = constructor.getParameterTypes();

			List<Field> toSave = new ArrayList<>();
			int i = 0;
			for (Field field : klass.getDeclaredFields()) {
				Class<?> type = field.getType();
				if (type == parameterTypes[i]) {
					field.setAccessible(true);
					toSave.add(field);
					i++;
				} else if (parameterTypes[i] == FrameSlot.class && FrameSlotNode.class.isAssignableFrom(klass)) {
					toSave.add(null);
					i++;
				}
				if (i == parameterTypes.length) {
					break;
				}
			}
			fields = toSave.toArray(new Field[toSave.size()]);
			assert i == parameterTypes.length;
		}

		public void write(Kryo kryo, Output output, Node node) {
			try {
				for (Field field : fields) {
					Object value;
					if (field == null) {
						value = ((FrameSlotNode) node).getSlot();
					} else {
						value = field.get(node);
						if (value instanceof OzNode[]) {
							OzNode[] nodes = (OzNode[]) value;
							OzNode[] values = new OzNode[nodes.length];
							for (int i = 0; i < nodes.length; i++) {
								values[i] = (OzNode) underef(nodes[i]);
							}
							value = values;
						} else {
							value = underef(value);
						}
					}
					kryo.writeClassAndObject(output, value);
				}
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}

		public Node read(Kryo kryo, Input input, Class<Node> type) {
			Object[] values = new Object[fields.length];
			for (int i = 0; i < fields.length; i++) {
				values[i] = kryo.readClassAndObject(input);
			}
			try {
				return (Node) constructor.newInstance(values);
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}
	}


	private static class DSLNodeSerializer extends Serializer<Node> {
		private final Field[] fields;
		private final Method constructor;
		private final boolean removeDeref;

		public DSLNodeSerializer(Class<? extends Node> genClass) {
			Class<?> baseClass = genClass.getAnnotation(GeneratedBy.class).value();
			List<Field> toSave = new ArrayList<>();

			for (Field field : baseClass.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					if (!field.isAnnotationPresent(Child.class)) { // most likely a helper node
						toSave.add(field);
					}
				}
			}

			for (Field field : genClass.getDeclaredFields()) {
				String name = field.getName();
				if (!name.equals("specialization_")
						&& !name.startsWith("seenUnsupported")
						&& !name.startsWith("exclude")) {
					toSave.add(field);
				}
			}

			fields = toSave.toArray(new Field[toSave.size()]);
			Class<?>[] parameterTypes = new Class[fields.length];

			for (int i = 0; i < fields.length; i++) {
				fields[i].setAccessible(true);
				parameterTypes[i] = fields[i].getType();
			}

			Class<?> enclosingClass = genClass.getEnclosingClass();
			boolean hasFactoryClass = enclosingClass != null && NodeFactory.class.isAssignableFrom(enclosingClass);
			final Class<?> factoryClass = hasFactoryClass ? enclosingClass : genClass;
			this.removeDeref = !hasFactoryClass;

			try {
				constructor = factoryClass.getMethod("create", parameterTypes);
			} catch (NoSuchMethodException e) {
				throw new Error(e);
			}
		}

		public void write(Kryo kryo, Output output, Node node) {
			try {
				for (Field field : fields) {
					Object value = field.get(node);
					if (removeDeref) {
						value = underef(value);
					}
					kryo.writeClassAndObject(output, value);
				}
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}

		public Node read(Kryo kryo, Input input, Class<Node> type) {
			Object[] values = new Object[fields.length];
			for (int i = 0; i < fields.length; i++) {
				values[i] = kryo.readClassAndObject(input);
			}
			try {
				return (Node) constructor.invoke(null, values);
			} catch (ReflectiveOperationException e) {
				throw new Error(e);
			}
		}
	}

	private static class PrintStreamSerializer extends Serializer<PrintStream> {
		public void write(Kryo kryo, Output output, PrintStream stream) {
			if (stream == System.out) {
				output.writeByte(1);
			} else if (stream == System.err) {
				output.writeByte(2);
			} else {
				throw new Error();
			}
		}

		public PrintStream read(Kryo kryo, Input input, Class<PrintStream> type) {
			switch (input.readByte()) {
			case 1:
				return System.out;
			case 2:
				return System.err;
			default:
				throw new Error();
			}
		}
	}

	private static class InputStreamSerializer extends Serializer<InputStream> {
		public void write(Kryo kryo, Output output, InputStream stream) {
			if (stream == System.in) {
				output.writeByte(0);
			} else {
				throw new Error();
			}
		}

		public InputStream read(Kryo kryo, Input input, Class<InputStream> type) {
			switch (input.readByte()) {
			case 0:
				return (BufferedInputStream) System.in;
			default:
				throw new Error();
			}
		}
	}

	private static class SingletonSerializer extends Serializer<Object> {
		private final Object singleton;

		public SingletonSerializer(Object singleton) {
			this.singleton = singleton;
		}

		public void write(Kryo kryo, Output output, Object object) {
		}

		public Object read(Kryo kryo, Input input, Class<Object> type) {
			return singleton;
		}
	}

	private static final TruffleRuntime TRUFFLE = Truffle.getRuntime();
	private static final MaterializedFrame FRAME = TRUFFLE.createMaterializedFrame(new Object[0]);
	private static final Class<? extends MaterializedFrame> MATERIALIZED_FRAME = FRAME.getClass();
	private static final Class<? extends RootCallTarget> ROOT_CALL_TARGET =
			TRUFFLE.createCallTarget(RootNode.createConstantNode(null)).getClass();
	private static final Class<? extends Shape> SHAPE = Arity.EMPTY.getClass();
	private static final Class<? extends DynamicObject> DYNAMIC_OBJECT = Arity.EMPTY.newInstance().getClass();

	private static final Kryo KRYO = getKryo();

	private static Kryo getKryo() {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.setReferences(true);
		kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
		kryo.setDefaultSerializer((k, type) -> {
			if (Node.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				Class<? extends Node> nodeClass = (Class<? extends Node>) type;
				if (type.getName().endsWith("Gen")) {
					return new DSLNodeSerializer(nodeClass);
				} else {
					return new NodeSerializer(nodeClass);
				}
			} else {
				return new FieldSerializer<>(k, type);
			}
		});

		// atoms
		kryo.register(String.class, new StringSerializer());

		// procs
		kryo.register(SHAPE, new ShapeSerializer());
		kryo.register(DYNAMIC_OBJECT, new DynamicObjectSerializer());

		kryo.register(OzProc.class, new OzProcSerializer());
		kryo.register(ROOT_CALL_TARGET, new RootCallTargetSerializer());
		kryo.register(OzRootNode.class, new OzRootNodeSerializer());

		// nodes
		kryo.register(OzNode[].class);
		kryo.register(SequenceNode.class);
		kryo.register(InitializeArgNode.class);
		kryo.register(InitializeVarNode.class);
		kryo.register(InitializeTmpNode.class);
		kryo.register(ReadArgumentNode.class);

		kryo.register(WriteFrameSlotNodeGen.class);
		kryo.register(ReadFrameSlotNodeGen.class);
		kryo.register(CallProcNodeGen.class);

		kryo.register(ReadLocalVariableNode.class);
		kryo.register(ReadCapturedVariableNode.class);
		kryo.register(WriteCapturedVariableNode.class);
		kryo.register(ProcDeclarationNode.class);
		kryo.register(ListLiteralNode.class);
		kryo.register(RecordLiteralNode.class);
		kryo.register(MakeDynamicRecordNode.class);

		kryo.register(SkipNode.class);
		kryo.register(UnboundLiteralNode.class);
		kryo.register(IfNode.class);
		kryo.register(TryNode.class);
		kryo.register(FailNodeGen.class);
		kryo.register(RaiseNodeGen.class);
		kryo.register(NoElseNode.class);
		kryo.register(AndNode.class);

		kryo.register(BindNodeGen.class);
		kryo.register(PatternMatchEqualNodeGen.class);
		kryo.register(PatternMatchConsNodeGen.class);
		kryo.register(PatternMatchRecordNodeGen.class);
		kryo.register(PatternMatchOpenRecordNodeGen.class);

		kryo.register(DerefNodeGen.class);
		kryo.register(DotNodeGen.class);
		kryo.register(EqualNodeGen.class);
		kryo.register(HeadNodeGen.class);
		kryo.register(TailNodeGen.class);

		kryo.register(LiteralNode.class);
		kryo.register(BooleanLiteralNode.class);
		kryo.register(LongLiteralNode.class);
		kryo.register(ConsLiteralNodeGen.class);

		// sources
		Source fileSource = sourceFromPath(Loader.INIT_FUNCTOR);
		kryo.register(fileSource.getClass(), new FileSourceSerializer());
		kryo.register(SourceSection.class);
		kryo.register(String[].class);

		// frames
		kryo.register(FrameSlot.class, new FrameSlotSerializer());
		kryo.register(FrameDescriptor.class, new FrameDescriptorSerializer());
		kryo.register(MATERIALIZED_FRAME, new FrameSerializer());
		kryo.register(Object[].class);
		kryo.register(long[].class);
		kryo.register(byte[].class);

		// values
		kryo.register(Arity.class);
		kryo.register(OzLanguage.class, new SingletonSerializer(OzLanguage.INSTANCE));
		kryo.register(Unit.class, new SingletonSerializer(Unit.INSTANCE));
		kryo.register(OzVar.class, new OzVarSerializer());
		kryo.register(OzName.class);
		kryo.register(OzUniqueName.class, new OzUniqueNameSerializer());
		kryo.register(OzCons.class);
		kryo.register(OzChunk.class);
		kryo.register(OzCell.class);
		kryo.register(OzDict.class);
		kryo.register(OzObject.class);

		// I/O
		kryo.register(PrintStream.class, new PrintStreamSerializer());
		kryo.register(System.in.getClass(), new InputStreamSerializer());

		return kryo;
	}

	private static Source sourceFromPath(String path) throws Error {
		try {
			return Source.fromFileName(path);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public static void serialize(Object object, String path) {
		try (Output output = new Output(new FileOutputStream(path))) {
			KRYO.writeClassAndObject(output, object);
		} catch (FileNotFoundException e) {
			throw new Error(e);
		} catch (Throwable e) {
			new File(path).delete();
			throw e;
		}
	}

	public static <T> T deserialize(String path, Class<T> klass) {
		try (Input input = new Input(new FileInputStream(path))) {
			Object value = KRYO.readClassAndObject(input);
			return klass.cast(value);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
