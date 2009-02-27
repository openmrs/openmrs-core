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
public class DrugIngredient implements java.io.Serializable {
	
	public static final long serialVersionUID = 94023L;
	
	// Fields
	
	private Concept drug;
	
	private Concept ingredient;
	
	// Constructors
	
	/** default constructor */
	public DrugIngredient() {
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof DrugIngredient) {
			DrugIngredient c = (DrugIngredient) obj;
			return (this.drug.equals(c.getDrug()) && this.ingredient.equals(c.getIngredient()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getDrug() == null || this.getIngredient() == null)
			return super.hashCode();
		return this.getDrug().hashCode() + this.getIngredient().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the drug.
	 */
	public Concept getDrug() {
		return drug;
	}
	
	/**
	 * @param drug The drug to set.
	 */
	public void setDrug(Concept drug) {
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
	
}
