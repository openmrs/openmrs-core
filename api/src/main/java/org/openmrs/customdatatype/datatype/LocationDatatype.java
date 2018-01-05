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
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

/**
 * This is a class for custom datatypes for locations
 *
 * @since 2.0.0
 */
@Component
public class LocationDatatype extends BaseMetadataDatatype<Location> {
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(String)
	 * @override
	 */
	@Override
	public Location deserialize(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return null;
		}
		return Context.getLocationService().getLocationByUuid(uuid);
	}
}
