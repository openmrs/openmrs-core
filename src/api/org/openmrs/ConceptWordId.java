package org.openmrs;

public class ConceptWordId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Concept concept;
	private String word;

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
	 * @return Returns the word.
	 */
	public String getWord() {
		return word;
	}

	/**
	 * @param word
	 *            The word to set.
	 */
	public void setWord(String word) {
		this.word = word;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ConceptWordId))
			return false;
		ConceptWordId cwi = (ConceptWordId) obj;
		return concept.equals(cwi.concept) && word.equals(cwi.word);
	}
	
	public int hashCode() {
		return concept.hashCode() + word.hashCode();
	}
}
