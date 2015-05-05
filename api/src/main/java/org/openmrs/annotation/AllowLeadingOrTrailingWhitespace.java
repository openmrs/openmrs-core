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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmrs.api.handler.OpenmrsObjectSaveHandler;

/**
 * Annotation used to indicate that a method allows strings with leading or
 * trailing whitespace.
 * 
 * <pre>
 * &#064;AllowLeadingOrTrailingWhitespace
 * public void setName(String name);
 * </pre>
 * 
 * Note: This annotation should be put onto the setter methods<br/>
 * <br/>
 * If this annotation is not present the {@link OpenmrsObjectSaveHandler} will
 * trim off whitespace before this object is persisted in the db.
 * 
 * @since 1.9
 * @see AllowEmptyStrings
 * @see OpenmrsObjectSaveHandler
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AllowLeadingOrTrailingWhitespace {

}
