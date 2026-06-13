/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

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
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

/**
 * This service contains methods relating to Form, FormField, and Field. Methods relating to
 * FieldType are in AdministrationService
 */
public interface FormService extends OpenmrsService {

	/**
	 * Create or update the given Form in the database
	 * <p>
	 * <strong>Should</strong> save given form successfully<br/>
	 * <strong>Should</strong> update an existing form<br/>
	 * <strong>Should</strong> throw an error when trying to save an existing form while forms are
	 * locked<br/>
	 * <strong>Should</strong> throw an error when trying to save a new form while forms are locked
	 *
	 * @param form the Form to save
	 * @return the Form that was saved
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public Form saveForm(Form form) throws APIException;

	/**
	 * Get form by internal form identifier
	 * <p>
	 * <strong>Should</strong> return null if no form exists with given formId<br/>
	 * <strong>Should</strong> return the requested form
	 *
	 * @param formId internal identifier
	 * @return requested form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Form getForm(Integer formId) throws APIException;

	/**
	 * Get form by exact name match. If there are multiple forms with this name, then this returns the
	 * one with the highest version (sorted alphabetically)
	 * <p>
	 * <strong>Should</strong> return null if no form has the exact form name
	 *
	 * @param name exact name of the form to fetch
	 * @return requested form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Form getForm(String name) throws APIException;

	/**
	 * Get Form by its UUID
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @param uuid
	 * @return form or null
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Form getFormByUuid(String uuid) throws APIException;

	/**
	 * Get form by exact name &amp; version match. If version is null, then this method behaves like
	 * {@link #getForm(String)}
	 * <p>
	 * <strong>Should</strong> get the specific version of the form with the given name
	 *
	 * @param name exact name of the form to fetch
	 * @param version exact version of the form to fetch
	 * @return requested form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Form getForm(String name, String version) throws APIException;

	/**
	 * Gets all Forms, including retired ones.
	 * <p>
	 * <strong>Should</strong> return all forms including retired
	 *
	 * @return all Forms, including retired ones
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getAllForms() throws APIException;

	/**
	 * Gets all forms, possibly including retired ones
	 * <p>
	 * <strong>Should</strong> return retired forms if includeRetired is true<br/>
	 * <strong>Should</strong> not return retired forms if includeRetired is false
	 *
	 * @param includeRetired whether or not to return retired forms
	 * @return all forms, possibly including retired ones
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getAllForms(boolean includeRetired) throws APIException;

	/**
	 * Gets all forms with name similar to the given name. (The precise fuzzy matching algorithm is not
	 * specified.)
	 * <p>
	 * <strong>Should</strong> match forms with partial match on name<br/>
	 * <strong>Should</strong> only return one form per name if onlyLatestVersion is true
	 *
	 * @param fuzzyName approximate name to match
	 * @param onlyLatestVersion whether or not to return only the latest version of each form (by name)
	 * @return forms with names similar to fuzzyName
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getForms(String fuzzyName, boolean onlyLatestVersion);

	/**
	 * Gets all forms that match all the (nullable) criteria
	 * <p>
	 * <strong>Should</strong> get multiple of the same form by field<br/>
	 * <strong>Should</strong> return duplicate form when given fields included in form multiple
	 * times<br/>
	 * <strong>Should</strong> only return published forms when given published equals true<br/>
	 * <strong>Should</strong> return both published and unpublished when given published is null<br/>
	 * <strong>Should</strong> match to forms with fuzzy partialNameSearch<br/>
	 * <strong>Should</strong> return forms with encounterType in given encounterTypes<br/>
	 * <strong>Should</strong> return unretired forms when retired equals false<br/>
	 * <strong>Should</strong> return retired forms when retired equals true<br/>
	 * <strong>Should</strong> return all forms including retired and unretired when retired is
	 * null<br/>
	 * <strong>Should</strong> return forms containing all form fields in containingAllFormFields<br/>
	 * <strong>Should</strong> return forms that have any matching formFields in containingAnyFormField
	 *
	 * @param partialNameSearch partial search of name
	 * @param published whether the form is published
	 * @param encounterTypes whether the form has any of these encounter types
	 * @param retired whether the form is retired
	 * @param containingAnyFormField includes forms that contain any of the specified FormFields
	 * @param containingAllFormFields includes forms that contain all of the specified FormFields
	 * @param fields whether the form has any of these fields. If a field is used more than once on a
	 *            form, that form is returning more than once in this list
	 * @return All forms that match the criteria
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getForms(String partialNameSearch, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields);

	/**
	 * Same as
	 * {@link #getForms(String, Boolean, Collection, Boolean, Collection, Collection, Collection)}
	 * except that it returns an integer that is the size of the list that would be returned
	 *
	 * @see #getForms(String, Boolean, Collection, Boolean, Collection, Collection, Collection)
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Integer getFormCount(String partialNameSearch, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields);

	/**
	 * Returns all published forms (not including retired ones)
	 * <p>
	 * <strong>Should</strong> only return published forms that are not retired
	 *
	 * @return all published non-retired forms
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getPublishedForms() throws APIException;

	/**
	 * Audit form, consolidate similar fields
	 * <p>
	 * <strong>Should</strong> should merge fields with similar attributes
	 *
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public int mergeDuplicateFields() throws APIException;

	/**
	 * Duplicate this form and form_fields associated with this form
	 * <p>
	 * <strong>Should</strong> clear changed details and update creation details<br/>
	 * <strong>Should</strong> give a new uuid to the duplicated form<br/>
	 * <strong>Should</strong> copy resources for old form to new form<br/>
	 * <strong>Should</strong> throw an error when trying to duplicate a form while forms are locked
	 *
	 * @param form
	 * @return New duplicated form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public Form duplicateForm(Form form) throws APIException;

	/**
	 * Retires the Form, leaving it in the database, but removing it from data entry screens
	 * <p>
	 * <strong>Should</strong> set the retired bit before saving
	 *
	 * @param form the Form to retire
	 * @param reason the retiredReason to set
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void retireForm(Form form, String reason) throws APIException;

	/**
	 * Unretires a Form that had previous been retired.
	 * <p>
	 * <strong>Should</strong> unset the retired bit before saving
	 *
	 * @param form the Form to revive
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void unretireForm(Form form) throws APIException;

	/**
	 * Completely remove a Form from the database. This is not reversible. It will fail if this form has
	 * already been used to create Encounters
	 * <p>
	 * <strong>Should</strong> delete given form successfully<br/>
	 * <strong>Should</strong> delete form resources for deleted form<br/>
	 * <strong>Should</strong> throw an error when trying to delete a form while forms are locked
	 *
	 * @param form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void purgeForm(Form form) throws APIException;

	/**
	 * Completely remove a Form from the database. This is not reversible. !! WARNING: Calling this
	 * method with cascade=true can be very destructive !!
	 * <p>
	 * <strong>Should</strong> throw APIException if cascade is true
	 *
	 * @param form
	 * @param cascade whether or not to cascade delete all dependent objects (including encounters!)
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void purgeForm(Form form, boolean cascade) throws APIException;

	/**
	 * Get all field types in the database including the retired ones
	 * <p>
	 * <strong>Should</strong> also get retired field types
	 *
	 * @return list of all field types
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FIELD_TYPES)
	public List<FieldType> getAllFieldTypes() throws APIException;

	/**
	 * Get all field types in the database with or without retired ones
	 * <p>
	 * <strong>Should</strong> get all field types including retired when includeRetired equals
	 * true<br/>
	 * <strong>Should</strong> get all field types excluding retired when includeRetired equals false
	 *
	 * @param includeRetired true/false whether to include the retired field types
	 * @return list of all field types
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FIELD_TYPES)
	public List<FieldType> getAllFieldTypes(boolean includeRetired) throws APIException;

	/**
	 * Get fieldType by internal identifier
	 * <p>
	 * <strong>Should</strong> return null when no field type matching given id
	 *
	 * @param fieldTypeId Integer id of FieldType to get
	 * @return fieldType with given internal identifier
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FIELD_TYPES)
	public FieldType getFieldType(Integer fieldTypeId) throws APIException;

	/**
	 * Get FieldType by its UUID
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @param uuid
	 * @return field type or null
	 */
	public FieldType getFieldTypeByUuid(String uuid) throws APIException;

