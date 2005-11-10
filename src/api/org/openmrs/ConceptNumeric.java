package org.openmrs;

import java.util.Date;

/**
 * ConceptNumeric 
 */
public class ConceptNumeric implements java.io.Serializable {

	public static final long serialVersionUID = 47323L;

	// Fields

	private Concept concept;
	private Double hiAbsolute;
	private Double hiCritical;
	private Double hiNormal;
	private Double lowAbsolute;
	private Double lowCritical;
	private Double lowNormal;
	private String units;
	private Date dateCreated;
	private Boolean precise;
	private Date dateChanged;
	private User creator;
	private User changedBy;

	// Constructors

	/** default constructor */
	public ConceptNumeric() {
	}

	public boolean equals(Object obj) {
		if (obj instanceof ConceptNumeric) {
			ConceptNumeric c = (ConceptNumeric)obj;
			return (this.concept.equals(c.getConcept()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConcept() == null) return super.hashCode();
		return this.getConcept().hashCode();
	}

	// Property accessors

	/**
	 * 
	 */
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}

	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}

	/**
	 * 
	 */
	public Double getHiCritical() {
		return this.hiCritical;
	}

	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}

	/**
	 * 
	 */
	public Double getHiNormal() {
		return this.hiNormal;
	}

	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}

	/**
	 * 
	 */
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}

	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}

	/**
	 * 
	 */
	public Double getLowCritical() {
		return this.lowCritical;
	}

	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}

	/**
	 * 
	 */
	public Double getLowNormal() {
		return this.lowNormal;
	}

	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}

	/**
	 * 
	 */
	public String getUnits() {
		return this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

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
	public Boolean getPrecise() {
		return this.precise;
	}

	public void setPrecise(Boolean precise) {
		this.precise = precise;
	}

	/**
	 * 
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	public User getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

}