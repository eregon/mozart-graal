package org.mozartoz.truffle.runtime;

import java.util.EnumSet;

import com.oracle.truffle.api.object.DynamicObject;
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

	private final Object label;
	private final Shape shape;

	public Arity(Object label, Shape shape) {
		this.label = label;
		this.shape = shape;
	}

	public Object getLabel() {
		return label;
	}

	public Shape getShape() {
		return shape;
	}

	public boolean matches(DynamicObject object) {
		return object.getShape() == shape && LABEL_LOCATION.get(object) == label;
	}

	private static final Object SOME_OBJECT = new Object();

	public static Arity build(Object label, Object... features) {
		Shape shape = Arity.BASE;
		for (Object feature : features) {
			shape = shape.defineProperty(feature, SOME_OBJECT, 0);
		}
		return new Arity(label, shape);
	}

}