	/**
	 * Get FieldType by its name
	 * <p>
	 * <strong>Should</strong> find object given valid name<br/>
	 * <strong>Should</strong> return null if no object found with given name
	 *
	 * @since 1.11
	 * @param name
	 * @return field type or null
	 */
	public FieldType getFieldTypeByName(String name) throws APIException;

	/**
	 * Returns all forms that contain the given concept as a field in their schema. (includes retired
	 * forms)
	 * <p>
	 * <strong>Should</strong> get forms with field matching given concept<br/>
	 * <strong>Should</strong> get all forms for concept
	 *
	 * @param concept the concept to search for in forms
	 * @return forms containing the specified concept in their schema
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Form> getFormsContainingConcept(Concept concept) throws APIException;

	/**
	 * Returns all FormFields in the database
	 * <p>
	 * <strong>Should</strong> get all form fields including retired
	 *
	 * @return all FormFields in the database
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<FormField> getAllFormFields() throws APIException;

	/**
	 * Find all Fields whose names are similar to or contain the given phrase. (The exact similarity
	 * algorithm is unspecified.) (includes retired fields)
	 * <p>
	 * <strong>Should</strong> get fields with name matching fuzzySearchPhrase at beginning<br/>
	 * <strong>Should</strong> get fields with name matching fuzzySearchPhrase at middle<br/>
	 * <strong>Should</strong> get fields with name matching fuzzySearchPhrase at end<br/>
	 * <strong>Should</strong> return fields in alphabetical order by name
	 *
	 * @param fuzzySearchPhrase
	 * @return Fields with names similar to or containing the given phrase
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Field> getFields(String fuzzySearchPhrase) throws APIException;

	/**
	 * Finds all Fields that point to the given concept, including retired ones.
	 * <p>
	 * <strong>Should</strong> get fields with concept matching given concept
	 *
	 * @param concept the concept to search for in the Field table
	 * @return fields that point to the given concept
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Field> getFieldsByConcept(Concept concept) throws APIException;

	/**
	 * Fetches all Fields in the database, including retired ones
	 * <p>
	 * <strong>Should</strong> get all fields including retired
	 *
	 * @return all Fields
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Field> getAllFields() throws APIException;

	/**
	 * Fetches all Fields in the database, possibly including retired ones
	 * <p>
	 * <strong>Should</strong> get all fields including retired when includeRetired is true<br/>
	 * <strong>Should</strong> get all fields excluding retired when includeRetired is false
	 *
	 * @param includeRetired whether or not to include retired Fields
	 * @return all Fields
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Field> getAllFields(boolean includeRetired) throws APIException;

	/**
	 * Returns all Fields that match these (nullable) criteria
	 * <p>
	 * <strong>Should</strong> get fields with form in given forms<br/>
	 * <strong>Should</strong> get fields with type in given fieldTypes<br/>
	 * <strong>Should</strong> get fields with concept in given concepts<br/>
	 * <strong>Should</strong> get fields with tableName in given tableNames<br/>
	 * <strong>Should</strong> get fields with attributeName in given attributeNames<br/>
	 * <strong>Should</strong> get fields with selectMultiple equals true when given selectMultiple
	 * equals true
	 *
	 * @param forms on any of these Forms
	 * @param fieldTypes having any of these FieldTypes
	 * @param concepts for any of these Concepts
	 * @param tableNames for any of these table names
	 * @param attributeNames for any of these attribute names
	 * @param selectMultiple whether to return only select-multi fields
	 * @param containsAllAnswers fields with all the following answers
	 * @param containsAnyAnswer fields with any of the following answers
	 * @param retired only retired/unretired fields
	 * @return all Fields matching the given criteria
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public List<Field> getFields(Collection<Form> forms, Collection<FieldType> fieldTypes, Collection<Concept> concepts,
	        Collection<String> tableNames, Collection<String> attributeNames, Boolean selectMultiple,
	        Collection<FieldAnswer> containsAllAnswers, Collection<FieldAnswer> containsAnyAnswer, Boolean retired)
	        throws APIException;

	/**
	 * Gets a Field by internal database id
	 * <p>
	 * <strong>Should</strong> return null if no field exists with given fieldId
	 *
	 * @param fieldId the id of the Field to fetch
	 * @return the Field with the given id
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public Field getField(Integer fieldId) throws APIException;

	/**
	 * Get Field by its UUID
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @param uuid
	 * @return field or null
	 */
	public Field getFieldByUuid(String uuid) throws APIException;

