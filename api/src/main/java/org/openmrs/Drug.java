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

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

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
	
	/**
	 * @deprecated moving it to order entry where it belongs.
	 */
	@Deprecated
	public Concept getRoute() {
		return route;
	}
	
	/**
	 * @deprecated moving it to order entry where it belongs.
	 */
	@Deprecated
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
	
	/**
	 * Convenience method that returns a display name for the drug, defaults to drug.name
	 * 
	 * @return the display name
	 * @since 1.8.5, 1.9.4, 1.10
	 */
	public String getDisplayName() {
		if (StringUtils.isNotBlank(getName()))
			return getName();
		if (getConcept() != null)
			return getConcept().getName().getName();
		return "";
	}
}
