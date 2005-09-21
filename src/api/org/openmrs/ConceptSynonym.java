package org.openmrs;

import java.util.Date;

/**
 * ConceptSynonym 
 */
public class ConceptSynonym implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Concept concept;
	private String synonym;
	private String locale;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptSynonym() {
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptSynonym) {
			ConceptSynonym c = (ConceptSynonym)obj;
			return (this.concept.equals(c.getConcept()) &&
					this.synonym.equals(c.getSynonym()) &&
					this.locale.equals(c.getLocale()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConcept() == null || this.getSynonym() == null || this.getLocale() == null) return super.hashCode();
		return this.getConcept().hashCode() + this.getSynonym().hashCode() + this.getLocale().hashCode();
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
	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
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