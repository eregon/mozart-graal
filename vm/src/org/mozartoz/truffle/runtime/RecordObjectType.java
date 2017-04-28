package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Property;

public class RecordObjectType extends ObjectType {

	public static final RecordObjectType INSTANCE = new RecordObjectType();

	@Override
	public ForeignAccess getForeignAccessFactory(DynamicObject object) {
		return RecordMessageResolutionForeign.ACCESS;
	}

	@Override
	public boolean equals(DynamicObject object, Object other) {
		throw new UnsupportedOperationException("Record and DynamicObject have structural equality");
	}

	@Override
	public String toString(DynamicObject record) {
		Object label = Arity.LABEL_LOCATION.get(record);
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
