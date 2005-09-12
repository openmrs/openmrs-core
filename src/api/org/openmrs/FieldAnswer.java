package org.openmrs;

import java.util.Date;

/**
 * FieldAnswer 
 */
public class FieldAnswer implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Date dateCreated;
	private Concept concept;
	private User creator;
	private Field field;

	// Constructors

	/** default constructor */
	public FieldAnswer() {
	}

	// Property accessors

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * 
	 */
	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FieldAnswer))
			return false;
		FieldAnswer fa = (FieldAnswer) obj;
		return field.equals(fa.field) && concept.equals(fa.concept);
	}
	
	public int hashCode() {
		return field.hashCode() + concept.hashCode();
	}

}