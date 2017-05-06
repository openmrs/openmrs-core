/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;

/**
 * Property editor for {@link LocationAttributeType}s
 * 
 * @since 1.9
 */
public class LocationAttributeTypeEditor extends OpenmrsPropertyEditor<LocationAttributeType> {
	
	@Override
	protected LocationAttributeType getObjectById(Integer id) {
		return Context.getLocationService().getLocationAttributeType(id);
	}
	
	@Override
	protected LocationAttributeType getObjectByUuid(String uuid) {
		return Context.getLocationService().getLocationAttributeTypeByUuid(uuid);
	}
}
