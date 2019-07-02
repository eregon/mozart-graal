package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(value = RecordLibrary.class, receiverType = DynamicObject.class)
public class RecordObjectType extends ObjectType {

	public static final RecordObjectType INSTANCE = new RecordObjectType();

	@ExportMessage
	static boolean isRecord(DynamicObject record) {
		return true;
	}

	@ExportMessage
	static Object label(DynamicObject record) {
		return OzRecord.getLabel(record);
	}

	@ExportMessage
	static Arity arity(DynamicObject record) {
		return OzRecord.getArity(record);
	}

	@ExportMessage
	static Object arityList(DynamicObject record) {
		return OzRecord.getArity(record).asOzList();
	}

	@ExportMessage static class HasFeature {
		@Specialization(guards = {
				"feature == cachedFeature",
				"record.getShape() == cachedShape"
		})
		static boolean cached(DynamicObject record, Object feature,
							 @Cached("feature") Object cachedFeature,
							 @Cached("record.getShape()") Shape cachedShape,
							 @Cached("cachedShape.hasProperty(cachedFeature)") boolean hasProperty) {
			return hasProperty;
		}

		@Specialization(replaces = "cached")
		static boolean uncached(DynamicObject record, Object feature) {
			return record.containsKey(feature);
		}
	}

	@ExportMessage static class Read {
		@Specialization(guards = {
				"feature == cachedFeature",
				"record.getShape() == cachedShape"
		})
		static Object cached(DynamicObject record, Object feature, Node node,
				@Cached("feature") Object cachedFeature,
				@Cached("record.getShape()") Shape cachedShape,
				@Cached("cachedShape.getProperty(cachedFeature)") Property property) {
			if (property != null) {
				return property.get(record, cachedShape);
			} else {
				throw Errors.noFieldError(node, record, cachedFeature);
			}
		}

		@Specialization(replaces = "cached")
		static Object uncached(DynamicObject record, Object feature, Node node) {
			Object value = record.get(feature);
			if (value == null) {
				assert !record.containsKey(feature);
				throw Errors.noFieldError(node, record, feature);
			}
			return value;
		}
	}

	@ExportMessage static class ReadOrDefault {
		@Specialization(guards = {
				"feature == cachedFeature",
				"record.getShape() == cachedShape"
		})
		static Object cached(DynamicObject record, Object feature, Object defaultValue,
							 @Cached("feature") Object cachedFeature,
							 @Cached("record.getShape()") Shape cachedShape,
							 @Cached("cachedShape.getProperty(cachedFeature)") Property property) {
			if (property != null) {
				return property.get(record, cachedShape);
			} else {
				return defaultValue;
			}
		}

		@Specialization(replaces = "cached")
		static Object uncached(DynamicObject record, Object feature, Object defaultValue) {
			return record.get(feature, defaultValue);
		}
	}

	@Override
	public Class<?> dispatch() {
		return RecordObjectType.class;
	}

	@Override
	public boolean equals(DynamicObject object, Object other) {
		throw new UnsupportedOperationException("Record and DynamicObject have structural equality");
	}

	@Override
	public String toString(DynamicObject record) {
		Object label = OzRecord.getLabel(record);
		StringBuilder builder = new StringBuilder();
		Arity arity = OzRecord.getArity(record);

		if (label == "#" && arity.isTupleArity() && arity.getWidth() >= 2) {
			int width = arity.getWidth();
			for (long i = 1; i <= width; i++) {
				builder.append(record.get(i));
				if (i != width) {
					builder.append("#");
				}
			}
		} else {
			builder.append(label).append('(');
			for (Property property : record.getShape().getProperties()) {
				Object value = property.get(record, record.getShape());
				builder.append(property.getKey()).append(':').append(value).append(' ');
			}
			builder.setCharAt(builder.length() - 1, ')');
		}

		return builder.toString();
	}

}
