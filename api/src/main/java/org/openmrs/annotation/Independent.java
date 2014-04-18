package org.openmrs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import org.openmrs.aop.RequiredDataAdvice;

/**
 * Annotation used to indicate that a field of an OpenmrsObject is independent
 * of that class.
 *
 * @see RequiredDataAdvice
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Independent {

}
