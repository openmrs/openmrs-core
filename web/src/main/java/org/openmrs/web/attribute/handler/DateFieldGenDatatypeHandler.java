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
