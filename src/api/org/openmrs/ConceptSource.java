package org.openmrs;

import java.util.Date;

/**
 * ConceptSource 
 */
public class ConceptSource implements java.io.Serializable {

	public static final long serialVersionUID = 375L;

	// Fields

	private Integer conceptSourceId;
	private String name;
	private String description;
	private String hl7Code;
	private User creator;
	private Date dateCreated;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	// Constructors

	/** default constructor */
	public ConceptSource() {
	}

	/** constructor with id */
	public ConceptSource(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptSource) {
			ConceptSource c = (ConceptSource)obj;
			return (this.conceptSourceId.equals(c.getConceptSourceId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptSourceId() == null) return super.hashCode();
		return this.getConceptSourceId().hashCode();
	}

	/**
	 * @return Returns the conceptSourceId.
	 */
	public Integer getConceptSourceId() {
		return conceptSourceId;
	}

	/**
	 * @param conceptSourceId The conceptSourceId to set.
	 */
	public void setConceptSourceId(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
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
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the hl7Code.
	 */
	public String getHl7Code() {
		return hl7Code;
	}

	/**
	 * @param hl7Code The hl7Code to set.
	 */
	public void setHl7Code(String hl7Code) {
		this.hl7Code = hl7Code;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}


}