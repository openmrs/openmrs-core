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
package org.openmrs.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.db.FormDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FormService {

	public void setFormDAO(FormDAO dao);

	/**
	 * Create a new form
	 * @param form
	 * @throws APIException
	 */
	public Form createForm(Form form) throws APIException;

	/**
	 * Get form by internal form identifier
	 * @param formId internal identifier
	 * @return requested form
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Form getForm(Integer formId) throws APIException;
	
	/**
	 * Get all forms.  If publishedOnly is true, a form must be marked as
	 * 'published' to be included in the list
	 * 
	 * @param published
	 * @return List of forms
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Form> getForms(boolean publishedOnly) throws APIException;
	
	/**
	 * Get all forms.  If publishedOnly is true, a form must be marked as
	 * 'published' to be included in the list.  If includeRetired is true
	 * 'retired' must be set to false to be include in the list
	 * 
	 * @param publishedOnly
	 * @param includeRetired
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Form> getForms(boolean publishedOnly, boolean includeRetired) throws APIException;
	

	/**
	 * Save changes to form
	 * @param form
	 * @throws APIException
	 */
	public void updateForm(Form form) throws APIException;

	/**
	 * Duplicate this form and form_fields associated with this form
	 * 
	 * @param form
	 * @return New duplicated form
	 * @throws APIException
	 */
	public Form duplicateForm(Form form) throws APIException;

	/** 
	 * Mark form as voided (effectively deleting form without removing
	 * their data &mdash; since anything the form touched in the database
	 * will still have their internal identifier and point to the voided
	 * form for historical tracking purposes.
	 * 
	 * @param form
	 * @param reason
	 * @throws APIException
	 */
	public void retireForm(Form form, String reason) throws APIException;

	/**
	 * Clear voided flag for form (equivalent to an "undelete" or
	 * Lazarus Effect for form)
	 * 
	 * @param form
	 * @throws APIException
	 */
	public void unretireForm(Form form) throws APIException;

	/**
	 * Delete form from database. This is included for troubleshooting and
	 * low-level system administration. Ideally, this method should <b>never</b>
	 * be called &mdash; <code>Forms</code> should be <em>retired</em> and
	 * not <em>deleted</em> altogether (since many foreign key constraints
	 * depend on forms, deleting a form would require deleting all traces, and
	 * any historical trail would be lost).
	 * 
	 * This method only clears form roles and attempts to delete the form
	 * record. If the form has been included in any other parts of the database
	 * (through a foreign key), the attempt to delete the form will violate
	 * foreign key constraints and fail.
	 * 
	 * @param form
	 * @throws APIException
	 */
	public void deleteForm(Form form) throws APIException;

	/**
	 * Get all field types
	 * 
	 * @return field types list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<FieldType> getFieldTypes() throws APIException;

	/**
	 * Get fieldType by internal identifier
	 * 
	 * @param fieldType id
	 * @return fieldType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public FieldType getFieldType(Integer fieldTypeId) throws APIException;

	/**
	 * 
	 * @return list of forms in the db
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Form> getForms() throws APIException;

	/**
	 * Returns the distinct set of forms with which this concept is associated
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Set<Form> getForms(Concept c) throws APIException;

	/**
	 * @param form
	 * @return list of fields for a specific form
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<FormField> getFormFields(Form form) throws APIException;

	/**
	 * 
	 * @return list of fields in the db matching part of search term
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Field> findFields(String searchPhrase) throws APIException;

	/**
	 * 
	 * @return list of fields in the db for given concept
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Field> findFields(Concept concept) throws APIException;

	/**
	 * 
	 * @return list of fields in the db
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Field> getFields() throws APIException;

	/**
	 * 
	 * @param fieldId
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Field getField(Integer fieldId) throws APIException;

	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void createField(Field field) throws APIException;

	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void updateField(Field field) throws APIException;

	/**
	 * 
	 * @param field
	 * @throws APIException
	 */
	public void deleteField(Field field) throws APIException;

	/**
	 * 
	 * @param fieldId
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public FormField getFormField(Integer formFieldId) throws APIException;

	/**
	 * Finds the FormField defined for this form/concept combination
	 * 
	 * Calls {@link #getFormField(Form, Concept, Collection)} with an empty
	 * ignore list and with <code>force</code> set to false
	 * 
	 * @param form Form that this concept was found on 
	 * @param concept (question) on this form that is being requested
	 * @return Formfield for this concept on this form
	 * @throws APIException
	 * 
	 * @see {@link #getFormField(Form, Concept, Collection)}
	 */
	@Transactional(readOnly=true)
	public FormField getFormField(Form form, Concept concept)
			throws APIException;
	
	/**
	 * Finds the FormField defined for this form/concept combination 
	 * while discounting any form field found in the <code>ignoreFormFields</code>
	 * collection 
	 * 
	 * This method was added when needing to relate observations to form fields
	 * during a display.  The use case would be that you know a Concept for a obs, 
	 * which was defined on a form (via a formField).  You can relate the formFields
	 * to Concepts easily enough, but if a Form reuses a Concept in two separate FormFields
	 * you don't want to only associate that first formField with that concept.  So, keep 
	 * a running list of formFields you've seen and pass them back in here to rule them out.
	 * 
	 * @param form Form that this concept was found on 
	 * @param concept Concept (question) on this form that is being requested
	 * @param ignoreFormFields FormFields to ignore (aka already seen formfields) 
	 * @param force if true and there are zero matches because all formFields were ignored
	 * 		(because of ignoreFormFields) than the first result is returned 
	 * @return Formfield for this concept on this form
	 * 
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force)
			throws APIException;
	
	/**
	 * Create the given form field in the database
	 * 
	 * @param formField FormField to create
	 * @throws APIException
	 */
	public void createFormField(FormField formField) throws APIException;

	/**
	 * Update the given FormField in the database
	 * 
	 * @param formField FormField to update
	 * @throws APIException
	 */
	public void updateFormField(FormField formField) throws APIException;

	/**
	 * Delete the given form field from the database
	 * 
	 * @param formField FormField to remove from the database
	 * @throws APIException
	 */
	public void deleteFormField(FormField formField) throws APIException;

	/**
     * Search for forms with the given text in their name
     * 
     * @param text text in form name to search for
     * @param includeUnpublished
     * @param includeRetired
     * @return forms list
     */
	@Transactional(readOnly=true)
	public List<Form> findForms(String text, boolean includeUnpublished, boolean includeRetired);
	
}