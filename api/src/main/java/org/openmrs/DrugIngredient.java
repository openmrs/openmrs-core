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
 * DrugIngredient
 */
public class DrugIngredient extends BaseOpenmrsObject implements java.io.Serializable, OpenmrsObject {
	
	public static final long serialVersionUID = 94023L;
	
	// Fields
	
	private Drug drug;
	
	private Concept ingredient;
	
	private Double strength;
	
	private Concept units;
	
	// Constructors
	
	/** default constructor */
	public DrugIngredient() {
	}
	
	// Property accessors
	
	/**
	 * @return the drug
	 */
	public Drug getDrug() {
		return drug;
	}
	
	/**
	 * @param drug the drug to set
	 */
	public void setDrug(Drug drug) {
		this.drug = drug;
	}
	
	/**
	 * @return Returns the ingredient.
	 */
	public Concept getIngredient() {
		return ingredient;
	}
	
	/**
	 * @param ingredient The ingredient to set.
	 */
	public void setIngredient(Concept ingredient) {
		this.ingredient = ingredient;
	}
	
	/**
	 * @return Returns the strength.
	 * @since 1.11
	 */
	public Double getStrength() {
		return strength;
	}
	
	/**
	 * @param strength The strength to set.
	 * @since 1.11
	 */
	public void setStrength(Double strength) {
		this.strength = strength;
	}
	
	/**
	 * @return Returns the units.
	 * @since 1.11
	 */
	public Concept getUnits() {
		return units;
	}
	
	/**
	 * @param units The units to set.
	 * @since 1.11
	 */
	public void setUnits(Concept units) {
		this.units = units;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}
