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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.openmrs.api.context.Context;

/**
 * Drug
 */
@Indexed
public class Drug extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 285L;
	
	// Fields
	@DocumentId
	private Integer drugId;
	
	private Boolean combination = false;
	
	private Concept dosageForm;
	
	private Double maximumDailyDose;
	
	private Double minimumDailyDose;
	
	private String strength;
	
	private Concept doseLimitUnits;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Concept concept;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Set<DrugReferenceMap> drugReferenceMaps;
	
	private Collection<DrugIngredient> ingredients;
	
	// Constructors
	
	/** default constructor */
	public Drug() {
		ingredients = new LinkedHashSet<>();
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
	 * Gets the entries concept drug name in the form of CONCEPTNAME (Drug: DRUGNAME)
	 * 
	 * @param locale
	 * @return full drug name (with concept name appended)
	 */
	public String getFullName(Locale locale) {
		if (concept == null) {
			return getName();
		} else {
			return getName() + " (" + concept.getName(locale).getName() + ")";
		}
	}
	
	/**
	 * Gets whether or not this is a combination drug
	 *
	 * @return Boolean
	 * 
	 * @deprecated as of 2.0, use {@link #getCombination()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isCombination() {
		return getCombination();
	}
	
	public Boolean getCombination() {
		return combination;
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
	 * Gets the strength
	 *
	 * @return String
	 * @since 1.10
	 */
	public String getStrength() {
		return strength;
	}
	
	/**
	 * Sets the strength
	 *
	 * @param strength
	 * @since 1.10
	 */
	public void setStrength(String strength) {
		this.strength = strength;
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
	@Override
	public Integer getId() {
		
		return getDrugId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
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
		if (StringUtils.isNotBlank(getName())) {
			return getName();
		}
		if (getConcept() != null) {
			return getConcept().getName().getName();
		}
		return "";
	}
	
	/**
	 * @return Returns the drugReferenceMaps.
	 * @since 1.10
	 */
	public Set<DrugReferenceMap> getDrugReferenceMaps() {
		if (drugReferenceMaps == null) {
			drugReferenceMaps = new LinkedHashSet<>();
		}
		return drugReferenceMaps;
	}
	
	/**
	 * @param drugReferenceMaps The drugReferenceMaps to set.
	 * @since 1.10
	 */
	public void setDrugReferenceMaps(Set<DrugReferenceMap> drugReferenceMaps) {
		this.drugReferenceMaps = drugReferenceMaps;
	}
	
	/**
	 * Add the given DrugReferenceMap object to this drug's list of drug reference mappings. If there is
	 * already a corresponding DrugReferenceMap object for this concept, this one will not be added.
	 *
	 * @param drugReferenceMap
	 * @since 1.10
	 *
	 * <strong>Should</strong> set drug as the drug to which a mapping is being added
	 *
	 * <strong>Should</strong> should not add duplicate drug reference maps
	 */
	public void addDrugReferenceMap(DrugReferenceMap drugReferenceMap) {
		if (drugReferenceMap != null && !getDrugReferenceMaps().contains(drugReferenceMap)) {
			drugReferenceMap.setDrug(this);
			if (drugReferenceMap.getConceptMapType() == null) {
				drugReferenceMap.setConceptMapType(Context.getConceptService().getDefaultConceptMapType());
			}
			getDrugReferenceMaps().add(drugReferenceMap);
		}
	}
	
	/**
	 * Gets the doseLimitUnits which represents the units of the existing maximumDailyDose and
	 * minimumDailyDose
	 * 
	 * @return the doseLimitUnits.
	 * @since 2.3.0
	 */
	public Concept getDoseLimitUnits() {
		return doseLimitUnits;
	}
	
	/**
	 * Sets the doseLimitUnits which represents the units of the existing maximumDailyDose and
	 * minimumDailyDose
	 * 
	 * @param doseLimitUnits The doseLimitUnits to set.
	 * @since 2.3.0
	 */
	public void setDoseLimitUnits(Concept doseLimitUnits) {
		this.doseLimitUnits = doseLimitUnits;
	}
}
