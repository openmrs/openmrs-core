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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Drug
 */
public class Drug extends BaseOpenmrsMetadata implements java.io.Serializable, Orderable<DrugOrder> {
	
	public static final long serialVersionUID = 285L;
	
	private static final String IDENTIFIER_PREFIX = "org.openmrs.Drug:";
	
	private static final Log log = LogFactory.getLog(Drug.class);
	
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
	
	private Collection<DrugIngredient> ingredients;
	
	// Constructors
	
	/** default constructor */
	public Drug() {
		ingredients = new LinkedHashSet<DrugIngredient>();
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
	 * Gets the entires concept drug name in the form of CONCEPTNAME (Drug:
	 * DRUGNAME)
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
	 * @return Returns the ingredients
	 * @since 1.10
	 */
	public Collection<DrugIngredient> getIngredients() {
		return ingredients;
	}
	
	/**
	 * @param ingredients
	 *            The ingredients to set
	 * @since 1.10
	 */
	public void setIngredients(Collection<DrugIngredient> ingredients) {
		this.ingredients = ingredients;
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
	 * @see org.openmrs.Orderable#getUniqueIdentifier()
	 */
	@Override
	public String getUniqueIdentifier() {
		return "org.openmrs.Drug:" + drugId;
	}
	
	/**
	 * Gets a numeric identifier from a string identifier.
	 * 
	 * @param identifier
	 *            the string identifier.
	 * @return the numeric identifier if it is a valid one, else null
	 * @should return numeric identifier of valid string identifier
	 * @should return null for an invalid string identifier
	 * @should fail if null or empty passed in
	 * @since 1.10
	 */
	public static Integer getNumericIdentifier(String identifier) {
		if (StringUtils.isBlank(identifier))
			throw new IllegalArgumentException("identifier cannot be null");
		
		if (!identifier.startsWith(IDENTIFIER_PREFIX))
			return null;
		
		try {
			return Integer.valueOf(identifier.substring(IDENTIFIER_PREFIX.length()));
		}
		catch (NumberFormatException ex) {
			log.error("invalid unique identifier for Drug:" + identifier, ex);
		}
		
		return null;
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
