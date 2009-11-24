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
package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
// TODO this should extend BaseOpenmrsMetadata and use retired instead of voided
public class ReportObjectWrapper extends BaseOpenmrsObject {
	
	private Integer reportObjectId; // database primary key
	
	private String name;
	
	private String description;
	
	private String xml;
	
	private String type;
	
	private String subType;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date dateVoided;
	
	private String voidReason;
	
	public ReportObjectWrapper() {
		// empty constructor
		this.xml = null;
		this.type = null;
		this.subType = null;
	}
	
	public ReportObjectWrapper(AbstractReportObject obj) {
		this.xml = null;
		this.type = null;
		this.subType = null;
		setReportObject(obj);
	}
	
	/**
	 * @return Returns the subType.
	 */
	public String getSubType() {
		return subType;
	}
	
	/**
	 * @param subType The subType to set.
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return Returns the reportObjectId.
	 */
	public Integer getReportObjectId() {
		return reportObjectId;
	}
	
	/**
	 * @param reportObjectId The reportObjectId to set.
	 */
	public void setReportObjectId(Integer reportObjectId) {
		this.reportObjectId = reportObjectId;
		
	}
	
	/**
	 * @return Returns the name;
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
	 * @return Returns the description
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
	
	public void setXml(String xml) {
		this.xml = xml;
	}
	
	public String getXml() {
		return xml;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
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
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	
	/**
	 * Helper function that does a null-safe test for equality between two objects. Does not check
	 * for type-safety.
	 * 
	 * @return Whether or not two objects (of the same class) are equal.
	 */
	public static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	public String toString() {
		return this.getReportObjectId() + ", " + this.getName() + ", " + this.getDescription() + ", " + this.getType()
		        + ", " + this.getSubType();
	}
	
	public void setReportObject(AbstractReportObject obj) {
		this.setReportObjectId(obj.getReportObjectId());
		this.setName(obj.getName());
		this.setDescription(obj.getDescription());
		this.setType(obj.getType());
		this.setSubType(obj.getSubType());
		this.setUuid(obj.getUuid());
		/*
		this.setCreator(obj.getCreator());
		this.setDateCreated(obj.getDateCreated());
		this.setChangedBy(obj.getChangedBy());
		this.setDateChanged(obj.getDateChanged());
		this.setVoided(obj.getVoided());
		this.setVoidedBy(obj.getVoidedBy());
		this.setDateVoided(obj.getDateVoided());
		this.setVoidReason(obj.getVoidReason());
		*/

		ReportObjectXMLEncoder roxe = new ReportObjectXMLEncoder(obj);
		this.xml = roxe.toXmlString();
	}
	
	public AbstractReportObject getReportObject() {
		if (xml != null) {
			ReportObjectXMLDecoder roxd = new ReportObjectXMLDecoder(this.xml);
			AbstractReportObject reportObj = roxd.toAbstractReportObject();
			reportObj.setUuid(this.getUuid());
			return reportObj;
		} else {
			return null;
		}
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getReportObjectId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setReportObjectId(id);
	}
	
}
