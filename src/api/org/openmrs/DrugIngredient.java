package org.openmrs;

/**
 * DrugIngredient 
 */
public class DrugIngredient implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private DrugIngredientId drugIngredientId;

	// Constructors

	/** default constructor */
	public DrugIngredient() {
	}

	public DrugIngredient(DrugIngredientId drugIngredientId) {
		this.drugIngredientId = drugIngredientId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Concept getDrug() {
		return drugIngredientId.getDrug();
	}

	public void setDrug(Concept drug) {
		drugIngredientId.setDrug(drug);
	}

	/**
	 * 
	 */
	public Concept getIngredient() {
		return drugIngredientId.getIngredient();
	}

	public void setIngredient(Concept ingredient) {
		drugIngredientId.setIngredient(ingredient);
	}

	/**
	 * @return Returns the drugIngredientId.
	 */
	public DrugIngredientId getDrugIngredientId() {
		return drugIngredientId;
	}

	/**
	 * @param drugIngredientId
	 *            The drugIngredientId to set.
	 */
	public void setDrugIngredientId(DrugIngredientId drugIngredientId) {
		this.drugIngredientId = drugIngredientId;
	}

}