package org.openmrs;

import java.util.Date;

/**
 * ConceptNumeric 
 */
public class ConceptNumeric implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptId;
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
	private Concept concept;
	private User userByCreator;
	private User userByChangedBy;

	// Constructors

	/** default constructor */
	public ConceptNumeric() {
	}

	/** constructor with id */
	public ConceptNumeric(Integer conceptId) {
		this.conceptId = conceptId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptId() {
		return this.conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

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
	public User getUserByCreator() {
		return this.userByCreator;
	}

	public void setUserByCreator(User userByCreator) {
		this.userByCreator = userByCreator;
	}

	/**
	 * 
	 */
	public User getUserByChangedBy() {
		return this.userByChangedBy;
	}

	public void setUserByChangedBy(User userByChangedBy) {
		this.userByChangedBy = userByChangedBy;
	}

}