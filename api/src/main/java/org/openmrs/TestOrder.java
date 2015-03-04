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

/**
 * This is a type of order that adds tests specific attributes like: laterality,
 * clinical history, etc.
 *
 * @since 1.9.2, 1.10
 */
public class TestOrder extends Order {
	
	public enum Laterality {
		LEFT, RIGHT, BILATERAL
	}
	
	public static final long serialVersionUID = 1L;
	
	private Concept specimenSource;
	
	private Laterality laterality;
	
	private String clinicalHistory;
	
	/**
	 * Default Constructor
	 */
	public TestOrder() {
	}
	
	/**
	 * @return the specimenSource
	 */
	public Concept getSpecimenSource() {
		return specimenSource;
	}
	
	/**
	 * @param specimenSource the specimenSource to set
	 */
	public void setSpecimenSource(Concept specimenSource) {
		this.specimenSource = specimenSource;
	}
	
	/**
	 * Gets the laterality.
	 *
	 * @return the laterality.
	 */
	public Laterality getLaterality() {
		return laterality;
	}
	
	/**
	 * Sets the laterality.
	 *
	 * @param laterality the laterality to set.
	 */
	public void setLaterality(Laterality laterality) {
		this.laterality = laterality;
	}
	
	/**
	 * Gets the clinical history.
	 *
	 * @return the clinical history.
	 */
	public String getClinicalHistory() {
		return clinicalHistory;
	}
	
	/**
	 * Sets the clinical history.
	 *
	 * @param clinicalHistory the clinical history to set.
	 */
	public void setClinicalHistory(String clinicalHistory) {
		this.clinicalHistory = clinicalHistory;
	}
	
}
