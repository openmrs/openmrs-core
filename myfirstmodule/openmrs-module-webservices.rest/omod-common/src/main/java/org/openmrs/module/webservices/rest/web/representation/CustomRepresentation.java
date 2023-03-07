/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.representation;

/**
 * Implementation of {@link Representation} where the user specifies which properties they want
 * included
 */
public class CustomRepresentation implements Representation {
	
	String specification;
	
	public CustomRepresentation(String specification) {
		if (specification == null)
			throw new IllegalArgumentException("specification is required");
		this.specification = specification;
	}
	
	@Override
	public String getRepresentation() {
		return specification;
	}
	
}
