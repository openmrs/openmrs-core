/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.name;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;

/**
 * @since 1.12
 */
public class NameSupport extends LayoutSupport<NameTemplate> {
	
	private static NameSupport singleton;
	
	public NameSupport() {
		if (singleton == null) {
			singleton = this;
		}
	}
	
	public static NameSupport getInstance() {
		if (singleton == null) {
			throw new APIException("Not Yet Instantiated");
		} else {
			return singleton;
		}
	}
	
	@Override
	public String getDefaultLayoutFormat() {
		String ret = Context.getAdministrationService().getGlobalProperty("layout.name.format");
		return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
	}
}
