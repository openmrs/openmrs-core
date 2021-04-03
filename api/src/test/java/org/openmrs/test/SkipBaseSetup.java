/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skips over the {@link BaseContextSensitiveTest#baseSetupWithStandardDataAndAuthentication()}
 * method when applied to "@Test" methods. This allows classes that extend
 * {@link BaseContextSensitiveTest} to not be forced into running
 * baseSetupWithStandardDataAndAuthentication(). This magic happens because of the
 * {@link SkipBaseSetupAnnotationExecutionListener} that is registered on the
 * {@link BaseContextSensitiveTest}
 * 
 * @see BaseContextSensitiveTest
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
public @interface SkipBaseSetup {

}
