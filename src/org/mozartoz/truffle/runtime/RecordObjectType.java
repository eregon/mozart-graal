package org.mozartoz.truffle.runtime;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Property;

public class RecordObjectType extends ObjectType {

	public static final RecordObjectType INSTANCE = new RecordObjectType();

	@Override
	public String toString(DynamicObject record) {
		Object label = OzArity.LABEL_LOCATION.get(record);
		StringBuilder builder = new StringBuilder();
		builder.append(label).append('(');
		for (Property property : record.getShape().getProperties()) {
			Object value = property.get(record, record.getShape());
			builder.append(property.getKey()).append(':').append(value).append(' ');
		}
		builder.setCharAt(builder.length() - 1, ')');
		return builder.toString();
	}

}
