package se.l4.vibe.internal.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Expose a field or method as an attribute.
 *
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface ExposeAsAttribute
{
	/** Name of the attribute. */
	String value() default "";
}
