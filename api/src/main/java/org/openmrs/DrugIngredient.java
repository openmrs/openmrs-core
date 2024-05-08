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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * DrugIngredient
 */
@Entity
@Table(name = "drug_ingredient")
public class DrugIngredient extends BaseOpenmrsObject implements OpenmrsObject {
	
	public static final long serialVersionUID = 94023L;
	
	// Fields
	@ManyToOne
	@JoinColumn(name = "drug_id", updatable = false, insertable = false)
	@Id
	private Drug drug;

	@ManyToOne
	@JoinColumn(name = "ingredient_id", updatable = false, insertable = false)
	@Id
	private Concept ingredient;

	@Column(name = "strength")
	private Double strength;
	
	@JoinColumn(name = "units", insertable = false, updatable = false)
	@ManyToOne
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
