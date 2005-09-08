package org.openmrs;

public class ConceptSynonymId implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	private Concept concept;
	private String synonym;

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept
	 *            The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the synonym.
	 */
	public String getSynonym() {
		return synonym;
	}

	/**
	 * @param synonym
	 *            The synonym to set.
	 */
	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ConceptSynonymId))
			return false;
		ConceptSynonymId csi = (ConceptSynonymId) obj;
		return concept.equals(csi.concept) && synonym.equals(csi.synonym);
	}
	
	public int hashCode() {
		return concept.hashCode() + synonym.hashCode();
	}
}
