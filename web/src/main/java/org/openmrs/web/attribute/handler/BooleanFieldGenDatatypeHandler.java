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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.messagesource.MessageSourceService;
import org.springframework.stereotype.Component;

/**
 * Handler for the boolean custom datatype
 */
@Component
public class BooleanFieldGenDatatypeHandler implements FieldGenDatatypeHandler<org.openmrs.customdatatype.datatype.Boolean, java.lang.Boolean> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
	 */
	@Override
	public void setHandlerConfiguration(String arg0) {
		// not used
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#render(org.openmrs.customdatatype.CustomDatatype, java.lang.String, java.lang.String)
	 */
	@Override
	public String render(org.openmrs.customdatatype.datatype.Boolean datatype, String referenceString, String view) {
		return referenceString;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "boolean";
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		MessageSourceService mss = Context.getMessageSourceService();
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("isNullable", "false");
		ret.put("trueLabel", mss.getMessage("general.true"));
		ret.put("falseLabel", mss.getMessage("general.false"));
		return ret;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public Boolean getValue(org.openmrs.customdatatype.datatype.Boolean datatype, HttpServletRequest request,
	        String formFieldName) throws InvalidCustomValueException {
		String result = request.getParameter(formFieldName);
		if (StringUtils.isBlank(result))
			return null;
		try {
			return Boolean.valueOf(result);
		}
		catch (Exception ex) {
			throw new InvalidCustomValueException("Invalid boolean: " + result);
		}
	}
	
}
