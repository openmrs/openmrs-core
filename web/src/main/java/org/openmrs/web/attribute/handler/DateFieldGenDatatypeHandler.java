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

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.DateDatatype;
import org.springframework.stereotype.Component;

/**
 * A fieldgen version of a {@link DateAttributeHandler} that uses the standard Date picker widget.
 * @since 1.9
 */
@Component
public class DateFieldGenDatatypeHandler implements FieldGenDatatypeHandler<DateDatatype, java.util.Date> {
	
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
	@Override
	public String getWidgetName() {
		return "java.util.Date";
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetConfiguration()
	 */
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public java.util.Date getValue(org.openmrs.customdatatype.datatype.DateDatatype datatype, HttpServletRequest request,
	        String formFieldName) throws InvalidCustomValueException {
		String stringVal = request.getParameter(formFieldName);
		if (StringUtils.isBlank(stringVal)) {
			return null;
		} else {
			try {
				java.util.Date date = Context.getDateFormat().parse(stringVal);
				datatype.validate(date);
				return date;
			}
			catch (ParseException ex) {
				throw new InvalidCustomValueException("general.invalid", ex);
			}
		}
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype, java.lang.String)
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype<Date> datatype, String valueReference) {
		return new CustomDatatype.Summary(toHtml(datatype, valueReference), true);
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype, java.lang.String)
	 */
	@Override
	public String toHtml(CustomDatatype<Date> datatype, String valueReference) {
		return Context.getDateFormat().format(datatype.fromReferenceString(valueReference));
	}
}