	/**
	 * Get FieldAnswer by its UUID
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @param uuid
	 * @return field answer or null
	 */
	public FieldAnswer getFieldAnswerByUuid(String uuid) throws APIException;

	/**
	 * Creates or updates the given Field
	 * <p>
	 * <strong>Should</strong> save given field successfully<br/>
	 * <strong>Should</strong> update an existing field
	 *
	 * @param field the Field to save
	 * @return the Field that was saved
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public Field saveField(Field field) throws APIException;

	/**
	 * Completely removes a Field from the database. Not reversible.
	 * <p>
	 * <strong>Should</strong> delete given field successfully
	 *
	 * @param field the Field to purge
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void purgeField(Field field) throws APIException;

	/**
	 * Completely removes a Field from the database. Not reversible. !! WARNING: calling this with
	 * cascade=true can be very destructive !!
	 * <p>
	 * <strong>Should</strong> throw APIException if cascade is true
	 *
	 * @param field the Field to purge
	 * @param cascade whether to cascade delete all FormFields pointing to this field
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void purgeField(Field field, boolean cascade) throws APIException;

	/**
	 * Gets a FormField by internal database id
	 * <p>
	 * <strong>Should</strong> return null if no formField exists with given id
	 *
	 * @param formFieldId the internal id to search on
	 * @return the FormField with the given id
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public FormField getFormField(Integer formFieldId) throws APIException;

	/**
	 * Get FormField by its UUID
	 * <p>
	 * <strong>Should</strong> find object given valid uuid<br/>
	 * <strong>Should</strong> return null if no object found with given uuid
	 *
	 * @param uuid
	 * @return form field or null
	 */
	public FormField getFormFieldByUuid(String uuid) throws APIException;

