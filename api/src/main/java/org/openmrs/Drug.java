/**
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

import java.util.Locale;

/**
 * Drug
 */
public class Drug extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 285L;
	
	// Fields
	
	private Integer drugId;
	
	private Boolean combination = false;
	
	private Concept dosageForm;
	
	private Double doseStrength;
	
	private Double maximumDailyDose;
	
	private Double minimumDailyDose;
	
	private Concept route;
	
	private String units;
	
	private Concept concept;
	
	// Constructors
	
	/** default constructor */
	public Drug() {
	}
	
	/** constructor with id */
	public Drug(Integer drugId) {
		this.drugId = drugId;
	}
	
	/**
	 * Compares two Drug objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Drug))
			return false;
		
		Drug drug = (Drug) obj;
		if (this.drugId != null && drug.drugId != null)
			return this.drugId.equals(drug.getDrugId());
		
		return this == obj;
	}
	
	public int hashCode() {
		if (this.getDrugId() == null)
			return super.hashCode();
		return this.getDrugId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * Gets the internal identification number for this drug
	 * 
	 * @return Integer
	 */
	public Integer getDrugId() {
		return this.drugId;
	}
	
	/**
	 * Sets the internal identification number for this drug
	 * 
	 * @param drugId
	 */
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}
	
	/**
	 * Gets the entires concept drug name in the form of CONCEPTNAME (Drug: DRUGNAME)
	 * 
	 * @param locale
	 * @return full drug name (with concept name appended)
	 */
	public String getFullName(Locale locale) {
		if (concept == null)
			return getName();
		else
			return getName() + " (" + concept.getName(locale).getName() + ")";
	}
	
	/**
	 * Gets whether or not this is a combination drug
	 * 
	 * @return Boolean
	 */
	public Boolean isCombination() {
		return this.combination;
	}
	
	public Boolean getCombination() {
		return isCombination();
	}
	
	/**
	 * Sets whether or not this is a combination drug
	 * 
	 * @param combination
	 */
	public void setCombination(Boolean combination) {
		this.combination = combination;
	}
	
	/**
	 * Gets the dose strength of this drug
	 * 
	 * @return Double
	 */
	public Double getDoseStrength() {
		return this.doseStrength;
	}
	
	/**
	 * Sets the dose strength
	 * 
	 * @param doseStrength
	 */
	public void setDoseStrength(Double doseStrength) {
		this.doseStrength = doseStrength;
	}
	
	/**
	 * Gets the units
	 * 
	 * @return String
	 */
	public String getUnits() {
		return this.units;
	}
	
	/**
	 * Sets the units
	 * 
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	/**
	 * Gets the concept this drug is tied to
	 * 
	 * @return Concept
	 */
	public Concept getConcept() {
		return this.concept;
	}
	
	/**
	 * Sets the concept this drug is tied to
	 * 
	 * @param concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public Concept getDosageForm() {
		return dosageForm;
	}
	
	public void setDosageForm(Concept dosageForm) {
		this.dosageForm = dosageForm;
	}
	
	public Double getMaximumDailyDose() {
		return maximumDailyDose;
	}
	
	public void setMaximumDailyDose(Double maximumDailyDose) {
		this.maximumDailyDose = maximumDailyDose;
	}
	
	public Double getMinimumDailyDose() {
		return minimumDailyDose;
	}
	
	public void setMinimumDailyDose(Double minimumDailyDose) {
		this.minimumDailyDose = minimumDailyDose;
	}
	
	public Concept getRoute() {
		return route;
	}
	
	public void setRoute(Concept route) {
		this.route = route;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getDrugId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setDrugId(id);
		
	}
	
}
