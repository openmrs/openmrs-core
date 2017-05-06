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

import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;

/**
 * Property editor for {@link LocationTag}s In version 1.9, added ability for this to also retrieve
 * objects by uuid
 * 
 * @since 1.7
 */
public class LocationTagEditor extends OpenmrsPropertyEditor<LocationTag> {
	
	public LocationTagEditor() {
	}
	
	@Override
	protected LocationTag getObjectById(Integer id) {
		return Context.getLocationService().getLocationTag(id);
	}
	
	@Override
	protected LocationTag getObjectByUuid(String uuid) {
		return Context.getLocationService().getLocationTagByUuid(uuid);
	}
}
