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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Form
 * 
 * @version 1.0
 */
public class Form implements java.io.Serializable {
	
	public static final long serialVersionUID = 845634L;
	
	// Fields
	
	private Integer formId;
	
	private String name;
	
	private String version;
	
	private Integer build;
	
	private Boolean published = false;
	
	private String description;
	
	private EncounterType encounterType;
	
	private String template;
	
	private String xslt;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Boolean retired = false;
	
	private User retiredBy;
	
	private Date dateRetired;
	
	private String retiredReason;
	
	private Set<FormField> formFields;
	
	// Constructors
	
	/** default constructor */
	public Form() {
	}
	
	/**
	 * Constructor with id
	 * 
	 * @should set formId with given parameter
	 */
	public Form(Integer formId) {
		this.formId = formId;
	}
	
	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 * @should have equal form objects by form id
	 * @should not have equal form objects by formId
	 * @should have equal form objects with no formId
	 * @should not have equal form objects when one has null formId
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Form) {
			Form f = (Form) obj;
			if (this.getFormId() != null && f.getFormId() != null)
				return (this.getFormId().equals(f.getFormId()));
		}
		
		// default to comparing the object pointers
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @should get hashCode even with null attributes
	 */
	public int hashCode() {
		if (this.getFormId() == null)
			return super.hashCode();
		return this.getFormId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the formId.
	 */
	public Integer getFormId() {
		return formId;
	}
	
	/**
	 * @param formId The formId to set.
	 */
	public void setFormId(Integer formId) {
		this.formId = formId;
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
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return Returns the build number
	 */
	public Integer getBuild() {
		return build;
	}
	
	/**
	 * @param build The build number to set
	 */
	public void setBuild(Integer build) {
		this.build = build;
	}
	
	/**
	 * @return Returns the published.
	 */
	public Boolean getPublished() {
		return published;
	}
	
	/**
	 * @param published The published to set.
	 */
	public void setPublished(Boolean published) {
		this.published = published;
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
	 * @return the type of encounter associated with this form
	 */
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	/**
	 * @param encounterType type of encounter associated with this form
	 */
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
	/**
	 * @return Returns the template.
	 */
	public String getTemplate() {
		return template;
	}
	
	/**
	 * @param template The template to set.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	/**
	 * @return Returns the creator
	 */
	public String getXslt() {
		return xslt;
	}
	
	public void setXslt(String xslt) {
		this.xslt = xslt;
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
	 * @return Returns the retired status.
	 */
	public Boolean isRetired() {
		return retired;
	}
	
	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @param retired The retired status to set.
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @return Returns the retiredBy.
	 */
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @param retiredBy The retiredBy to set.
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @return Returns the dateRetired.
	 */
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @param dateRetired The dateRetired to set.
	 */
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @return Returns the retiredReason.
	 */
	public String getRetiredReason() {
		return retiredReason;
	}
	
	/**
	 * @param retiredReason The retiredReason to set.
	 */
	public void setRetiredReason(String retiredReason) {
		this.retiredReason = retiredReason;
	}
	
	/**
	 * @return Returns the formFields.
	 */
	public Set<FormField> getFormFields() {
		return formFields;
	}
	
	/**
	 * @return Returns the formFields.
	 */
	public List<FormField> getOrderedFormFields() {
		if (this.formFields != null) {
			List<FormField> fieldList = new ArrayList<FormField>();
			Set<FormField> fieldSet = new HashSet<FormField>();
			fieldSet.addAll(this.formFields);
			
			int fieldSize = fieldSet.size();
			
			for (int i = 0; i < fieldSize; i++) {
				int fieldNum = 0;
				FormField next = null;
				
				for (FormField ff : fieldSet) {
					if (ff.getFieldNumber() != null) {
						if (ff.getFieldNumber().intValue() < fieldNum || fieldNum == 0) {
							fieldNum = ff.getFieldNumber().intValue();
							next = ff;
						}
					} else {
						if (fieldNum == 0) {
							next = ff;
						}
					}
				}
				
				fieldList.add(next);
				fieldSet.remove(next);
			}
			
			return fieldList;
		} else {
			return null;
		}
	}
	
	/**
	 * @param formFields The formFields to set.
	 */
	public void setFormFields(Set<FormField> formFields) {
		this.formFields = formFields;
	}
	
	/**
	 * Adds a FormField to the list of form fields
	 * 
	 * @param formField FormField to be added
	 */
	public void addFormField(FormField formField) {
		if (formFields == null)
			formFields = new HashSet<FormField>();
		if (!formFields.contains(formField) && formField != null) {
			formField.setForm(this);
			this.formFields.add(formField);
		}
	}
	
	/**
	 * Removes a FormField from the list of form fields
	 * 
	 * @param formField FormField to be removed
	 */
	public void removeFormField(FormField formField) {
		if (formFields != null) {
			this.formFields.remove(formField);
		}
	}
	
	public String toString() {
		if (formId == null)
			return "";
		return formId.toString();
	}
	
}
