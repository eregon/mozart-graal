package org.mozartoz.truffle.runtime;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.mozartoz.truffle.nodes.OzGuards;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.object.Layout;
import com.oracle.truffle.api.object.Location;
import com.oracle.truffle.api.object.LocationModifier;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.object.Shape.Allocator;

public class Arity {

	private static final Shape EMPTY = Layout.createLayout().createShape(RecordObjectType.INSTANCE);
	private static final Allocator ALLOCATOR = EMPTY.getLayout().createAllocator();
	private static final HiddenKey LABEL_KEY = new HiddenKey("label");
	public static final Location LABEL_LOCATION = ALLOCATOR.locationForType(Object.class,
			EnumSet.of(LocationModifier.Final, LocationModifier.NonNull));
	public static final Property LABEL_PROPERTY = Property.create(LABEL_KEY, LABEL_LOCATION, 0);

	public static final Shape BASE = EMPTY.addProperty(LABEL_PROPERTY);

	public static final Arity CONS_ARITY = Arity.build("|", 1L, 2L);
	static final Shape CONS_SHAPE = CONS_ARITY.getShape();

	public static final Arity PAIR_ARITY = Arity.build("#", 1L, 2L);

	private final Object label;
	private final Shape shape;

	private Arity(Object label, Shape shape) {
		assert OzGuards.isLiteral(label);
		this.label = label;
		this.shape = shape;
	}

	public Object getLabel() {
		return label;
	}

	public Shape getShape() {
		return shape;
	}

	public int getWidth() {
		return shape.getPropertyCount();
	}

	public boolean isConsArity() {
		return label == "|" && getShape() == CONS_SHAPE;
	}

	public boolean isTupleArity() {
		int width = getWidth();
		for (int i = 1; i <= width; i++) {
			long feature = (long) i;
			if (!(shape.hasProperty(feature))) {
				return false;
			}
		}
		return true;
	}

	public boolean matches(DynamicObject object) {
		return object.getShape() == shape && LABEL_LOCATION.get(object) == label;
	}

	public boolean matchesOpen(DynamicObject object) {
		Shape a = object.getShape();
		Shape b = shape;
		return a.isRelated(b) && a.getPropertyCount() >= b.getPropertyCount() && isAncestorShape(a, b);
	}

	private boolean isAncestorShape(Shape shape, Shape ancestor) {
		while (shape != null) {
			if (shape == ancestor) {
				return true;
			}
			shape = shape.getParent();
		}
		return false;
	}

	public DynamicObjectFactory createFactory() {
		return shape.createFactory();
	}

	public Object asOzList() {
		Object features = "nil";
		for (Property property : shape.getPropertyListInternal(false)) {
			if (!property.isHidden()) {
				features = new OzCons(property.getKey(), features);
			}
		}
		return features;
	}

	private static final List<Class<?>> FEATURES_TYPES_ORDER = Arrays.asList(
			Long.class,
			BigInteger.class,
			String.class,
			OzUniqueName.class,
			OzName.class
	);

	private static final Comparator<Object> COMPARE_FEATURES = new Comparator<Object>() {
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object a, Object b) {
			if (a.getClass() == b.getClass()) {
				return ((Comparable<Object>) a).compareTo((Comparable<Object>) b);
			} else {
				assert FEATURES_TYPES_ORDER.contains(a.getClass());
				assert FEATURES_TYPES_ORDER.contains(b.getClass());
				return Integer.compare(
						FEATURES_TYPES_ORDER.indexOf(a.getClass()),
						FEATURES_TYPES_ORDER.indexOf(b.getClass()));
			}
		}
	};

	public static void sortFeaturesInPlace(Object[] features) {
		Arrays.sort(features, COMPARE_FEATURES);
	}

	public static Object[] sortFeatures(Object[] features) {
		Object[] sorted = features.clone();
		sortFeaturesInPlace(sorted);
		return sorted;
	}

	private static boolean isSortedFeatures(Object[] features) {
		return Arrays.equals(features, sortFeatures(features));
	}

	private static final Object SOME_OBJECT = new Object();

	public static Arity build(Object label, Object... features) {
		assert isSortedFeatures(features);
		Shape shape = Arity.BASE;
		for (Object feature : features) {
			assert OzGuards.isFeature(feature);
			shape = shape.defineProperty(feature, SOME_OBJECT, 0);
		}
		return new Arity(label, shape);
	}

	public static Arity forAtom(String atom) {
		return new Arity(atom, BASE);
	}

	public static Arity forRecord(DynamicObject record) {
		return new Arity(LABEL_LOCATION.get(record), record.getShape());
	}

}
