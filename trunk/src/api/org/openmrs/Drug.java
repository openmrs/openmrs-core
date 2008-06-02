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

import java.util.Date;
import java.util.Locale;

/**
 * Drug 
 */
public class Drug implements java.io.Serializable {

	public static final long serialVersionUID = 285L;

	// Fields

	private Integer drugId;
	private String name;
	private Boolean combination = false;
	private Concept dosageForm;
	private Double doseStrength;
	private Double maximumDailyDose;
	private Double minimumDailyDose;
	private Concept route;
	private String units;
	private Date dateCreated;
	private Concept concept;
	private User creator;
	private Boolean retired = false;
	private User retiredBy;
	private Date dateRetired;
	private String retireReason;

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
		return (this.drugId.equals(drug.getDrugId()));
	}
	
	public int hashCode() {
		if (this.getDrugId() == null) return super.hashCode();
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
	 * @param drugId
	 */
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}

	/**
	 * Gets the entires concept drug name in the form of
	 * CONCEPTNAME (Drug: DRUGNAME)
	 * @param locale
	 * @return full drug name (with concept name appended)
	 */
	public String getFullName(Locale locale) {
		if (concept == null)
			return name;
		else
			return name + " (" + concept.getName(locale).getName() + ")"; 
	}
	/**
	 * Gets the name of this drug
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of this drug
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets whether or not this is a combination drug
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
	 * @param combination
	 */
	public void setCombination(Boolean combination) {
		this.combination = combination;
	}

	/**
	 * Gets the dose strength of this drug
	 * @return Double
	 */
	public Double getDoseStrength() {
		return this.doseStrength;
	}

	/**
	 * Sets the dose strength
	 * @param Double
	 */
	public void setDoseStrength(Double doseStrength) {
		this.doseStrength = doseStrength;
	}

	/**
	 * Gets the units
	 * @return String
	 */
	public String getUnits() {
		return this.units;
	}

	/**
	 * Sets the units
	 * @param String
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * Gets the date created
	 * @return Date
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	/**
	 * Sets the date this was created
	 * @param Date
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * Gets the concept this drug is tied to
	 * @return Concept
	 */
	public Concept getConcept() {
		return this.concept;
	}

	/**
	 * Sets the concept this drug is tied to
	 * @param Concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * Gets the creator of this drug record
	 * @return User
	 */
	public User getCreator() {
		return this.creator;
	}

	/**
	 * Sets the creator of this drug record
	 * @param User
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateRetired() {
		return dateRetired;
	}

	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
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

	public Boolean isRetired() {
		return retired;
	}
	
	public Boolean getRetired() {
		return isRetired();
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	public User getRetiredBy() {
		return retiredBy;
	}

	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}

	public String getRetireReason() {
		return retireReason;
	}

	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

}