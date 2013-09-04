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

import org.apache.commons.lang.StringUtils;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.SerializingCustomDatatype;

/**
 * This is a superclass for custom datatypes for metadata
 * 
 * @since 1.10
 */
public abstract class BaseMetadataFieldGenDatatypeHandler<T extends BaseOpenmrsFieldGenDatatypeHandler> implements FieldGenDatatypeHandler<SerializingCustomDatatype<T>, T> {
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype<T> datatype, String valueReference) {
		return toHtmlSummary(datatype, valueReference);
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public String toHtml(CustomDatatype<T> datatype, String valueReference) {
		SerializingCustomDatatype<T> dt = (SerializingCustomDatatype<T>) datatype;
		String result = ((OpenmrsMetadata) dt.deserialize(valueReference)).getName();
		
		return result;
	}
}
