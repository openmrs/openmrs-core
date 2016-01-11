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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.CustomDatatype;

/**
 * This is a superclass for custom datatypes for OpenmrsMetadata
 * 
 * @since 2.0.0
 */
public abstract class BaseMetadataDatatype<T extends OpenmrsMetadata> extends BaseOpenmrsDatatype<T> {
	
	/**
	 * @see BaseOpenmrsDatatype#doGetTextSummary(Object)
	 * @should use the name in summary instance
	 */
	@Override
	public Summary doGetTextSummary(T typedValue) {
		return new CustomDatatype.Summary(typedValue.getName(), true);
	}
	
}
