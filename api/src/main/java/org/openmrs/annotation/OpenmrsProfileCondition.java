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

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Spring {@link Condition} that gates bean registration on whether the current runtime matches the
 * OpenMRS platform version and/or module requirements declared in {@link OpenmrsProfile}.
 * <p>
 * The matching logic is intentionally delegated to
 * {@link OpenmrsProfileExcludeFilter#matchOpenmrsProfileAttributes(Map)} so that version/module
 * semantics remain centralised and are not duplicated here.
 *
 * @since 2.8
 * @see OpenmrsProfile
 * @see OpenmrsProfileExcludeFilter
 */
public class OpenmrsProfileCondition implements Condition {

	/**
	 * Returns {@code true} (allow registration) when the runtime profile satisfies all constraints
	 * declared on {@link OpenmrsProfile}, and {@code false} otherwise.
	 * <p>
	 * If the annotation attributes cannot be read for any reason the condition defaults to {@code true}
	 * so as not to block beans that carry no meaningful constraint.
	 */
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(OpenmrsProfile.class.getName());

		if (annotationAttributes == null) {
			// @OpenmrsProfile not present on this metadata; don't block registration
			return true;
		}

		// Build the attribute map in the exact shape that matchOpenmrsProfileAttributes() expects:
		// "openmrsPlatformVersion" -> String, "modules" -> String[]
		Map<String, Object> profile = new HashMap<>();
		profile.put("openmrsPlatformVersion", annotationAttributes.getOrDefault("openmrsPlatformVersion", ""));
		profile.put("modules", annotationAttributes.getOrDefault("modules", new String[0]));

		return OpenmrsProfileExcludeFilter.matchOpenmrsProfileAttributes(profile);
	}
}
