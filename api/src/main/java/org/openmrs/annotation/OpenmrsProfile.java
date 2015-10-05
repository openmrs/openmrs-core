/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Place it on classes which you want to be beans created conditionally based on
 * OpenMRS version and/or started modules.
 * 
 * @since 1.10, 1.9.8, 1.8.5, 1.7.5
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenmrsProfile {
	
	/**
	 * @deprecated Since 1.11.3, 1.10.2, 1.9.9 use {@link #openmrsPlatformVersion()}.
	 */
	@Deprecated
	public String openmrsVersion() default "";
	
	/**
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
	public String openmrsPlatformVersion() default "";
	
	public String[] modules() default {};
}
