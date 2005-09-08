package org.openmrs;

public class ConceptSetId implements java.io.Serializable {
	
	public static final long serialVersionUID = 1L;
	
	private Concept concept;
	private Concept set;
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	/**
	 * @return Returns the set.
	 */
	public Concept getSet() {
		return set;
	}
	/**
	 * @param set The set to set.
	 */
	public void setSet(Concept set) {
		this.set = set;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ConceptSetId))
			return false;
		ConceptSetId csi = (ConceptSetId) obj;
		return (concept.equals(csi.concept) && set.equals(csi.set));
	}
	
	public int hashCode() {
		return concept.hashCode() + set.hashCode();
	}
}
