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
package org.openmrs.web.attribute;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.web.attribute.handler.FieldGenAttributeHandler;

/**
 * Web-layer utility methods related to customizable {@link Attribute}s
 */
public class WebAttributeUtil {
	
	/**
	 * Gets the value of an attribute out of an HTTP request, treating it according to the appropriate handler type.
	 * 
	 * @param request
	 * @param handler
	 * @param paramName
	 * @return
	 */
	public static <T> T getValue(HttpServletRequest request, AttributeHandler<T> handler, String paramName) {
		if (handler instanceof FieldGenAttributeHandler) {
			return ((FieldGenAttributeHandler<T>) handler).getValue(request, paramName);
		} else {
			String submittedValue = request.getParameter(paramName);
			return handler.deserialize(submittedValue);
		}
	}
	
}
