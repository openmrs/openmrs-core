/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Spring Java-based configuration class for initializing test-specific components
 * in the OpenMRS testing context.
 * <p>
 * This configuration class bootstraps the testing environment by:
 * </p>
 * <ul>
 *   <li>Importing test-scoped Java beans via {@code @Import}, including:
 *     <ul>
 *       <li>{@code Listener1} and {@code Listener2} for test implementations of {@code PrivilegeListener}</li>
 *       <li>{@code NonServiceTestBeanImpl} and {@code TimestampOrderNumberGenerator}
 *       <li>{@code TestUserSessionListener} which tracks session state during test authentication</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @since 3.0.0
 */
@Configuration
@ImportResource({
	"classpath*:TestingApplicationContext.xml",
	"classpath*:AltAuthSchemeTestingApplicationContext.xml",
})
@Import({
	org.openmrs.aop.AuthorizationAdviceTest.Listener1.class,
	org.openmrs.aop.AuthorizationAdviceTest.Listener2.class,
	org.openmrs.api.db.ContextDAOTest.TestUserSessionListener.class,
	org.openmrs.api.cache.NonServiceTestBeanImpl.class,
	org.openmrs.orders.TimestampOrderNumberGenerator.class
})
public class OpenmrsTestConfig {
}
