/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.attribute.handler;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;

/**
 * This is a superclass for FieldGenDatatypeHandlers for OpenmrsObjects
 * 
 * @since 1.12
 */
public abstract class SerializingFieldGenDatatypeHandler<DT extends SerializingCustomDatatype<T>, T> implements FieldGenDatatypeHandler<DT, T> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, java.lang.String)
	 * @should return the correct typed value
	 */
	@Override
	public T getValue(DT datatype, HttpServletRequest request, String formFieldName) throws InvalidCustomValueException {
		String formFieldValue = request.getParameter(formFieldName);
		if (StringUtils.isBlank(formFieldValue)) {
			return null;
		}
		return datatype.deserialize(formFieldValue);
	}
}
