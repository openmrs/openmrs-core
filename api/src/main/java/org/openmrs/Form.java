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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Form
 *
 * @version 1.0
 */
public class Form extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 845634L;
	
	// Fields
	
	private Integer formId;
	
	private String version;
	
	private Integer build;
	
	private Boolean published = false;
	
	private EncounterType encounterType;
	
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
			List<FormField> fieldList = new ArrayList<>();
			Set<FormField> fieldSet = new HashSet<>(this.formFields);
			
			int fieldSize = fieldSet.size();
			
			for (int i = 0; i < fieldSize; i++) {
				int fieldNum = 0;
				FormField next = null;
				
				for (FormField ff : fieldSet) {
					if (ff.getFieldNumber() != null) {
						if (ff.getFieldNumber() < fieldNum || fieldNum == 0) {
							fieldNum = ff.getFieldNumber();
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
		if (formFields == null) {
			formFields = new HashSet<>();
		}
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
	
	@Override
	public String toString() {
		if (formId == null) {
			return "";
		}
		return formId.toString();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getFormId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFormId(id);
		
	}
}
