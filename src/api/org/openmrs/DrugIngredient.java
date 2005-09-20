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
	
	public boolean equals(Object obj) {
		if (obj instanceof DrugIngredient) {
			DrugIngredient c = (DrugIngredient)obj;
			return (this.drug.equals(c.getDrug()) &&
					this.ingredient.equals(c.getIngredient()));
		}
		return false;
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