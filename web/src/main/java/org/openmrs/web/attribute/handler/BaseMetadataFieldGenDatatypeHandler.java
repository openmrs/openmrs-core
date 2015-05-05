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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.datatype.BaseMetadataDatatype;

/**
 * This is a superclass for FieldGenDatatypeHandlers for OpenmrsMetadata
 * 
 * @since 1.12
 */
public abstract class BaseMetadataFieldGenDatatypeHandler<DT extends BaseMetadataDatatype<T>, T extends OpenmrsMetadata> extends SerializingFieldGenDatatypeHandler<DT, T> {
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 * @should return the name
	 */
	@Override
	public String toHtml(CustomDatatype<T> datatype, String valueReference) {
		return toHtmlSummary(datatype, valueReference).getSummary();
	}
	
	/**
	 * @see SerializingFieldGenDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      String)
	 * @should use the name in the html summary instance
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype<T> datatype, String valueReference) {
		return datatype.getTextSummary(valueReference);
	}
}
