/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.attribute.handler;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;

/**
 * This is a superclass for custom datatypes for data
 * 
 * @since 1.10
 */
public abstract class BaseOpenmrsFieldGenDatatypeHandler<T extends OpenmrsObject> implements FieldGenDatatypeHandler<SerializingCustomDatatype<T>, T> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public T getValue(SerializingCustomDatatype<T> datatype, HttpServletRequest request, String formFieldValue)
	    throws InvalidCustomValueException {
		String result = request.getParameter(formFieldValue);
		if (StringUtils.isBlank(result)) {
			return null;
		}
		return datatype.deserialize(formFieldValue);
	}
}
