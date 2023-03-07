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

import org.openmrs.module.webservices.rest.web.annotation.RepHandler;

/**
 * A named representation, like "minimal"
 */
public class NamedRepresentation implements Representation {
	
	private String representation;
	
	public NamedRepresentation(String representation) {
		if (representation == null)
			throw new IllegalArgumentException("representation is required");
		this.representation = representation;
	}
	
	/**
	 * @return the representation
	 * @see Representation#getRepresentation()
	 */
	@Override
	public String getRepresentation() {
		return representation;
	}
	
	/**
	 * @param representation the representation to set
	 */
	public void setRepresentation(String representation) {
		this.representation = representation;
	}
	
	/**
	 * @param ann
	 * @return true if ann is for {@link NamedRepresentation} with the correct name specified
	 */
	public boolean matchesAnnotation(RepHandler ann) {
		return NamedRepresentation.class.isAssignableFrom(ann.value()) && ann.name().equals(representation);
	}
	
}
