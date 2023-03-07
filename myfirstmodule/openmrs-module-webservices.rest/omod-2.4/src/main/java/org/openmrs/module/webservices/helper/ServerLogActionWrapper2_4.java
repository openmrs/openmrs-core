/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import org.openmrs.util.MemoryAppender;
import org.openmrs.util.OpenmrsUtil;

public class ServerLogActionWrapper2_4 extends ServerLogActionWrapper {

	@Override
	public MemoryAppender getMemoryAppender() {
		try {
			return OpenmrsUtil.getMemoryAppender();
		} catch (ClassCastException | NullPointerException e) {
			return null;
		}
	}
}
