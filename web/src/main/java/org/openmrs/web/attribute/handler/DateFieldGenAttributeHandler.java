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
import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.DateAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A fieldgen version of a {@link DateAttributeHandler} that uses the standard Date picker widget.
 * @since 1.9
 */
@Component
@Order(0)
public class DateFieldGenAttributeHandler extends DateAttributeHandler implements FieldGenAttributeHandler<Date> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenAttributeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "java.util.Date";
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
	public Date getValue(HttpServletRequest request, String formFieldName) {
		String stringVal = request.getParameter(formFieldName);
		if (StringUtils.isBlank(stringVal)) {
			return null;
		} else {
			try {
				Date date = Context.getDateFormat().parse(stringVal);
				super.validate(date);
				return date;
			}
			catch (ParseException ex) {
				throw new InvalidAttributeValueException("general.invalid", ex);
			}
		}
	}
	
}
