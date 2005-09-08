package org.openmrs;

import java.util.Date;

/**
 * ConceptSynonym 
 */
public class ConceptSynonym implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private ConceptSynonymId conceptSynonymId;
	private String locale;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptSynonym() {
	}

	// Property accessors

	/**
	 * 
	 */
	public ConceptSynonymId getConceptSynonymId() {
		return conceptSynonymId;
	}

	public void setConceptSynonymId(ConceptSynonymId conceptSynonymId) {
		this.conceptSynonymId = conceptSynonymId;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return conceptSynonymId.getConcept();
	}

	public void setConcept(Concept concept) {
		conceptSynonymId.setConcept(concept);
	}

	/**
	 * 
	 */
	public String getSynonym() {
		return conceptSynonymId.getSynonym();
	}

	public void setSynonym(String synonym) {
		conceptSynonymId.setSynonym(synonym);
	}

	/**
	 * @return Returns the locale.
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}