/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.extension;

import java.util.Set;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.provider.Link;

/**
 * Indicates that a module can add an encounter to a visit. It must accept <b>patientId</b> and
 * <b>visitId</b> URL parameters. It may accept an optional <b>returnUrl</b> parameter.
 */
public abstract class AddEncounterToVisitExtension extends Extension {
	
	/**
	 * @see org.openmrs.module.Extension#getMediaType()
	 */
	@Override
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
	
	/**
	 * @return the forms for which this module can add an encounter to visit
	 */
	public abstract Set<Link> getAddEncounterToVisitLinks();
}
