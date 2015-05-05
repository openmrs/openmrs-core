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
