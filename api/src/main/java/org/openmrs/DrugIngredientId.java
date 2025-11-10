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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for DrugIngredient.
 */
@Embeddable
public class DrugIngredientId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "drug_id")
	private Integer drugId;

	@Column(name = "ingredient_id")
	private Integer ingredientId;

	public DrugIngredientId() {
	}

	public DrugIngredientId(Integer drugId, Integer ingredientId) {
		this.drugId = drugId;
		this.ingredientId = ingredientId;
	}

	public Integer getDrugId() {
		return drugId;
	}

	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}

	public Integer getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Integer ingredientId) {
		this.ingredientId = ingredientId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DrugIngredientId that = (DrugIngredientId) o;
		return Objects.equals(drugId, that.drugId) &&
			Objects.equals(ingredientId, that.ingredientId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(drugId, ingredientId);
	}
}
