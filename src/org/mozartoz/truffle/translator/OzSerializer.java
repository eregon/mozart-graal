package org.mozartoz.truffle.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.mozartoz.truffle.nodes.DerefIfBoundNode;
import org.mozartoz.truffle.nodes.DerefIfBoundNodeGen;
import org.mozartoz.truffle.nodes.DerefNode;
import org.mozartoz.truffle.nodes.DerefNodeGen;
import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.nodes.OzRootNode;
import org.mozartoz.truffle.nodes.builtins.BuiltinsManager;
import org.mozartoz.truffle.nodes.builtins.ListBuiltins.HeadNode;
import org.mozartoz.truffle.nodes.builtins.ListBuiltins.TailNode;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.HeadNodeGen;
import org.mozartoz.truffle.nodes.builtins.ListBuiltinsFactory.TailNodeGen;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.DotNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltins.EqualNode;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.DotNodeFactory.DotNodeGen;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory;
import org.mozartoz.truffle.nodes.builtins.ValueBuiltinsFactory.EqualNodeFactory.EqualNodeGen;
import org.mozartoz.truffle.nodes.call.CallProcNode;
import org.mozartoz.truffle.nodes.call.CallProcNodeGen;
import org.mozartoz.truffle.nodes.call.ExecuteValuesNode;
import org.mozartoz.truffle.nodes.call.ReadArgumentNode;
import org.mozartoz.truffle.nodes.control.AndNode;
import org.mozartoz.truffle.nodes.control.IfNode;
import org.mozartoz.truffle.nodes.control.SequenceNode;
import org.mozartoz.truffle.nodes.control.SkipNode;
import org.mozartoz.truffle.nodes.control.TryNode;
import org.mozartoz.truffle.nodes.literal.BooleanLiteralNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNode;
import org.mozartoz.truffle.nodes.literal.ConsLiteralNodeGen;
import org.mozartoz.truffle.nodes.literal.LiteralNode;
import org.mozartoz.truffle.nodes.literal.LongLiteralNode;
import org.mozartoz.truffle.nodes.literal.ProcDeclarationNode;
import org.mozartoz.truffle.nodes.literal.RecordLiteralNode;
import org.mozartoz.truffle.nodes.local.BindNode;
import org.mozartoz.truffle.nodes.local.BindNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeArgNode;
import org.mozartoz.truffle.nodes.local.InitializeArgNodeGen;
import org.mozartoz.truffle.nodes.local.InitializeVarNode;
import org.mozartoz.truffle.nodes.local.ReadCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNode;
import org.mozartoz.truffle.nodes.local.ReadFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.local.ReadLocalVariableNode;
import org.mozartoz.truffle.nodes.local.WriteCapturedVariableNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNode;
import org.mozartoz.truffle.nodes.local.WriteFrameSlotNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchCaptureNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchCaptureNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchConsNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchEqualNodeGen;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNode;
import org.mozartoz.truffle.nodes.pattern.PatternMatchRecordNodeGen;
import org.mozartoz.truffle.runtime.Arity;
import org.mozartoz.truffle.runtime.OzChunk;
import org.mozartoz.truffle.runtime.OzCons;
import org.mozartoz.truffle.runtime.OzLanguage;
import org.mozartoz.truffle.runtime.OzName;
import org.mozartoz.truffle.runtime.OzProc;
import org.mozartoz.truffle.runtime.OzUniqueName;
import org.mozartoz.truffle.runtime.OzVar;
import org.mozartoz.truffle.runtime.Unit;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
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
				Object key = property.getKey();
				Object value = kryo.readClassAndObject(input);
				if (key.toString().startsWith("AAAA")) {
					System.out.println("HERE " + key);
					System.out.println(value);
				}
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

	private static class SingleSerializer<N, T> extends Serializer<N> {

		private final Function<N, T> get;
		private final Function<T, N> create;

		public SingleSerializer(Function<N, T> get, Function<T, N> create) {
			this.get = get;
			this.create = create;
		}

		public void write(Kryo kryo, Output output, N node) {
			kryo.writeClassAndObject(output, get.apply(node));
		}

		@SuppressWarnings("unchecked")
		public N read(Kryo kryo, Input input, Class<N> type) {
			T value = (T) kryo.readClassAndObject(input);
			return create.apply(value);
		}
	}

	private static class DoubleSerializer<N, T, U> extends Serializer<N> {

		private final Function<N, T> get1;
		private final Function<N, U> get2;
		private final BiFunction<T, U, N> create;

		public DoubleSerializer(Function<N, T> get1, Function<N, U> get2, BiFunction<T, U, N> create) {
			this.get1 = get1;
			this.get2 = get2;
			this.create = create;
		}

		public void write(Kryo kryo, Output output, N node) {
			kryo.writeClassAndObject(output, get1.apply(node));
			kryo.writeClassAndObject(output, get2.apply(node));
		}

		@SuppressWarnings("unchecked")
		public N read(Kryo kryo, Input input, Class<N> type) {
			T value1 = (T) kryo.readClassAndObject(input);
			U value2 = (U) kryo.readClassAndObject(input);
			return create.apply(value1, value2);
		}
	}

	@FunctionalInterface
	public static interface TriFunction<T, U, V, R> {
		R apply(T t, U u, V v);
	}

	public static class TripleSerializer<N, T, U, V> extends Serializer<N> {

		private final Function<N, T> get1;
		private final Function<N, U> get2;
		private final Function<N, V> get3;
		private final TriFunction<T, U, V, N> create;

		public TripleSerializer(Function<N, T> get1, Function<N, U> get2, Function<N, V> get3, TriFunction<T, U, V, N> create) {
			this.get1 = get1;
			this.get2 = get2;
			this.get3 = get3;
			this.create = create;
		}

		public void write(Kryo kryo, Output output, N node) {
			kryo.writeClassAndObject(output, get1.apply(node));
			kryo.writeClassAndObject(output, get2.apply(node));
			kryo.writeClassAndObject(output, get3.apply(node));
		}

		@SuppressWarnings("unchecked")
		public N read(Kryo kryo, Input input, Class<N> type) {
			T value1 = (T) kryo.readClassAndObject(input);
			U value2 = (U) kryo.readClassAndObject(input);
			V value3 = (V) kryo.readClassAndObject(input);
			return create.apply(value1, value2, value3);
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

	private static class SkipNodeSerializer extends Serializer<SkipNode> {
		public void write(Kryo kryo, Output output, SkipNode object) {
		}

		public SkipNode read(Kryo kryo, Input input, Class<SkipNode> type) {
			return new SkipNode();
		}
	}

	private static final TruffleRuntime TRUFFLE = Truffle.getRuntime();
	private static final Class<? extends VirtualFrame> VIRTUAL_FRAME =
			TRUFFLE.createVirtualFrame(new Object[0], new FrameDescriptor()).getClass();
	private static final Class<? extends MaterializedFrame> MATERIALIZED_FRAME =
			TRUFFLE.createMaterializedFrame(new Object[0]).getClass();
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

		kryo.register(String.class, new StringSerializer());

		kryo.register(SHAPE, new ShapeSerializer());
		kryo.register(DYNAMIC_OBJECT, new DynamicObjectSerializer());

		kryo.register(OzProc.class, new OzProcSerializer());
		kryo.register(ROOT_CALL_TARGET, new RootCallTargetSerializer());
		kryo.register(OzRootNode.class, new OzRootNodeSerializer());

		// nodes
		kryo.register(OzNode[].class);
		kryo.register(SequenceNode.class, new SingleSerializer<>(
				SequenceNode::getStatements, SequenceNode::createFrom));
		kryo.register(InitializeArgNodeGen.class,
				new DoubleSerializer<>(InitializeArgNode::getSlot, InitializeArgNode::getVar, InitializeArgNodeGen::create));
		kryo.register(InitializeVarNode.class,
				new SingleSerializer<>(InitializeVarNode::getSlot, InitializeVarNode::new));
		kryo.register(ReadArgumentNode.class, new SingleSerializer<>(
				ReadArgumentNode::getIndex, ReadArgumentNode::new));

		kryo.register(FrameSlot.class, new FrameSlotSerializer());
		kryo.register(WriteFrameSlotNodeGen.class,
				new SingleSerializer<>(WriteFrameSlotNode::getSlot, WriteFrameSlotNodeGen::create));
		kryo.register(ReadFrameSlotNodeGen.class,
				new SingleSerializer<>(ReadFrameSlotNode::getSlot, ReadFrameSlotNodeGen::create));
		kryo.register(CallProcNodeGen.class,
				new DoubleSerializer<>(CallProcNode::getArguments, CallProcNode::getFunction, CallProcNodeGen::create));

		kryo.register(ExecuteValuesNode.class);
		kryo.register(ReadLocalVariableNode.class, new SingleSerializer<>(
				ReadLocalVariableNode::getSlot, ReadLocalVariableNode::new));
		kryo.register(ReadCapturedVariableNode.class, new DoubleSerializer<>(
				ReadCapturedVariableNode::getSlot, ReadCapturedVariableNode::getDepth, ReadCapturedVariableNode::new));
		kryo.register(WriteCapturedVariableNode.class, new DoubleSerializer<>(
				WriteCapturedVariableNode::getSlot, WriteCapturedVariableNode::getDepth, WriteCapturedVariableNode::new));
		kryo.register(DerefNodeGen.class, new SingleSerializer<>(
				DerefNode::getValue, DerefNodeGen::create));
		kryo.register(DerefIfBoundNodeGen.class, new SingleSerializer<>(
				DerefIfBoundNode::getValue, DerefIfBoundNodeGen::create));
		kryo.register(ProcDeclarationNode.class, new SingleSerializer<>(
				ProcDeclarationNode::getCallTarget, ProcDeclarationNode::new));
		kryo.register(RecordLiteralNode.class, new DoubleSerializer<>(
				RecordLiteralNode::getArity, RecordLiteralNode::getValues, RecordLiteralNode::new));

		kryo.register(BindNodeGen.class, new TripleSerializer<>(
				BindNode::getWriteLeft, BindNode::getLeft, BindNode::getRight, BindNodeGen::create));

		kryo.register(SkipNode.class, new SkipNodeSerializer());
		kryo.register(IfNode.class,
				new TripleSerializer<>(IfNode::getCondition, IfNode::getThenExpr, IfNode::getElseExpr, IfNode::new));
		kryo.register(TryNode.class, new TripleSerializer<>(
				TryNode::getWriteExceptionVarNode, TryNode::getBody, TryNode::getCatchBody, TryNode::new));
		kryo.register(AndNode.class, new SingleSerializer<>(AndNode::getConditions, AndNode::new));
		kryo.register(PatternMatchCaptureNodeGen.class, new DoubleSerializer<>(
				PatternMatchCaptureNode::getVar, PatternMatchCaptureNode::getValue, PatternMatchCaptureNodeGen::create));
		kryo.register(PatternMatchEqualNodeGen.class, new DoubleSerializer<>(
				PatternMatchEqualNode::getConstant, PatternMatchEqualNode::getValue, PatternMatchEqualNodeGen::create));
		kryo.register(PatternMatchConsNodeGen.class, new SingleSerializer<>(
				PatternMatchConsNode::getValue, PatternMatchConsNodeGen::create));
		kryo.register(PatternMatchRecordNodeGen.class, new DoubleSerializer<>(
				PatternMatchRecordNode::getArity, PatternMatchRecordNode::getValue, PatternMatchRecordNodeGen::create));

		kryo.register(DotNodeGen.class, new DoubleSerializer<>(
				DotNode::getRecord, DotNode::getFeature, DotNodeFactory::create));
		kryo.register(EqualNodeGen.class, new DoubleSerializer<>(
				EqualNode::getLeft, EqualNode::getRight, EqualNodeFactory::create));
		kryo.register(HeadNodeGen.class, new SingleSerializer<>(HeadNode::getCons, HeadNodeGen::create));
		kryo.register(TailNodeGen.class, new SingleSerializer<>(TailNode::getCons, TailNodeGen::create));

		Source fileSource = sourceFromPath(Loader.INIT_FUNCTOR);
		kryo.register(fileSource.getClass(), new SingleSerializer<>(Source::getName, OzSerializer::sourceFromPath));
		kryo.register(SourceSection.class);
		kryo.register(String[].class);

		kryo.register(FrameDescriptor.class, new FrameDescriptorSerializer());

		kryo.register(VIRTUAL_FRAME);
		kryo.register(MATERIALIZED_FRAME);
		kryo.register(Object[].class);
		kryo.register(long[].class);
		kryo.register(byte[].class);

		kryo.register(LiteralNode.class, new SingleSerializer<>(LiteralNode::getValue, LiteralNode::new));
		kryo.register(BooleanLiteralNode.class, new SingleSerializer<>(BooleanLiteralNode::getValue, BooleanLiteralNode::new));
		kryo.register(LongLiteralNode.class, new SingleSerializer<>(LongLiteralNode::getValue, LongLiteralNode::new));
		kryo.register(ConsLiteralNodeGen.class, new DoubleSerializer<>(
				ConsLiteralNode::getHead, ConsLiteralNode::getTail, ConsLiteralNodeGen::create));

		kryo.register(Arity.class);
		kryo.register(OzLanguage.class, new SingletonSerializer(OzLanguage.INSTANCE));
		kryo.register(Unit.class, new SingletonSerializer(Unit.INSTANCE));
		kryo.register(OzVar.class, new OzVarSerializer());
		kryo.register(OzName.class);
		kryo.register(OzUniqueName.class, new OzUniqueNameSerializer());
		kryo.register(OzCons.class);
		kryo.register(OzChunk.class);

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
		} catch (KryoException e) {
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