	/**
	 * Finds the FormField defined for this form/concept combination while discounting any form field
	 * found in the <code>ignoreFormFields</code> collection This method was added when needing to
	 * relate observations to form fields during a display. The use case would be that you know a
	 * Concept for a obs, which was defined on a form (via a formField). You can relate the formFields
	 * to Concepts easily enough, but if a Form reuses a Concept in two separate FormFields you don't
	 * want to only associate that first formField with that concept. So, keep a running list of
	 * formFields you've seen and pass them back in here to rule them out.
	 * <p>
	 * <strong>Should</strong> get form fields by form and concept<br/>
	 * <strong>Should</strong> not fail with null ignoreFormFields argument<br/>
	 * <strong>Should</strong> simply return null for nonexistent concepts<br/>
	 * <strong>Should</strong> simply return null for nonexistent forms<br/>
	 * <strong>Should</strong> ignore formFields passed to ignoreFormFields
	 *
	 * @param form Form that this concept was found on
	 * @param concept Concept (question) on this form that is being requested
	 * @param ignoreFormFields FormFields to ignore (aka already seen formfields)
	 * @param force if true and there are zero matches because all formFields were ignored (because of
	 *            ignoreFormFields) than the first result is returned
	 * @return Formfield for this concept on this form
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_FORMS)
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force)
	        throws APIException;

	/**
	 * Creates or updates the given FormField
	 * <p>
	 * <strong>Should</strong> propagate save to the Field property on the given FormField<br/>
	 * <strong>Should</strong> save given formField successfully<br/>
	 * <strong>Should</strong> inject form fields from serializable complex obs handlers
	 *
	 * @param formField the FormField to save
	 * @return the formField that was just saved
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public FormField saveFormField(FormField formField) throws APIException;

	/**
	 * Completely removes the given FormField from the database. This is not reversible
	 * <p>
	 * <strong>Should</strong> delete the given form field successfully
	 *
	 * @param formField the FormField to purge
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public void purgeFormField(FormField formField) throws APIException;

	/**
	 * Retires field
	 * <p>
	 * <strong>Should</strong> set the retired bit before saving
	 *
	 * @param field the Field to retire
	 * @return the Field that was retired
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public Field retireField(Field field) throws APIException;

	/**
	 * Unretires field
	 * <p>
	 * <strong>Should</strong> unset the retired bit before saving
	 *
	 * @param field the Field to unretire
	 * @return the Field that was unretired
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FORMS)
	public Field unretireField(Field field) throws APIException;

	/**
	 * Saves the given field type to the database
	 * <p>
	 * <strong>Should</strong> create new field type<br/>
	 * <strong>Should</strong> update existing field type
	 *
	 * @param fieldType the field type to save
	 * @return the saved field type
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.MANAGE_FIELD_TYPES)
	public FieldType saveFieldType(FieldType fieldType) throws APIException;

	/**
	 * Deletes the given field type from the database. This should not be done. It is preferred to just
	 * retired this field type with #retireFieldType(FieldType)
	 * <p>
	 * <strong>Should</strong> delete the given field type successfully
	 *
	 * @param fieldType the field type to purge
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.PURGE_FIELD_TYPES)
	public void purgeFieldType(FieldType fieldType) throws APIException;

	/**
	 * Finds a FormResource by its id
	 * <p>
	 * <strong>Should</strong> find a saved FormResource<br/>
	 * <strong>Should</strong> return null if no FormResource found
	 *
	 * @param formResourceId the id of the resource
	 * @since 1.9
	 */
	public FormResource getFormResource(Integer formResourceId) throws APIException;

