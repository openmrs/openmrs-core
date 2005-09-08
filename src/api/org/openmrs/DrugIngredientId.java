package org.openmrs;

public class DrugIngredientId implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	private Concept drug;
	private Concept ingredient;

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
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DrugIngredientId))
			return false;
		DrugIngredientId dii = (DrugIngredientId) obj;
		return drug.equals(dii.drug) && ingredient.equals(dii.ingredient);
	}
	
	public int hashCode() {
		return drug.hashCode() + ingredient.hashCode();
	}

}
