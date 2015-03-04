/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.springframework.test.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to indicate that a method is <i>not transactional</i>.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 2.0
 * @deprecated as of Spring 3.0, in favor of moving the non-transactional test
 * method to a separate (non-transactional) test class or to a
 * {@link org.springframework.test.context.transaction.BeforeTransaction
 * &#64;BeforeTransaction} or
 * {@link org.springframework.test.context.transaction.AfterTransaction
 * &#64;AfterTransaction} method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface NotTransactional {}
