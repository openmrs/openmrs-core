/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.springframework.stereotype.Component;

/**
 * This is a class for custom datatypes for concepts
 *
 * @since 2.0.0
 */
@Component
public class ConceptDatatype extends BaseOpenmrsDatatype<Concept> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(String)
	 * @override
	 */
	@Override
	public Concept deserialize(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return null;
		}
		return Context.getConceptService().getConceptByUuid(uuid);
	}
	
	/**
	 * @see BaseOpenmrsDatatype#doGetTextSummary(Object)
	 * @should use the name in summary instance
	 */
	@Override
	public Summary doGetTextSummary(Concept concept) {
		String name = "";
		if (concept != null && concept.getName() != null) {
			name = concept.getName().getName();
		}
		return new CustomDatatype.Summary(name, true);
	}
}
