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
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}
