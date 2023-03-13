/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * A Concept object can represent either a question or an answer to a data point. That data point is
 * usually an {@link Obs}. <br>
 * <br>
 * ConceptName is the real world term used to express a Concept within the idiom of a particular
 * locale. <br>
 * The purpose of this class therefore is to record information of a single encounter by taking the
 * concept and concept name or a string for a concept that may not be in the database.
 * 
 * @since 2.2
 */
@Embeddable
public class CodedOrFreeText {
	
	@ManyToOne
	Concept coded;
	
	@ManyToOne
	ConceptName specificName;
	
	String nonCoded;
	
	public CodedOrFreeText() {
	}
	
	/**
	 * Convenience constructor with concept, concept name and manually written concept name to save
	 *
	 * @param coded the coded concept to set
	 * @param specificName the specific name of the coded concept to set
	 * @param nonCoded the manually written string name of the concept to set
	 */
	public CodedOrFreeText(Concept coded, ConceptName specificName, String nonCoded) {
		this.coded = coded;
		this.specificName = specificName;
		this.nonCoded = nonCoded;
	}
	
	/**
	 * Gets the coded concept
	 *
	 * @return coded concept
	 */
	public Concept getCoded() {
		return coded;
	}
	
	/**
	 * Sets the coded concept
	 *
	 * @param coded the coded concept to set.
	 */
	public void setCoded(Concept coded) {
		this.coded = coded;
	}
	
	/**
	 * Gets the specific name of the coded concept
	 *
	 * @return specific name of the coded concept.
	 */
	public ConceptName getSpecificName() {
		return specificName;
	}
	
	/**
	 * Sets the specific name
	 *
	 * @param specificName the specific name of the coded concept to set.
	 */
	public void setSpecificName(ConceptName specificName) {
		this.specificName = specificName;
	}
	
	/**
	 * Gets the manually written string name of the concept
	 *
	 * @return manually written string name of the concept.
	 */
	public String getNonCoded() {
		return nonCoded;
	}
	
	/**
	 * Sets the manually written string concept name
	 *
	 * @param nonCoded the manually written string name of the concept to set.
	 */
	public void setNonCoded(String nonCoded) {
		this.nonCoded = nonCoded;
	}
}
