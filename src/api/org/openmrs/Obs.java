package org.openmrs;

import java.util.Date;

/**
 * Obs 
 */
public class Obs implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	protected Integer obsId;
	protected Concept conceptId;
	protected Date obsDatetime;
	protected Integer obsGroupId;
	protected Concept valueCoded;
	protected Integer valueGroupId;
	protected Boolean valueBoolean;
	protected Date valueDatetime;
	protected Double valueNumeric;
	protected String valueModifier;
	protected String valueText;
	protected String comment;
	protected Patient patient;
	protected Order order;
	protected Location location;
	protected Encounter encounter;
	protected User creator;
	protected Date dateCreated;
	protected Boolean voided;
	protected User voidedBy;
	protected Date dateVoided;
	protected String voidReason;

	// Constructors

	/** default constructor */
	public Obs() {
	}

	/** constructor with id */
	public Obs(Integer obsId) {
		this.obsId = obsId;
	}

	/** determine if the current observation is complex
	 *  --overridden in extending ComplexObs.java class
	 */
	public Boolean isComplexObs() {
		return false;
	}
	
	// Property accessors

	/**
	 * 
	 */
	public Integer getObsId() {
		return this.obsId;
	}

	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	/**
	 * 
	 */
	public Date getObsDatetime() {
		return this.obsDatetime;
	}

	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}

	/**
	 * 
	 */
	public Integer getObsGroupId() {
		return this.obsGroupId;
	}

	public void setObsGroupId(Integer obsGroupId) {
		this.obsGroupId = obsGroupId;
	}

	/**
	 * 
	 */
	public Integer getValueGroupId() {
		return this.valueGroupId;
	}

	public void setValueGroupId(Integer valueGroupId) {
		this.valueGroupId = valueGroupId;
	}

	/**
	 * 
	 */
	public Boolean getValueBoolean() {
		return this.valueBoolean;
	}

	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	/**
	 * 
	 */
	public Date getValueDatetime() {
		return this.valueDatetime;
	}

	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}

	/**
	 * 
	 */
	public Double getValueNumeric() {
		return this.valueNumeric;
	}

	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	/**
	 * 
	 */
	public String getValueModifier() {
		return this.valueModifier;
	}

	public void setValueModifier(String valueModifier) {
		this.valueModifier = valueModifier;
	}

	/**
	 * 
	 */
	public String getValueText() {
		return this.valueText;
	}

	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	/**
	 * 
	 */
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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
	public Boolean getVoided() {
		return this.voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * 
	 */
	public Date getDateVoided() {
		return this.dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * 
	 */
	public String getVoidReason() {
		return this.voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * 
	 */
	public Patient getPatient() {
		return this.patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * 
	 */
	public Concept getConceptId() {
		return this.conceptId;
	}

	public void setConceptId(Concept conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * 
	 */
	public Concept getvalueCoded() {
		return this.valueCoded;
	}

	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}

	/**
	 * 
	 */
	public Order getOrder() {
		return this.order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * 
	 */
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * 
	 */
	public Encounter getEncounter() {
		return this.encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
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
	public User getVoidedBy() {
		return this.voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

}