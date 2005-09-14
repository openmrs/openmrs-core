package org.openmrs;

/**
 * DrugIngredient
 * 
 * @author Burke Mamlin
 */
public class DrugIngredient implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Concept drug;
	private Concept ingredient;

	// Constructors

	/** default constructor */
	public DrugIngredient() {
	}

	// Property accessors

	/**
	 * @return Returns the drug.
	 */
	public Concept getDrug() {
		return drug;
	}

	/**
	 * @param drug
	 *            The drug to set.
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
	 * @param ingredient
	 *            The ingredient to set.
	 */
	public void setIngredient(Concept ingredient) {
		this.ingredient = ingredient;
	}

}