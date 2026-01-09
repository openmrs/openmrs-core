/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Concept
import org.openmrs.EncounterType
import org.openmrs.Field
import org.openmrs.FieldAnswer
import org.openmrs.FieldType
import org.openmrs.Form
import org.openmrs.FormField
import org.openmrs.FormResource
import org.openmrs.annotation.Authorized
import org.openmrs.util.PrivilegeConstants

/**
 * This service contains methods relating to Form, FormField, and Field.
 * Methods relating to FieldType are in AdministrationService.
 */
interface FormService : OpenmrsService {

    /**
     * Create or update the given Form in the database.
     *
     * @param form the Form to save
     * @return the Form that was saved
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun saveForm(form: Form): Form

    /**
     * Get form by internal form identifier.
     *
     * @param formId internal identifier
     * @return requested form
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getForm(formId: Int?): Form?

    /**
     * Get form by exact name match.
     *
     * @param name exact name of the form to fetch
     * @return requested form
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getForm(name: String): Form?

    /**
     * Get Form by its UUID.
     *
     * @param uuid the uuid
     * @return form or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFormByUuid(uuid: String): Form?

    /**
     * Get form by exact name and version match.
     *
     * @param name exact name of the form to fetch
     * @param version exact version of the form to fetch
     * @return requested form
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getForm(name: String, version: String?): Form?

    /**
     * Gets all Forms, including retired ones.
     *
     * @return all Forms, including retired ones
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getAllForms(): List<Form>

    /**
     * Gets all forms, possibly including retired ones.
     *
     * @param includeRetired whether or not to return retired forms
     * @return all forms, possibly including retired ones
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getAllForms(includeRetired: Boolean): List<Form>

    /**
     * Gets all forms with name similar to the given name.
     *
     * @param fuzzyName approximate name to match
     * @param onlyLatestVersion whether or not to return only the latest version of each form
     * @return forms with names similar to fuzzyName
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    fun getForms(fuzzyName: String, onlyLatestVersion: Boolean): List<Form>

    /**
     * Gets all forms that match all the (nullable) criteria.
     *
     * @param partialNameSearch partial search of name
     * @param published whether the form is published
     * @param encounterTypes whether the form has any of these encounter types
     * @param retired whether the form is retired
     * @param containingAnyFormField includes forms that contain any of the specified FormFields
     * @param containingAllFormFields includes forms that contain all of the specified FormFields
     * @param fields whether the form has any of these fields
     * @return All forms that match the criteria
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    fun getForms(
        partialNameSearch: String?,
        published: Boolean?,
        encounterTypes: @JvmSuppressWildcards Collection<EncounterType>?,
        retired: Boolean?,
        containingAnyFormField: @JvmSuppressWildcards Collection<FormField>?,
        containingAllFormFields: @JvmSuppressWildcards Collection<FormField>?,
        fields: @JvmSuppressWildcards Collection<Field>?
    ): List<Form>

    /**
     * Same as [getForms] except that it returns an integer that is the size of the list.
     *
     * @see getForms
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    fun getFormCount(
        partialNameSearch: String?,
        published: Boolean?,
        encounterTypes: @JvmSuppressWildcards Collection<EncounterType>?,
        retired: Boolean?,
        containingAnyFormField: @JvmSuppressWildcards Collection<FormField>?,
        containingAllFormFields: @JvmSuppressWildcards Collection<FormField>?,
        fields: @JvmSuppressWildcards Collection<Field>?
    ): Int?

    /**
     * Returns all published forms (not including retired ones).
     *
     * @return all published non-retired forms
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getPublishedForms(): List<Form>

    /**
     * Audit form, consolidate similar fields.
     *
     * @return the number of merged fields
     * @throws APIException if merging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun mergeDuplicateFields(): Int

    /**
     * Duplicate this form and form_fields associated with this form.
     *
     * @param form the form to duplicate
     * @return New duplicated form
     * @throws APIException if duplication fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun duplicateForm(form: Form): Form

    /**
     * Retires the Form, leaving it in the database, but removing it from data entry screens.
     *
     * @param form the Form to retire
     * @param reason the retiredReason to set
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun retireForm(form: Form, reason: String)

    /**
     * Unretires a Form that had previously been retired.
     *
     * @param form the Form to revive
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun unretireForm(form: Form)

    /**
     * Completely remove a Form from the database.
     *
     * @param form the Form to purge
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun purgeForm(form: Form)

    /**
     * Completely remove a Form from the database.
     *
     * @param form the Form to purge
     * @param cascade whether or not to cascade delete all dependent objects
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun purgeForm(form: Form, cascade: Boolean)

    /**
     * Get all field types in the database including the retired ones.
     *
     * @return list of all field types
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FIELD_TYPES)
    @Throws(APIException::class)
    fun getAllFieldTypes(): List<FieldType>

    /**
     * Get all field types in the database with or without retired ones.
     *
     * @param includeRetired true/false whether to include the retired field types
     * @return list of all field types
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FIELD_TYPES)
    @Throws(APIException::class)
    fun getAllFieldTypes(includeRetired: Boolean): List<FieldType>

    /**
     * Get fieldType by internal identifier.
     *
     * @param fieldTypeId Integer id of FieldType to get
     * @return fieldType with given internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FIELD_TYPES)
    @Throws(APIException::class)
    fun getFieldType(fieldTypeId: Int?): FieldType?

    /**
     * Get FieldType by its UUID.
     *
     * @param uuid the uuid
     * @return field type or null
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getFieldTypeByUuid(uuid: String): FieldType?

    /**
     * Get FieldType by its name.
     *
     * @param name the name
     * @return field type or null
     * @throws APIException if retrieval fails
     * @since 1.11
     */
    @Throws(APIException::class)
    fun getFieldTypeByName(name: String): FieldType?

