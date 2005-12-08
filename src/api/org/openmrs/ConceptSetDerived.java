package org.openmrs;


/**
 * ConceptSetDerived 
 */
public class ConceptSetDerived implements java.io.Serializable {

	public static final long serialVersionUID = 3788L;

	// Fields

	private Concept concept;
	private Concept set;
	private Double sortWeight;

	// Constructors

	/** default constructor */
	public ConceptSetDerived() {
	}
	
	public ConceptSetDerived(Concept set, Concept concept, Double weight) {
		setSet(set);
		setConcept(concept);
		setSortWeight(weight);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptSetDerived) {
			ConceptSetDerived c = (ConceptSetDerived)obj;
			return (this.concept.equals(c.getConcept()) &&
					this.set.equals(c.getSet()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConcept() == null || this.getSet() == null) return super.hashCode();
		return this.getConcept().hashCode() + this.getSet().hashCode();
	}

	// Property accessors

	/**
	 * 
	 */
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public Concept getSet() {
		return set;
	}

	public void setSet(Concept set) {
		this.set = set;
	}

	/**
	 * @return Returns the sortWeight.
	 */
	public Double getSortWeight() {
		return sortWeight;
	}

	/**
	 * @param sortWeight
	 *            The sortWeight to set.
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}

}