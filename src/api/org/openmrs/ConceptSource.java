/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * A concept source is defined as any institution that keeps a concept
 * dictionary. Examples are ICD9, ICD10, SNOMED, or any other OpenMRS
 * implementation
 * 
 */
@Root
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

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ConceptSource) {
			if (conceptSourceId == null) 
				return false;
			
			ConceptSource c = (ConceptSource) obj;
			return (conceptSourceId.equals(c.getConceptSourceId()));
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getConceptSourceId() == null)
			return super.hashCode();
		return conceptSourceId.hashCode();
	}

	/**
	 * @return Returns the conceptSourceId.
	 */
	@Attribute
	public Integer getConceptSourceId() {
		return conceptSourceId;
	}

	/**
	 * @param conceptSourceId The conceptSourceId to set.
	 */
	@Attribute
	public void setConceptSourceId(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}

	/**
	 * @return Returns the creator.
	 */
	@Element
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	@Element
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	@Element
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	@Element(required = false)
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	@Element(required = false)
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the description.
	 */
	@Element(data = true)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	@Element(data = true)
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the hl7Code.
	 */
	@Attribute
	public String getHl7Code() {
		return hl7Code;
	}

	/**
	 * @param hl7Code The hl7Code to set.
	 */
	@Attribute
	public void setHl7Code(String hl7Code) {
		this.hl7Code = hl7Code;
	}

	/**
	 * @return Returns the name.
	 */
	@Element(data = true)
	public String getName() {
		return name;
	}

	/**
	 * @param name The concept source name to set.
	 */
	@Element(data = true)
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return the voided status
	 */
	public Boolean isVoided() {
		return voided;
	}

	/**
	 * This method exists to satisfy spring and hibernates slightly bung use of
	 * Boolean object getters and setters.
	 * 
	 * @deprecated Use the "proper" isVoided method.
	 * @see #isVoided()
	 */
	@Attribute
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided status
	 */
	@Attribute
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the openmrs user that voided this source
	 */
	@Element(required = false)
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The openmrs user that voided this source
	 */
	@Element(required = false)
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the reason this source was voided
	 */
	@Element(data = true, required = false)
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The reason this source is voided
	 */
	@Element(data = true, required = false)
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

}