	/**
	 * Finds a FormResource by its uuid
	 *
	 * @param uuid the uuid of the resource
	 * @since 1.9
	 */
	public FormResource getFormResourceByUuid(String uuid) throws APIException;

	/**
	 * Finds a FormResource based on a given Form and name
	 *
	 * @param form the Form that the resource belongs to
	 * @param name the name of the resource
	 * @since 1.9
	 */
	public FormResource getFormResource(Form form, String name) throws APIException;

	/**
	 * Finds all FormResources tied to a given form
	 *
	 * @param form
	 * @return the resources attached to the form
	 * @throws APIException
	 * @since 1.9
	 */
	public Collection<FormResource> getFormResourcesForForm(Form form) throws APIException;

	/**
	 * Saves or updates the given form resource
	 * <p>
	 * <strong>Should</strong> persist a FormResource<br/>
	 * <strong>Should</strong> overwrite an existing resource with same name<br/>
	 * <strong>Should</strong> be able to save an XSLT
	 *
	 * @param formResource the resource to be saved
	 * @since 1.9
	 */
	public FormResource saveFormResource(FormResource formResource) throws APIException;

	/**
	 * Purges a form resource
	 * <p>
	 * <strong>Should</strong> delete a form resource
	 *
	 * @param formResource the resource to be purged
	 * @since 1.9
	 */
	public void purgeFormResource(FormResource formResource) throws APIException;

	/**
	 * Checks if the forms are locked, and if they are throws an exception when saving or deleting a
	 * form
	 *
	 * @throws FormsLockedException
	 */
	public void checkIfFormsAreLocked() throws FormsLockedException;
}
