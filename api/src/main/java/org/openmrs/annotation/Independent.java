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

import org.openmrs.aop.RequiredDataAdvice;

/**
 * In OpenMRS when the appropriate object  is retired/voided, all of its member collections are being recursively processed as well.
 * This isn't desired behavior in every single case: ex. Set&lt;LocationTag&gt; member in Location class.
 * Independent annotation is used to indicate that a particular member collection of an object shouldn't be
 * recursively handled, when the parent object is being retired/voided.
 *
 * @see RequiredDataAdvice#recursivelyHandle(Class, org.openmrs.OpenmrsObject, org.openmrs.User, java.util.Date, String, java.util.List)
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Independent {

}
