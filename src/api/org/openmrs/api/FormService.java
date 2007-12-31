package org.openmrs.api;

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
	 * @param form
	 * @param concept
	 * @return Formfield for this concept
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public FormField getFormField(Form form, Concept concept)
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