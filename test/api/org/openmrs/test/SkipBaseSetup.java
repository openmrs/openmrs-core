package org.openmrs.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skips over the {@link BaseContextSensitiveTest#baseSetupWithStandardDataAndAuthentication()}
 * method when applied to "@Test" methods.
 * 
 * This allows classes that extend {@link BaseContextSensitiveTest} to 
 * not be forced into running baseSetupWithStandardDataAndAuthentication().
 * 
 * This magic happens because of the {@link SkipBaseSetupAnnotationExecutionListener}
 * that is registered on the {@link BaseContextSensitiveTest}
 * 
 * @see BaseContextSensitiveTest
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SkipBaseSetup {
	
}
