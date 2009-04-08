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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Form
 * 
 * @version 1.0
 */
public class Form extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 845634L;
	
	// Fields
	
	private Integer formId;
	
	private String version;
	
	private Integer build;
	
	private Boolean published = false;
	
	private EncounterType encounterType;
	
	private String template;
	
	private String xslt;
	
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
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getFormId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setFormId(id);
		
	}
	
	/**
	 * @deprecated use {@link #setRetireReason(String)}
	 */
	public void setRetiredReason(String reason) {
		setRetireReason(reason);
		
	}
	
	/**
	 * @deprecated use {@link #getRetireReason()}
	 */
	public String getRetiredReason() {
		return getRetireReason();
	}
	
}
