package org.openmrs;

/**
 * ConceptWord 
 */
public class ConceptWord implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private ConceptWordId conceptWordId;

	// Constructors

	/** default constructor */
	public ConceptWord() {
	}

	public ConceptWord(ConceptWordId conceptWordId) {
		this.conceptWordId = conceptWordId;
	}

	// Property accessors
	
	/**
	 * @return Returns the conceptWordId.
	 */
	public ConceptWordId getConceptWordId() {
		return conceptWordId;
	}

	/**
	 * @param conceptWordId
	 *            The conceptWordId to set.
	 */
	public void setConceptWordId(ConceptWordId conceptWordId) {
		this.conceptWordId = conceptWordId;
	}
	
	/**
	 * 
	 */
	public Concept getConcept() {
		return conceptWordId.getConcept();
	}
	
	public void setConcept(Concept concept) {
		conceptWordId.setConcept(concept);
	}

	/**
	 * 
	 */
	public String getWord() {
		return conceptWordId.getWord();
	}
	
	public void setWord(String word) {
		conceptWordId.setWord(word);
	}

}