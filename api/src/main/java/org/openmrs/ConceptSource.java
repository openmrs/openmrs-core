/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * A concept source is defined as any institution that keeps a concept dictionary. Examples are
 * ICD9, ICD10, SNOMED, or any other OpenMRS implementation
 */
@Root
public class ConceptSource extends BaseOpenmrsMetadata implements Voidable, java.io.Serializable {
	
	public static final long serialVersionUID = 375L;
	
	// Fields
	
	private Integer conceptSourceId;
	
	private String hl7Code;
	
	// Constructors
	
	/** default constructor */
	public ConceptSource() {
	}
	
	/** constructor with id */
	public ConceptSource(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
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
	
	@Element
	public User getCreator() {
		return super.getCreator();
	}
	
	@Element
	public void setCreator(User creator) {
		super.setCreator(creator);
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Element
	public Date getDateCreated() {
		return super.getDateCreated();
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element
	public void setDateCreated(Date dateCreated) {
		super.setDateCreated(dateCreated);
	}
	
	/**
	 * @return Returns the dateVoided.
	 * @deprecated
	 */
	@Deprecated
	@Element(required = false)
	public Date getDateVoided() {
		return getDateRetired();
	}
	
	/**
	 * @param dateVoided The dateVoided to set.
	 * @deprecated
	 */
	@Deprecated
	@Element(required = false)
	public void setDateVoided(Date dateVoided) {
		setDateRetired(dateVoided);
	}
	
	@Element(data = true)
	public String getDescription() {
		return super.getDescription();
	}
	
	@Element(data = true)
	public void setDescription(String description) {
		super.setDescription(description);
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
	
	@Element(data = true)
	public String getName() {
		return super.getName();
	}
	
	@Element(data = true)
	public void setName(String name) {
		super.setName(name);
	}
	
	/**
	 * @return the voided status
	 * @deprecated use isRetired
	 */
	@Deprecated
	public Boolean isVoided() {
		return isRetired();
	}
	
	/**
	 * This method exists to satisfy spring and hibernates slightly bung use of Boolean object
	 * getters and setters.
	 * 
	 * @deprecated Use the "proper" isRetired method.
	 * @see #isVoided()
	 */
	@Deprecated
	@Attribute
	public Boolean getVoided() {
		return isVoided();
	}
	
	/**
	 * @param voided The voided status
	 * @deprecated use setRetired
	 */
	@Deprecated
	@Attribute
	public void setVoided(Boolean voided) {
		setRetired(voided);
	}
	
	/**
	 * @return Returns the openmrs user that voided this source
	 * @deprecated use getRetiredBy
	 */
	@Deprecated
	@Element(required = false)
	public User getVoidedBy() {
		return getRetiredBy();
	}
	
	/**
	 * @param voidedBy The openmrs user that voided this source
	 * @deprecated use setRetiredBy
	 */
	@Deprecated
	@Element(required = false)
	public void setVoidedBy(User voidedBy) {
		setRetiredBy(voidedBy);
	}
	
	/**
	 * @return Returns the reason this source was voided
	 * @deprecated use getRetireReason
	 */
	@Deprecated
	@Element(data = true, required = false)
	public String getVoidReason() {
		return getRetireReason();
	}
	
	/**
	 * @param voidReason The reason this source is voided
	 * @deprecated use setRetireReason
	 */
	@Deprecated
	@Element(data = true, required = false)
	public void setVoidReason(String voidReason) {
		setRetireReason(voidReason);
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptSourceId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptSourceId(id);
	}
}
