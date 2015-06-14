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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatype.Summary;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;
import org.springframework.stereotype.Component;

/**
 * Handler for the Location custom datatype
 */
@Component
public class LocationFieldGenDatatypeHandler implements FieldGenDatatypeHandler<LocationDatatype, Location> {
	
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
		return "org.openmrs.Location";
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
	public Location getValue(LocationDatatype datatype, HttpServletRequest request, String formFieldName)
	        throws InvalidCustomValueException {
		String fieldValue = request.getParameter(formFieldName);
		if (StringUtils.isBlank(fieldValue))
			return null;
		try {
			SimpleXStreamSerializer serializer = new SimpleXStreamSerializer();
			return serializer.deserialize(fieldValue, Location.class);
		}
		catch (SerializationException ex) {
			throw new InvalidCustomValueException("Invalid Location " + fieldValue);
		}
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype, java.lang.String)
	 */
	@Override
	public Summary toHtmlSummary(CustomDatatype<Location> datatype, String valueReference) {
		return new CustomDatatype.Summary(toHtml(datatype, valueReference), true);
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype, java.lang.String)
	 */
	@Override
	public String toHtml(CustomDatatype<Location> datatype, String valueReference) {
		try {
			SimpleXStreamSerializer serializer = new SimpleXStreamSerializer();
			return serializer.deserialize(valueReference, Location.class).toString();
		}
		catch (SerializationException ex) {
			throw new InvalidCustomValueException("Invalid renders Location" + valueReference);
		}
	}
}
