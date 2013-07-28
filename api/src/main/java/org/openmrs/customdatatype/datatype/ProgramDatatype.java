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
package org.openmrs.customdatatype.datatype;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for program, represented by org.openmrs.Program.
 * 
 * @since 1.10
 */
@Component
public class ProgramDatatype extends SerializingCustomDatatype<Program> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 * @Should return a Program uuid during serialization
	 */
	@Override
	public String serialize(Program typedValue) {
		if (typedValue == null)
			return null;
		return typedValue.getUuid();
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 * @Should reconstruct a program serialized by this handler
	 */
	@Override
	public Program deserialize(String serializedValue) {
		if (StringUtils.isEmpty(serializedValue))
			return null;
		return Context.getProgramWorkflowService().getProgramByUuid(serializedValue);
	}
}
