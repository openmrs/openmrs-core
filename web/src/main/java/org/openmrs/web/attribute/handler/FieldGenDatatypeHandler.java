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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.InvalidCustomValueException;

/**
 * A web-layer extension of a {@link CustomDatatypeHandler}, which also defines what fieldgen widget is used to
 * collect the values, and how to fetch them from a web request.
 * @since 1.9
 */
public interface FieldGenDatatypeHandler<DT extends CustomDatatype<T>, T> extends HtmlDisplayableDatatypeHandler<T> {
	
	/**
	 * @return the name of the fieldgen widget to be used to allow data entry for attribute types that
	 * use this handler
	 */
	String getWidgetName();
	
	/**
	 * @return extra configuration properties to be passed to the fieldgen widget
	 */
	Map<String, Object> getWidgetConfiguration();
	
	/**
	 * Fetches an attribute value from a web request, for a given form field name
	 * @param datatype
	 * @param request
	 * @param formFieldName
	 * @return
	 * @throws InvalidAttributeValueException if the submitted value is invalid
	 */
	T getValue(DT datatype, HttpServletRequest request, String formFieldName) throws InvalidCustomValueException;
	
}
