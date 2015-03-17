/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.FormResource;
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
	 * Creates new form from the given <code>Form</code>
	 * 
	 * @param form <code>Form</code> to duplicate
	 * @return newly duplicated <code>Form</code>
	 * @throws DAOException
	 */
	public Form duplicateForm(Form form) throws DAOException;
	
	/**
	 * Get form by internal form identifier
	 * 
	 * @param formId <code>Integer</code> internal identifier for requested Form
	 * @return requested <code>Form</code>
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
	 * @param name String name of the forms to get
	 * @return All forms with the given name, sorted by (alphabetically) by descending version
	 * @throws DAOException
	 */
	public List<Form> getFormsByName(String name) throws DAOException;
	
	/**
	 * Delete form from database. This is included for troubleshooting and low-level system
	 * administration. Ideally, this method should <b>never</b> be called &mdash; <code>Forms</code>
	 * should be <em>retired</em> and not <em>deleted</em> altogether (since many foreign key
	 * constraints depend on forms, deleting a form would require deleting all traces, and any
	 * historical trail would be lost). This method only clears form roles and attempts to delete
	 * the form record. If the form has been included in any other parts of the database (through a
	 * foreign key), the attempt to delete the form will violate foreign key constraints and fail.
	 * 
	 * @param form Form to delete
	 * @throws DAOException
	 */
	public void deleteForm(Form form) throws DAOException;
	
	/**
	 * Get all field types
	 * 
	 * @param includeRetired boolean - include retired field types as well?
	 * @return List<FieldTypes> object with all FieldTypes, possibly including retired ones
	 * @throws DAOException
	 */
	public List<FieldType> getAllFieldTypes(boolean includeRetired) throws DAOException;
	
	/**
	 * Get fieldType by internal identifier
	 * 
	 * @param fieldTypeId Internal Integer identifier of FieldType
	 * @return The FieldType with specified internal identifier
	 * @throws DAOException
	 */
	public FieldType getFieldType(Integer fieldTypeId) throws DAOException;
	
	/**
	 * Returns all forms in the database, possibly including retired ones
	 * 
	 * @param includeRetired boolean - include retired forms?
	 * @return List<Form> object of all forms, possibly including retired ones
	 * @throws DAOException
	 */
	public List<Form> getAllForms(boolean includeRetired) throws DAOException;
	
	/**
	 * Returns all FormFields in the database
	 * 
	 * @return List<FormField> object of all FormFields in the database
	 * @throws DAOException
	 */
	public List<FormField> getAllFormFields() throws DAOException;
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept,
	 *      java.util.Collection, boolean)
	 */
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force)
	        throws DAOException;
	
	/**
	 * @param search String phrase to search for
	 * @return list of fields in the database matching search phrase
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
	 * <p>
	 * This will fail if any other entities reference this field via a foreign key
	 * 
	 * @param field the Field to delete
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
	 * Deletes a FormField from the database. This will fail if any other entities reference this
	 * FormField via a foreign key
	 * 
	 * @param formField the FormField to delete
	 * @throws DAOException
	 */
	public void deleteFormField(FormField formField) throws DAOException;
	
	/**
	 * Returns all fields that match a broad range of (nullable) criteria
	 * 
	 * @param forms <code>Collection</code> of <code>Form</code> to which the requested Fields must
	 *            belong
	 * @param fieldTypes <code>Collection</code> of <code>FieldType</code> of which the requested
	 *            fields must be
	 * @param concepts <code>Collection</code> of <code>Concepts</code> which the fields must point
	 *            to
	 * @param tableNames <code>Collection of <code>TableName</code>s which the fields must point to
	 * @param attributeNames <code>Collection of <code>String</code> attribute names which the
	 *            fields must point to
	 * @param selectMultiple <code>Boolean</code> value that matching fields must have for
	 *            selectMultiple
	 * @param containsAllAnswers <code>Collection</code> of <code>FieldAnswer</code>s, all of which
	 *            a matching field must contain (not yet implemented)
	 * @param containsAnyAnswer <code>Collection</code> of <code>FieldAnswer</code>s, any one of
	 *            which a matching field must contain (not yet implemented)
	 * @param retired <code>Boolean</code> retired status that fields must match
	 * @return All Fields that match the criteria
	 */
	public List<Field> getFields(Collection<Form> forms, Collection<FieldType> fieldTypes, Collection<Concept> concepts,
	        Collection<String> tableNames, Collection<String> attributeNames, Boolean selectMultiple,
	        Collection<FieldAnswer> containsAllAnswers, Collection<FieldAnswer> containsAnyAnswer, Boolean retired)
	        throws DAOException;
	
	/**
	 * Get all forms that contain the given <code>Concept</code> as one of their fields. (Includes
	 * retired forms.)
	 * 
	 * @param concept the <code>Concept</code> to search through form fields for
	 * @return forms that contain a form field referencing the given concept
	 */
	public List<Form> getFormsContainingConcept(Concept concept) throws DAOException;
	
	/**
	 * Gets all forms that match all the criteria. Expects the list objects to be non-null
	 * 
	 * @param partialName String of partial name of form to search on
	 * @param published boolean - is the form published?
	 * @param encounterTypes Collection of <code>EncounterType</code>s that the form must represent
	 * @param retired boolean - is the form retired?
	 * @param containingAnyFormField Collection of <code>FormField</code>s, any one of which must be
	 *            contained in the form
	 * @param containingAllFormFields Collection of <code>FormField</code>s, all of which must be
	 *            contained in the form
	 * @param fields Collection of <code>Field</code>s that the form must contain
	 * @return All forms that match the criteria
	 * @see org.openmrs.api.FormService#getForms(java.lang.String, java.lang.Boolean,
	 *      java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection,
	 *      java.util.Collection)
	 */
	public List<Form> getForms(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) throws DAOException;
	
	/**
	 * @see #getForms(String, Boolean, Collection, Boolean, Collection, Collection, Collection)
	 */
	public Integer getFormCount(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) throws DAOException;
	
	/**
	 * Delete the given field type from the database
	 * 
	 * @param fieldType FieldType to delete
	 */
	public void deleteFieldType(FieldType fieldType) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Field getFieldByUuid(String uuid);
	
	public FieldAnswer getFieldAnswerByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public FieldType getFieldTypeByUuid(String uuid);
	
	/**
	 * Return fieldType  associated with given name
	 *
	 * @param name Name of the fileType to query
	 * @return fieldType object associate with given name
	 */
	public FieldType getFieldTypeByName(String name);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Form getFormByUuid(String uuid);
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public FormField getFormFieldByUuid(String uuid);
	
	/**
	 * Save the given field type to the database
	 * 
	 * @param fieldType FieldType to save to the database
	 * @return the newly saved field type
	 */
	public FieldType saveFieldType(FieldType fieldType) throws DAOException;
	
	/**
	 * Return a list of FormFields given a Field
	 * 
	 * @param field
	 * @return List of FormFields
	 */
	public List<FormField> getFormFieldsByField(Field field);
	
	/**
	 * @see FormService#getFormResource(java.lang.Integer) 
	 * @since 1.9
	 */
	public FormResource getFormResource(Integer formResourceId);
	
	/**
	 * @see FormService#getFormResourceByUuid(java.lang.String) 
	 * @since 1.9
	 */
	public FormResource getFormResourceByUuid(String uuid);
	
	/**
	 * @see FormService#getFormResource(org.openmrs.Form, java.lang.String)
	 * @since 1.9
	 */
	public FormResource getFormResource(Form form, String name);
	
	/**
	 * @see FormService#getFormResourcesForForm(org.openmrs.Form)
	 * @since 1.9
	 */
	public Collection<FormResource> getFormResourcesForForm(Form form);
	
	/**
	 * @see FormService#saveFormResource(org.openmrs.FormResource)
	 * @since 1.9
	 */
	public FormResource saveFormResource(FormResource formResource);
	
	/**
	 * @see FormService#purgeFormResource(org.openmrs.FormResource)
	 * @since 1.9
	 */
	public void deleteFormResource(FormResource formResource);
	
}
