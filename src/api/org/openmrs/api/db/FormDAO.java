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
package org.openmrs.api.db;

import java.util.Collection;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;

/**
 * Database access functions for the Form, FormField, and Field objects
 */
public interface FormDAO {
	
	/**
	 * @see FormService#saveForm(Form)
	 */
	public Form saveForm(Form form) throws DAOException;

	/**
	 * Creates new form from the given <code>form</code>
	 * 
	 * @param form Form to duplicate
	 * @return newly duplicated Form
	 * @throws DAOException
	 */
	public Form duplicateForm(Form form) throws DAOException;
	
	/**
	 * Get form by internal form identifier
	 * @param formId internal identifier
	 * @return requested form
	 * @throws DAOException
	 */
	public Form getForm(Integer formId) throws DAOException;
	
	/**
	 * Get form by exact name and version
	 * 
	 * @param name the name of the form to get
	 * @param version the version of the form to get
	 * @return the form with the exact name and version given
	 * @throws DAOException
	 */
	public Form getForm(String name, String version) throws DAOException;
	
	/**
	 * Gets all forms with the given name, sorted (alphabetically) by descending version
	 * 
	 * @param name the name of the forms to get
	 * @return All forms with the given name, sorted by (alphabetically) by descending version
	 * @throws DAOException
	 */
	public List<Form> getFormsByName(String name) throws DAOException;
	
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
	 * @throws DAOException
	 */
	public void deleteForm(Form form) throws DAOException;
	
	/**
	 * Get all field types
	 * 
	 * @param includeRetired true/false whether to also include retired types
	 * @return field types list
	 * @throws DAOException
	 */
	public List<FieldType> getAllFieldTypes(boolean includeRetired) throws DAOException;

	/**
	 * Get fieldType by internal identifier
	 * 
	 * @param fieldType id
	 * @return fieldType with given internal identifier
	 * @throws DAOException
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws DAOException;
	
	/**
	 * Returns all forms in the database, possibly including retired ones
	 * 
	 * @param includeRetired whether or not to include retired forms
	 * @return all forms, possibly including retired ones
	 * @throws DAOException
	 */
	public List<Form> getAllForms(boolean includeRetired) throws DAOException;

	/**
	 * Returns all FormFields in the database
	 * 
	 * @return
	 * @throws DAOException
	 */
	public List<FormField> getAllFormFields() throws DAOException;
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept, java.util.Collection, boolean)
	 */
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force) throws DAOException;

	/**
	 * 
	 * @return list of fields in the db matching search phrase
	 * @throws DAOException
	 */
	public List<Field> getFields(String search) throws DAOException;
	
	/**
	 * Returns all fields in the database, possibly including retired ones
	 * 
	 * @param includeRetired whether or not to return retired fields
	 * @return all fields in the database, possibly including retired ones
	 * @throws DAOException
	 */
	public List<Field> getAllFields(boolean includeRetired) throws DAOException;

	/**
	 * @see FormService#getField(Integer)
	 */	
	public Field getField(Integer fieldId) throws DAOException;
	
	/**
	 * @see FormService#saveField(Field)
	 */
	public Field saveField(Field field) throws DAOException;
		
	/**
	 * Deletes a field from the database.
	 * This will fail if any other entities reference this field via a foreign key 
	 * @param field the field to delete
	 * @throws DAOException
	 */
	public void deleteField(Field field) throws DAOException;
	
	/**
	 * @see FormService#getFormField(Integer)
	 */
	public FormField getFormField(Integer formFieldId) throws DAOException;

	/**
	 * @see FormService#saveFormField(FormField)
	 */
	public FormField saveFormField(FormField formField) throws DAOException;
	
	/**
	 * Deletes a FormField from the database.
	 * This will fail if any other entities reference this FormField via a foreign key
	 * @param formField the FormField to delete
	 * @throws DAOException
	 */
	public void deleteFormField(FormField formField) throws DAOException;
	
	/**
     * Returns all fields that match a broad range of (nullable) criteria
	 * 
     * @param forms fields belonging to any of these forms
     * @param fieldTypes fields of any of these types
     * @param concepts fields pointing to any of these concepts
     * @param tableNames fields pointing to any of these table names
     * @param attributeNames fields pointing to any of these attribute names
     * @param selectMultiple fields with this selectMultiple value
     * @param containsAllAnswers fields that contain all these answers (not yet implemented)
     * @param containsAnyAnswer fields that contain any of these answers (not yet implemented)
     * @param retired fields with this retired status
	 * @return
	 */
    public List<Field> getFields(Collection<Form> forms,
            Collection<FieldType> fieldTypes, Collection<Concept> concepts,
            Collection<String> tableNames, Collection<String> attributeNames,
            Boolean selectMultiple, Collection<FieldAnswer> containsAllAnswers,
            Collection<FieldAnswer> containsAnyAnswer, Boolean retired) throws DAOException;
	
	/**
     * Get all forms that contain the given concept as one of their fields. (Includes retired forms.)
	 * 
     * @param concept the concept to search through form fields for
     * @return forms that contain a form field referencing the given concept
	 */
    public List<Form> getFormsContainingConcept(Concept concept) throws DAOException;

	/**
	 * Gets all forms that match all the criteria.  Expects the list objects
	 * to be non-null
	 * 
	 * @param partialNameSearch partial search of name
	 * @param published whether the form is published
	 * @param encounterTypes whether the form has any of these encounter types
	 * @param retired whether the form is retired
	 * @param containingAnyFormField includes forms that contain any of the specified FormFields
	 * @param containingAllFormFields includes forms that contain all of the specified FormFields
	 * @param fields whether the form has any of these fields
	 * @return All forms that match the 
     * @see org.openmrs.api.FormService#getForms(java.lang.String, java.lang.Boolean, java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection, java.util.Collection)
	 */
    public List<Form> getForms(String partialName, Boolean published,
            Collection<EncounterType> encounterTypes, Boolean retired,
            Collection<FormField> containingAnyFormField,
            Collection<FormField> containingAllFormFields,
            Collection<Field> fields) throws DAOException;
	
	/**
     * Delete the given field type from the database
	 * 
     * @param fieldType
	 */
    public void deleteFieldType(FieldType fieldType) throws DAOException;

	/**
     * Save the given field type to the database
     * 
     * @param fieldType
     * @return the newly saved field type
     */
    public FieldType saveFieldType(FieldType fieldType) throws DAOException;
	
}
