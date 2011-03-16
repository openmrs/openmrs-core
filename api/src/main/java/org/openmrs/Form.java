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

import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.FormConstants;

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
	@Override
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
	@Override
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
	 * @param formId
	 *            The formId to set.
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
	 * @param version
	 *            The version to set.
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
	 * @param build
	 *            The build number to set
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
	 * @param published
	 *            The published to set.
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
	 * @param encounterType
	 *            type of encounter associated with this form
	 */
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}
	
	/**
	 * Convenience method for accessing the FormService
	 * 
	 * TODO: remove this method when xslt and template properties / methods are removed
	 * 
	 * @return the current context's form service
	 * @throws APIException
	 */
	private FormService getFormService() throws APIException {
		FormService formService = Context.getFormService();
		if (formService == null)
			throw new APIException("Cannot retrieve form template; no service available from context");
		
		return formService;
	}
	
	/**
	 * @return Returns the template.
	 * @deprecated use
	 *             {@link org.openmrs.api.FormService#getFormResource(Form, String, String)}
	 */
	@Deprecated
	public String getTemplate() {
		FormService formService = this.getFormService();
		
		String template;
		try {
			template = (String) formService.getFormResource(this, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER,
			    FormConstants.FORM_RESOURCE_FORMENTRY_TEMPLATE);
		}
		catch (DAOException e) {
			// template does not exist
			return null;
		}
		
		// TODO use a conversion method in FormUtil, for various reasons (i.e.
		// compression)
		return template;
	}
	
	/**
	 * @param template
	 *            The template to set.
	 * @deprecated use
	 *             {@link org.openmrs.api.FormService#saveFormResource(Form, String, String, byte[])}
	 */
	@Deprecated
	public void setTemplate(String template) {
		FormService formService = this.getFormService();
		formService.saveFormResource(this, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER,
		    FormConstants.FORM_RESOURCE_FORMENTRY_TEMPLATE, template);
	}
	
	/**
	 * @return Returns the creator
	 * @deprecated use
	 *             {@link org.openmrs.api.FormService#getFormResource(Form, String, String)}
	 */
	@Deprecated
	public String getXslt() {
		FormService formService = this.getFormService();
		
		String xslt;
		try {
			xslt = (String) formService.getFormResource(this, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER,
			    FormConstants.FORM_RESOURCE_FORMENTRY_XSLT);
		}
		catch (DAOException e) {
			// xslt does not exist
			return null;
		}
		
		// TODO use a conversion method in FormUtil, for various reasons (i.e.
		// compression)
		return xslt;
	}
	
	/**
	 * @param xslt
	 *            the xslt to set.
	 * @deprecated use
	 *             {@link org.openmrs.api.FormService#saveFormResource(Form, String, String, byte[])}
	 */
	@Deprecated
	public void setXslt(String xslt) {
		FormService formService = this.getFormService();
		formService.saveFormResource(this, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER,
		    FormConstants.FORM_RESOURCE_FORMENTRY_XSLT, xslt);
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
	 * @param formFields
	 *            The formFields to set.
	 */
	public void setFormFields(Set<FormField> formFields) {
		this.formFields = formFields;
	}
	
	/**
	 * Adds a FormField to the list of form fields
	 * 
	 * @param formField
	 *            FormField to be added
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
	 * @param formField
	 *            FormField to be removed
	 */
	public void removeFormField(FormField formField) {
		if (formFields != null) {
			this.formFields.remove(formField);
		}
	}
	
	@Override
	public String toString() {
		if (formId == null)
			return "";
		return formId.toString();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getFormId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setFormId(id);
		
	}
	
	/**
	 * @deprecated use {@link #setRetireReason(String)}
	 */
	@Deprecated
	public void setRetiredReason(String reason) {
		setRetireReason(reason);
		
	}
	
	/**
	 * @deprecated use {@link #getRetireReason()}
	 */
	@Deprecated
	public String getRetiredReason() {
		return getRetireReason();
	}
	
}
