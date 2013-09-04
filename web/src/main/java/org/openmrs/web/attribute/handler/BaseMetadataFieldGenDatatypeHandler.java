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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.openmrs.messagesource.MessageSourceService;

/**
 * This is an abstract super class for handlers like ProviderFieldGenDatatypeHandler,
 * ConceptFieldGenDatatypeHandler, LocationFieldGenDatatypeHandler, ProgramFieldGenDatatypeHandler etc
 */
public abstract class BaseMetadataFieldGenDatatypeHandler<T extends OpenmrsMetadata> implements FieldGenDatatypeHandler<SerializingCustomDatatype<T>, T> {
	
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
