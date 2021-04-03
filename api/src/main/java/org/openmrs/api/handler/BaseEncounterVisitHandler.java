/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import org.openmrs.api.context.Context;

/**
 * This base class should be used by specific implementations of {@link EncounterVisitHandler}.
 * <p>
 * It delivers a default implementation for {@link EncounterVisitHandler#getDisplayName()}.
 * 
 * @since 1.9
 */
public abstract class BaseEncounterVisitHandler implements EncounterVisitHandler {
	
	/**
	 * Delegates to {@link EncounterVisitHandler#getDisplayName(java.util.Locale)} with
	 * {@link Context#getLocale()} as a parameter.
	 * 
	 * @return a displayable string so that users can pick between different assignment handlers
	 */
	@Override
	public String getDisplayName() {
		return getDisplayName(Context.getLocale());
	}
	
}
