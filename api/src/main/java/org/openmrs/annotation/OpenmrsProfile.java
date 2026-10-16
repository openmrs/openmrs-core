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

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Place it on classes which you want to be beans created conditionally based on OpenMRS version
 * and/or started modules.
 * <p>
 * This annotation is self-enforcing: it meta-annotates {@link Component} so that the class is
 * picked up by any default-filter component-scan, and {@link Conditional} so that the bean is only
 * registered when the profile actually matches — regardless of which component-scan discovers it.
 *
 * @since 1.10, 1.9.8, 1.8.5, 1.7.5
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component // makes bare @OpenmrsProfile a scan stereotype
@Conditional(OpenmrsProfileCondition.class) // gates registration in every scan / @Bean / @Import
public @interface OpenmrsProfile {

	/**
	 * Optional bean name, equivalent to {@link Component#value()}. Use this when
	 * {@code @OpenmrsProfile} is the only stereotype on the class. If the class also carries another
	 * stereotype annotation with a non-empty value, the two names must agree; a conflict will cause a
	 * Spring startup exception.
	 *
	 * @since 3.0.0
	 */
	String value() default "";

	/**
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
	String openmrsPlatformVersion() default "";

	String[] modules() default {};
}
