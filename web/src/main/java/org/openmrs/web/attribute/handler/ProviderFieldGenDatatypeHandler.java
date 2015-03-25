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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatype.Summary;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.ProviderDatatype;
/**
 * An implementation of {@link FieldGenDatatypeHandler} for {@link ProviderDatatype}
 * @since 1.12
 *
 */
public class ProviderFieldGenDatatypeHandler implements FieldGenDatatypeHandler<ProviderDatatype, Provider> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.ProviderFieldGenDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public Summary toHtmlSummary(CustomDatatype<Provider> datatype, String valueReference) {
		return datatype.getTextSummary(valueReference);
		
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public String toHtml(CustomDatatype<Provider> datatype, String valueReference) {
		if (datatype == null || !(datatype instanceof ProviderDatatype))
			return null;
		return datatype.getTextSummary(valueReference).getSummary();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
	 */
	@Override
	public void setHandlerConfiguration(String handlerConfig) {
		// not used
		
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
	 */
	
	@Override
	public String getWidgetName() {
		return "org.openmrs.Provider";
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, java.lang.String)
	 * @should return the corresponding provider object if it exists
	 */
	@Override
	public Provider getValue(ProviderDatatype datatype, HttpServletRequest request, String formFieldName)
	        throws InvalidCustomValueException {
		String result = request.getParameter(formFieldName);
		if (StringUtils.isEmpty(result))
			return null;
		try {
			return datatype.deserialize(result);
		}
		catch (Exception ex) {
			throw new InvalidCustomValueException("Invalid Provider ID : " + result, ex);
		}
		
	}
	
}
