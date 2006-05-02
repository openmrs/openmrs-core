package org.openmrs;


/**
 * ConceptSetDerived 
 */
public class ConceptSetDerived implements java.io.Serializable {

	public static final long serialVersionUID = 3788L;

	// Fields

	private Concept concept;
	private Concept conceptSet;
	private Double sortWeight;

	// Constructors

	/** default constructor */
	public ConceptSetDerived() {
	}
	
	public ConceptSetDerived(Concept set, Concept concept, Double weight) {
		setConceptSet(set);
		setConcept(concept);
		setSortWeight(weight);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptSetDerived) {
			ConceptSetDerived c = (ConceptSetDerived)obj;
			return (this.concept.equals(c.getConcept()) &&
					this.conceptSet.equals(c.getConceptSet()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConcept() == null || this.getConceptSet() == null) return super.hashCode();
		return this.getConcept().hashCode() + this.getConceptSet().hashCode();
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
	public Concept getConceptSet() {
		return conceptSet;
	}

	public void setConceptSet(Concept set) {
		this.conceptSet = set;
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