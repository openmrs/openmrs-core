/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

/**
 * This is a type of order that adds tests specific attributes like: specimen source, laterality,
 * clinical history, etc.
 * 
 * @since 1.9
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
	 * Gets the specimen source.
	 *
	 * @return the specimen source.
	 */
	public Concept getSpecimenSource() {
		return specimenSource;
	}
	
	/**
	 * Sets the specimen source.
	 * 
	 * @param specimenSource the specimen source to set.
	 */
	public void setSpecimenSource(Concept specimenSource) {
		this.specimenSource = specimenSource;
	}
	
	/**
	 * Gets the laterality.
	 * 
	 * @return the laterality.
	 * @since 1.10
	 */
	public Laterality getLaterality() {
		return laterality;
	}
	
	/**
	 * Sets the laterality.
	 * 
	 * @param laterality the laterality to set.
	 * @since 1.10
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
