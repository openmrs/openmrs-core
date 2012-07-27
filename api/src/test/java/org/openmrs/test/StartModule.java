package org.openmrs.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Putting this annotation on a method will start the listed modules.
 * 
 * @see BaseContextSensitiveTest
 * @see StartModuleExecutionListener
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface StartModule {
	
	/**
	 * A list of full paths to omods.
	 * 
	 * @return a list of omods to start up
	 */
	String[] value();
}