    /**
     * Returns all forms that contain the given concept as a field in their schema.
     *
     * @param concept the concept to search for in forms
     * @return forms containing the specified concept in their schema
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFormsContainingConcept(concept: Concept): List<Form>

    /**
     * Returns all FormFields in the database.
     *
     * @return all FormFields in the database
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getAllFormFields(): List<FormField>

    /**
     * Find all Fields whose names are similar to or contain the given phrase.
     *
     * @param fuzzySearchPhrase the search phrase
     * @return Fields with names similar to or containing the given phrase
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFields(fuzzySearchPhrase: String): List<Field>

    /**
     * Finds all Fields that point to the given concept, including retired ones.
     *
     * @param concept the concept to search for in the Field table
     * @return fields that point to the given concept
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFieldsByConcept(concept: Concept): List<Field>

    /**
     * Fetches all Fields in the database, including retired ones.
     *
     * @return all Fields
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getAllFields(): List<Field>

    /**
     * Fetches all Fields in the database, possibly including retired ones.
     *
     * @param includeRetired whether or not to include retired Fields
     * @return all Fields
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getAllFields(includeRetired: Boolean): List<Field>

    /**
     * Returns all Fields that match these (nullable) criteria.
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
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFields(
        forms: @JvmSuppressWildcards Collection<Form>?,
        fieldTypes: @JvmSuppressWildcards Collection<FieldType>?,
        concepts: @JvmSuppressWildcards Collection<Concept>?,
        tableNames: @JvmSuppressWildcards Collection<String>?,
        attributeNames: @JvmSuppressWildcards Collection<String>?,
        selectMultiple: Boolean?,
        containsAllAnswers: @JvmSuppressWildcards Collection<FieldAnswer>?,
        containsAnyAnswer: @JvmSuppressWildcards Collection<FieldAnswer>?,
        retired: Boolean?
    ): List<Field>

    /**
     * Gets a Field by internal database id.
     *
     * @param fieldId the id of the Field to fetch
     * @return the Field with the given id
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getField(fieldId: Int?): Field?

    /**
     * Get Field by its UUID.
     *
     * @param uuid the uuid
     * @return field or null
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getFieldByUuid(uuid: String): Field?

    /**
     * Get FieldAnswer by its UUID.
     *
     * @param uuid the uuid
     * @return field answer or null
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getFieldAnswerByUuid(uuid: String): FieldAnswer?

    /**
     * Creates or updates the given Field.
     *
     * @param field the Field to save
     * @return the Field that was saved
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun saveField(field: Field): Field

    /**
     * Completely removes a Field from the database.
     *
     * @param field the Field to purge
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun purgeField(field: Field)

    /**
     * Completely removes a Field from the database.
     *
     * @param field the Field to purge
     * @param cascade whether to cascade delete all FormFields pointing to this field
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun purgeField(field: Field, cascade: Boolean)

    /**
     * Gets a FormField by internal database id.
     *
     * @param formFieldId the internal id to search on
     * @return the FormField with the given id
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFormField(formFieldId: Int?): FormField?

    /**
     * Get FormField by its UUID.
     *
     * @param uuid the uuid
     * @return form field or null
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getFormFieldByUuid(uuid: String): FormField?

    /**
     * Finds the FormField defined for this form/concept combination.
     *
     * @param form Form that this concept was found on
     * @param concept Concept (question) on this form that is being requested
     * @param ignoreFormFields FormFields to ignore (aka already seen formfields)
     * @param force if true and there are zero matches because all formFields were ignored
     * @return Formfield for this concept on this form
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_FORMS)
    @Throws(APIException::class)
    fun getFormField(
        form: Form,
        concept: Concept,
        ignoreFormFields: @JvmSuppressWildcards Collection<FormField>?,
        force: Boolean
    ): FormField?

    /**
     * Creates or updates the given FormField.
     *
     * @param formField the FormField to save
     * @return the formField that was just saved
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun saveFormField(formField: FormField): FormField

    /**
     * Completely removes the given FormField from the database.
     *
     * @param formField the FormField to purge
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun purgeFormField(formField: FormField)

    /**
     * Retires field.
     *
     * @param field the Field to retire
     * @return the Field that was retired
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun retireField(field: Field): Field

    /**
     * Unretires field.
     *
     * @param field the Field to unretire
     * @return the Field that was unretired
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FORMS)
    @Throws(APIException::class)
    fun unretireField(field: Field): Field

    /**
     * Saves the given field type to the database.
     *
     * @param fieldType the field type to save
     * @return the saved field type
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_FIELD_TYPES)
    @Throws(APIException::class)
    fun saveFieldType(fieldType: FieldType): FieldType

    /**
     * Deletes the given field type from the database.
     *
     * @param fieldType the field type to purge
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_FIELD_TYPES)
    @Throws(APIException::class)
    fun purgeFieldType(fieldType: FieldType)

    /**
     * Finds a FormResource by its id.
     *
     * @param formResourceId the id of the resource
     * @return the FormResource or null
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun getFormResource(formResourceId: Int?): FormResource?

    /**
     * Finds a FormResource by its uuid.
     *
     * @param uuid the uuid of the resource
     * @return the FormResource or null
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun getFormResourceByUuid(uuid: String): FormResource?

    /**
     * Finds a FormResource based on a given Form and name.
     *
     * @param form the Form that the resource belongs to
     * @param name the name of the resource
     * @return the FormResource or null
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun getFormResource(form: Form, name: String): FormResource?

    /**
     * Finds all FormResources tied to a given form.
     *
     * @param form the form
     * @return the resources attached to the form
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun getFormResourcesForForm(form: Form): Collection<FormResource>

    /**
     * Saves or updates the given form resource.
     *
     * @param formResource the resource to be saved
     * @return the saved FormResource
     * @throws APIException if saving fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun saveFormResource(formResource: FormResource): FormResource

    /**
     * Purges a form resource.
     *
     * @param formResource the resource to be purged
     * @throws APIException if purging fails
     * @since 1.9
     */
    @Throws(APIException::class)
    fun purgeFormResource(formResource: FormResource)

    /**
     * Checks if the forms are locked, and if they are throws an exception.
     *
     * @throws FormsLockedException if forms are locked
     */
    @Throws(FormsLockedException::class)
    fun checkIfFormsAreLocked()
}
