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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.web.attribute.handler;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.openmrs.messagesource.MessageSourceService;

/**
 * 
 */
public abstract class BaseMetadataFieldGenDatatypeHandler<T extends OpenmrsMetadata> implements FieldGenDatatypeHandler<SerializingCustomDatatype<T>, T> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
	 */
	@Override
	public void setHandlerConfiguration(String arg0) {
		// not used
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
	 */
	public abstract String getWidgetName();
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	public abstract CustomDatatype.Summary toHtmlSummary(CustomDatatype<T> datatype, String valueReference);
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	public abstract String toHtml(CustomDatatype<T> datatype, String valueReference);
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public abstract T getValue(SerializingCustomDatatype<T> datatype, HttpServletRequest request, String formFieldName)
	    throws InvalidCustomValueException;
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		MessageSourceService mss = Context.getMessageSourceService();
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("isNullable", "false");
		ret.put("label", mss.getMessage("general.true"));
		return ret;
	}
}
