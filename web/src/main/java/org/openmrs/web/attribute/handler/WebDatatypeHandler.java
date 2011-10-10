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

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.InvalidCustomValueException;


/**
 * A web-layer extension of {@link CustomDatatypeHandler}, which generates the HTML for a widget, and handles the submission of that widget 
 */
public interface WebDatatypeHandler<DT extends CustomDatatype<T>, T> extends CustomDatatypeHandler<DT, T> {
	
	String getWidgetHtml(DT datatype, String formFieldName, T startingValue);
	
	T getValue(DT datatype, HttpServletRequest request, String formFieldName) throws InvalidCustomValueException;
	
}
