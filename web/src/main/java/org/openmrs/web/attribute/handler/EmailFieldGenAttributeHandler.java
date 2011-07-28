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

import org.openmrs.attribute.handler.EmailAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class EmailFieldGenAttributeHandler extends EmailAttributeHandler implements FieldGenAttributeHandler<String> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenAttributeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return String.class.getName();
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenAttributeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenAttributeHandler#getValue(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public String getValue(HttpServletRequest request, String formFieldName) {
		String val = request.getParameter(formFieldName);
		validate(val);
		return val;
	}
	
}
