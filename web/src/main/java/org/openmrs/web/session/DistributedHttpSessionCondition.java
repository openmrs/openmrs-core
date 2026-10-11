/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.session;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Resolves the {@code session.distributed} flag (off by default) from, in order: JVM system
 * property, {@code session.distributed} or {@code SESSION_DISTRIBUTED} env var, then OpenMRS
 * runtime properties.
 * <p>
 * Both consumers - the Spring {@code @Conditional} ({@link #matches}, gating the config beans) and
 * the servlet filter {@link org.openmrs.web.security.OpenmrsSecurityContextFilter}
 * ({@link #isEnabled()}) - resolve it the exact same way, so they can never disagree. In particular
 * {@link #matches} deliberately does NOT consult the Spring
 * {@link org.springframework.core.env.Environment}: a value visible only there (e.g. a servlet
 * context-param) would enable the config but not the filter, which would then push the
 * non-serializable {@code UserContext} into a replicated session.
 *
 * @since 3.0.0
 */
public class DistributedHttpSessionCondition implements Condition {

	static final String PROPERTY = "session.distributed";

	static final String ENV_PROPERTY = "SESSION_DISTRIBUTED";

	private static final Logger log = LoggerFactory.getLogger(DistributedHttpSessionCondition.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// Intentionally resolve()-only (not context.getEnvironment()), so this config gate matches the
		// servlet filter's isEnabled() exactly - see the class Javadoc.
		boolean enabled = isEnabled();
		log.debug("Distributed HTTP session backend {}", enabled ? "enabled" : "disabled");
		return enabled;
	}

	/**
	 * The same flag resolved outside a Spring {@code @Conditional} (for the plain servlet filter).
	 *
	 * @return true if the distributed HTTP session backend is enabled
	 * @since 3.0.0
	 */
	public static boolean isEnabled() {
		return Boolean.parseBoolean(resolve());
	}

	private static String resolve() {
		String value = System.getProperty(PROPERTY);
		if (value == null) {
			value = System.getenv(PROPERTY);
		}
		if (value == null) {
			value = System.getenv(ENV_PROPERTY);
		}
		if (value == null) {
			value = fromRuntimeProperties();
		}
		return value;
	}

	private static String fromRuntimeProperties() {
		try {
			return Context.getRuntimeProperties().getProperty(PROPERTY);
		} catch (Exception e) {
			// Context may not be initialised yet in some bootstrap/test paths; treat as disabled.
			return null;
		}
	}
}
