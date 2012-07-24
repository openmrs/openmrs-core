/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to provide a few extra options to the API logging advice. This annotation is only
 * needed if a specific method needs some extra information (like if you want to limit the output of
 * this method when being logged because of size concerns or security concerns).<br/>
 * <br/>
 * To use, simply place the annotation before a method declaration in its interface:
 * 
 * <pre>
 *    &#064;Logging(ignoreAllArgumentValues=true)
 *    public SomeString setSomeLargeString(String aVeryLargeString);
 *    
 *    &#064;Logging(ignoreAllArgumentValues=true)
 *    public User saveUser(User userToSave, String password);
 *    
 *    &#064;Logging(ignoredArgumentIndexes={1})
 *    public User saveUser(User userToSave, String password);
 *  </pre>
 * 
 * @see org.openmrs.aop.LoggingAdvice
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Logging {
	
	/**
	 * If set to true, suppress any automatic OpenMRS logging of this method (E.g. you will no
	 * longer see "In method XyzService.saveXyz...".) Defaults to false.
	 * 
	 * @return boolean true/false to suppress OpenMRS's automatic logging of this method
	 * @since 1.8
	 */
	public boolean ignore() default false;
	
	/**
	 * If set to true, the annotated method will not print out the contents of the arguments every
	 * time this method is accessed and logged. Defaults to false
	 * 
	 * @return boolean true/false to ignore the argument content when logging
	 */
	public boolean ignoreAllArgumentValues() default false;
	
	/**
	 * This list should set the argument indexes that should not be printed. This is useful if one
	 * of the arguments/parameters is more sensitive than others. Note: This parameter does not need
	 * defined if the <code>Logging</code> annotation has set {@link #ignoreAllArgumentValues()} to
	 * true
	 * 
	 * @return list of argument indexes to ignore. (first argument is 0, second is 1, etc)
	 */
	public int[] ignoredArgumentIndexes() default {};
	
}
