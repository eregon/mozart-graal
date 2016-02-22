package org.mozartoz.truffle.nodes.builtins;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Builtin {

	public static final Builtin DEFAULT = new Builtin() {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Builtin.class;
		}

		@Override
		public String name() {
			return "";
		}

		public boolean proc() {
			return false;
		};
	};

	String name() default "";

	boolean proc() default false;

}
