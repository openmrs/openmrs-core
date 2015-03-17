/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.FormsLockedException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.FormDAO;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.SerializableComplexObsHandler;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.validator.FormValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

/**
 * Default implementation of the {@link FormService}
 * <p>
 * This class should not be instantiated alone, get a service class from the Context:
 * Context.getFormService();
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.FormService
 */
@Transactional
public class FormServiceImpl extends BaseOpenmrsService implements FormService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private FormDAO dao;
	
	private final FormValidator formValidator;
	
	/**
	 * Default empty constructor
	 */
	public FormServiceImpl() {
		formValidator = new FormValidator();
	}
	
	/**
	 * Method used to inject the data access object.
	 * 
	 * @param dao
	 */
	public void setFormDAO(FormDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.FormService#createForm(org.openmrs.Form)
	 * @deprecated
	 */
	@Deprecated
	public Form createForm(Form form) throws APIException {
		return Context.getFormService().saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Form getForm(Integer formId) throws APIException {
		return dao.getForm(formId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(boolean, boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Form> getForms(boolean publishedOnly) throws APIException {
		if (publishedOnly) {
			return Context.getFormService().getPublishedForms();
		} else {
			return Context.getFormService().getAllForms();
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(boolean, boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Form> getForms(boolean publishedOnly, boolean includeRetired) throws APIException {
		if (publishedOnly && includeRetired) {
			log.warn("Should probably not be searching for published forms, but including retired ones");
			List<Form> ret = new ArrayList<Form>();
			ret.addAll(Context.getFormService().getPublishedForms());
			ret.addAll(Context.getFormService().getForms(null, true, null, true, null, null, null));
			return ret;
		} else {
			if (publishedOnly) {
				return Context.getFormService().getPublishedForms();
			} else {
				return Context.getFormService().getAllForms(includeRetired);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#updateForm(org.openmrs.Form)
	 * @deprecated
	 */
	@Deprecated
	public void updateForm(Form form) throws APIException {
		Context.getFormService().saveForm(form);
	}
	
	/**
	 * Duplicate this form and form_fields associated with this form
	 * 
	 * @param form
	 * @return New duplicated form
	 * @throws APIException
	 * @see org.openmrs.api.FormService#duplicateForm(org.openmrs.Form)
	 */
	public Form duplicateForm(Form form) throws APIException {
		// Map of /Old FormFieldId/ to /New FormField Object/
		//TreeMap<Integer, FormField> formFieldMap = new TreeMap<Integer, FormField>();
		//formFieldMap.put(null, null); //for parentless formFields
		checkIfFormsAreLocked();
		// get original form id for reference later
		Integer originalFormId = form.getFormId();
		
		for (FormField formField : form.getFormFields()) {
			//formFieldMap.put(formField.getFormFieldId(), formField);
			formField.setUuid(null);
			formField.setFormFieldId(null);
			//formField.setParent(formFieldMap.get(formField.getParent().getFormFieldId()));
		}
		// this is required because Hibernate would recognize the original collection
		form.setFormFields(new HashSet<FormField>(form.getFormFields()));
		
		form.setUuid(null);
		form.setFormId(null);
		form.setCreator(null);
		form.setDateCreated(null);
		form.setChangedBy(null);
		form.setDateChanged(null);
		
		Context.clearSession();
		
		RequiredDataAdvice.recursivelyHandle(SaveHandler.class, form, null);
		Form newForm = dao.duplicateForm(form);
		
		// duplicate form resources from the old form to the new one
		duplicateFormResources(Context.getFormService().getForm(originalFormId), newForm);
		
		return newForm;
	}
	
	/**
	 * @see org.openmrs.api.FormService#retireForm(org.openmrs.Form, java.lang.String)
	 */
	public void retireForm(Form form, String reason) throws APIException {
		form.setRetired(true);
		form.setRetireReason(reason);
		Context.getFormService().saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#unretireForm(org.openmrs.Form)
	 */
	public void unretireForm(Form form) throws APIException {
		form.setRetired(false);
		Context.getFormService().saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#deleteForm(org.openmrs.Form)
	 * @deprecated
	 */
	@Deprecated
	public void deleteForm(Form form) throws APIException {
		Context.getFormService().purgeForm(form, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldTypes()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<FieldType> getFieldTypes() throws APIException {
		return Context.getFormService().getAllFieldTypes();
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFieldTypes()
	 */
	@Transactional(readOnly = true)
	public List<FieldType> getAllFieldTypes() throws APIException {
		return Context.getFormService().getAllFieldTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFieldTypes(boolean)
	 */
	@Transactional(readOnly = true)
	public List<FieldType> getAllFieldTypes(boolean includeRetired) throws APIException {
		return dao.getAllFieldTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldType(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public FieldType getFieldType(Integer fieldTypeId) throws APIException {
		return dao.getFieldType(fieldTypeId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Form> getForms() throws APIException {
		return Context.getFormService().getAllForms();
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(org.openmrs.Concept)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Set<Form> getForms(Concept c) throws APIException {
		return new HashSet<Form>(Context.getFormService().getFormsContainingConcept(c));
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormFields(org.openmrs.Form)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<FormField> getFormFields(Form form) throws APIException {
		List<FormField> formFields = new Vector<FormField>();
		
		if (form != null && form.getFormFields() != null) {
			formFields.addAll(form.getFormFields());
		}
		
		return formFields;
	}
	
	/**
	 * @see org.openmrs.api.FormService#findFields(java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Field> findFields(String searchPhrase) throws APIException {
		return Context.getFormService().getFields(searchPhrase);
	}
	
	/**
	 * @see org.openmrs.api.FormService#findFields(org.openmrs.Concept)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Field> findFields(Concept concept) throws APIException {
		return Context.getFormService().getFieldsByConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFields()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Field> getFields() throws APIException {
		return Context.getFormService().getAllFields();
	}
	
	/**
	 * @see org.openmrs.api.FormService#getField(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Field getField(Integer fieldId) throws APIException {
		return dao.getField(fieldId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#createField(org.openmrs.Field)
	 * @deprecated
	 */
	@Deprecated
	public void createField(Field field) throws APIException {
		Context.getFormService().saveField(field);
	}
	
	/**
	 * @see org.openmrs.api.FormService#updateField(org.openmrs.Field)
	 * @deprecated
	 */
	@Deprecated
	public void updateField(Field field) throws APIException {
		Context.getFormService().saveField(field);
	}
	
	/**
	 * @see org.openmrs.api.FormService#deleteField(org.openmrs.Field)
	 * @deprecated
	 */
	@Deprecated
	public void deleteField(Field field) throws APIException {
		Context.getFormService().purgeField(field);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public FormField getFormField(Integer formFieldId) throws APIException {
		return dao.getFormField(formFieldId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept)
	 * @see #getFormField(Form, Concept, Collection, boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public FormField getFormField(Form form, Concept concept) throws APIException {
		return Context.getFormService().getFormField(form, concept, null, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept,
	 *      java.util.Collection, boolean)
	 */
	@Transactional(readOnly = true)
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force)
	        throws APIException {
		// create an empty ignoreFormFields list if none was passed in
		if (ignoreFormFields == null) {
			ignoreFormFields = Collections.emptyList();
		}
		
		return dao.getFormField(form, concept, ignoreFormFields, force);
	}
	
	/**
	 * @see org.openmrs.api.FormService#createFormField(org.openmrs.FormField)
	 * @deprecated
	 */
	@Deprecated
	public void createFormField(FormField formField) throws APIException {
		Context.getFormService().saveFormField(formField);
	}
	
	/**
	 * @see org.openmrs.api.FormService#updateFormField(org.openmrs.FormField)
	 * @deprecated
	 */
	@Deprecated
	public void updateFormField(FormField formField) throws APIException {
		Context.getFormService().saveFormField(formField);
	}
	
	/**
	 * @see org.openmrs.api.FormService#deleteFormField(org.openmrs.FormField)
	 * @deprecated
	 */
	@Deprecated
	public void deleteFormField(FormField formField) throws APIException {
		Context.getFormService().purgeFormField(formField);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Field getFieldByUuid(String uuid) throws APIException {
		return dao.getFieldByUuid(uuid);
	}
	
	@Transactional(readOnly = true)
	public FieldAnswer getFieldAnswerByUuid(String uuid) throws APIException {
		return dao.getFieldAnswerByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldTypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public FieldType getFieldTypeByUuid(String uuid) throws APIException {
		return dao.getFieldTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldTypeByName(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public FieldType getFieldTypeByName(String name) throws APIException {
		return dao.getFieldTypeByName(name);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Form getFormByUuid(String uuid) throws APIException {
		return dao.getFormByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormFieldByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public FormField getFormFieldByUuid(String uuid) throws APIException {
		return dao.getFormFieldByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#findForms(java.lang.String, boolean, boolean)
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<Form> findForms(String text, boolean includeUnpublished, boolean includeRetired) {
		if (includeUnpublished) {
			return Context.getFormService().getForms(text, null, null, includeRetired, null, null, null);
		} else {
			return Context.getFormService().getForms(text, true, null, includeRetired, null, null, null);
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFields()
	 */
	@Transactional(readOnly = true)
	public List<Field> getAllFields() throws APIException {
		return Context.getFormService().getAllFields(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFields(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Field> getAllFields(boolean includeRetired) throws APIException {
		return dao.getAllFields(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFormFields()
	 */
	@Transactional(readOnly = true)
	public List<FormField> getAllFormFields() throws APIException {
		return dao.getAllFormFields();
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllForms()
	 */
	@Transactional(readOnly = true)
	public List<Form> getAllForms() throws APIException {
		return Context.getFormService().getAllForms(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllForms(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Form> getAllForms(boolean includeRetired) throws APIException {
		return dao.getAllForms(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFields(java.util.Collection, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean,
	 *      java.util.Collection, java.util.Collection, java.lang.Boolean)
	 */
	@Transactional(readOnly = true)
	public List<Field> getFields(Collection<Form> forms, Collection<FieldType> fieldTypes, Collection<Concept> concepts,
	        Collection<String> tableNames, Collection<String> attributeNames, Boolean selectMultiple,
	        Collection<FieldAnswer> containsAllAnswers, Collection<FieldAnswer> containsAnyAnswer, Boolean retired)
	        throws APIException {
		
		if (forms == null) {
			forms = Collections.emptyList();
		}
		
		if (fieldTypes == null) {
			fieldTypes = Collections.emptyList();
		}
		
		if (concepts == null) {
			concepts = Collections.emptyList();
		}
		
		if (tableNames == null) {
			tableNames = Collections.emptyList();
		}
		
		if (attributeNames == null) {
			attributeNames = Collections.emptyList();
		}
		
		if (containsAllAnswers == null) {
			containsAllAnswers = Collections.emptyList();
		}
		
		if (containsAnyAnswer == null) {
			containsAnyAnswer = Collections.emptyList();
		}
		
		return dao.getFields(forms, fieldTypes, concepts, tableNames, attributeNames, selectMultiple, containsAllAnswers,
		    containsAnyAnswer, retired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.String)
	 * @should return the form with the highest version, if more than one form with the given name
	 *         exists
	 */
	@Transactional(readOnly = true)
	public Form getForm(String name) throws APIException {
		List<Form> forms = dao.getFormsByName(name);
		if (forms == null || forms.size() == 0) {
			return null;
		} else {
			return forms.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.String, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Form getForm(String name, String version) throws APIException {
		return dao.getForm(name, version);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(java.lang.String, boolean)
	 */
	@Transactional(readOnly = true)
	public List<Form> getForms(String fuzzyName, boolean onlyLatestVersion) {
		// get all forms including unpublished and including retired
		List<Form> forms = Context.getFormService().getForms(fuzzyName, null, null, null, null, null, null);
		
		Set<String> namesAlreadySeen = new HashSet<String>();
		for (Iterator<Form> i = forms.iterator(); i.hasNext();) {
			Form form = i.next();
			if (namesAlreadySeen.contains(form.getName())) {
				i.remove();
			} else {
				namesAlreadySeen.add(form.getName());
			}
		}
		return forms;
	}
	
	/**
	 * @deprecated see
	 *             {@link #getForms(String, Boolean, Collection, Boolean, Collection, Collection, Collection)}
	 */
	@Transactional(readOnly = true)
	@Deprecated
	public List<Form> getForms(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields) {
		
		return Context.getFormService().getForms(partialName, published, encounterTypes, retired, containingAnyFormField,
		    containingAllFormFields, null);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(java.lang.String, java.lang.Boolean,
	 *      java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection,
	 *      java.util.Collection)
	 */
	@Transactional(readOnly = true)
	public List<Form> getForms(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) {
		
		if (encounterTypes == null) {
			encounterTypes = Collections.emptyList();
		}
		
		if (containingAllFormFields == null) {
			containingAllFormFields = Collections.emptyList();
		}
		
		if (containingAnyFormField == null) {
			containingAnyFormField = Collections.emptyList();
		}
		
		if (fields == null) {
			fields = Collections.emptyList();
		}
		
		return dao.getForms(partialName, published, encounterTypes, retired, containingAnyFormField,
		    containingAllFormFields, fields);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormCount(java.lang.String, java.lang.Boolean,
	 *      java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection,
	 *      java.util.Collection)
	 */
	@Transactional(readOnly = true)
	public Integer getFormCount(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) {
		
		if (encounterTypes == null) {
			encounterTypes = Collections.emptyList();
		}
		
		if (containingAllFormFields == null) {
			containingAllFormFields = Collections.emptyList();
		}
		
		if (containingAnyFormField == null) {
			containingAnyFormField = Collections.emptyList();
		}
		
		if (fields == null) {
			fields = Collections.emptyList();
		}
		
		return dao.getFormCount(partialName, published, encounterTypes, retired, containingAnyFormField,
		    containingAllFormFields, fields);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getPublishedForms()
	 */
	@Transactional(readOnly = true)
	public List<Form> getPublishedForms() throws APIException {
		return Context.getFormService().getForms(null, true, null, false, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeField(org.openmrs.Field)
	 */
	public void purgeField(Field field) throws APIException {
		Context.getFormService().purgeField(field, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeField(org.openmrs.Field, boolean)
	 */
	public void purgeField(Field field, boolean cascade) throws APIException {
		if (cascade) {
			throw new APIException("general.not.yet.implemented", (Object[]) null);
		} else {
			dao.deleteField(field);
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeForm(org.openmrs.Form)
	 */
	public void purgeForm(Form form) throws APIException {
		checkIfFormsAreLocked();
		Context.getFormService().purgeForm(form, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeForm(org.openmrs.Form, boolean)
	 */
	public void purgeForm(Form form, boolean cascade) throws APIException {
		if (cascade) {
			throw new APIException("general.not.yet.implemented", (Object[]) null);
		}
		
		// remove resources
		for (FormResource resource : Context.getFormService().getFormResourcesForForm(form)) {
			Context.getFormService().purgeFormResource(resource);
		}
		
		dao.deleteForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeFormField(org.openmrs.FormField)
	 */
	public void purgeFormField(FormField formField) throws APIException {
		dao.deleteFormField(formField);
	}
	
	/**
	 * @see org.openmrs.api.FormService#retireField(org.openmrs.Field)
	 */
	public Field retireField(Field field) throws APIException {
		if (!field.getRetired()) {
			field.setRetired(true);
			return Context.getFormService().saveField(field);
		} else {
			return field;
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveField(org.openmrs.Field)
	 */
	public Field saveField(Field field) throws APIException {
		return dao.saveField(field);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveForm(org.openmrs.Form)
	 */
	public Form saveForm(Form form) throws APIException {
		checkIfFormsAreLocked();
		BindException errors = new BindException(form, "form");
		formValidator.validate(form, errors);
		if (errors.hasErrors()) {
			throw new APIException(errors);
		}
		
		if (form.getFormFields() != null) {
			for (FormField ff : form.getFormFields()) {
				if (ff.getForm() == null) {
					ff.setForm(form);
				} else if (!ff.getForm().equals(form)) {
					throw new APIException("Form.contains.FormField.error", new Object[] { ff });
				}
			}
		}
		
		return dao.saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveFormField(org.openmrs.FormField)
	 */
	public FormField saveFormField(FormField formField) throws APIException {
		Field field = formField.getField();
		if (field.getCreator() == null) {
			field.setCreator(Context.getAuthenticatedUser());
		}
		if (field.getDateCreated() == null) {
			field.setDateCreated(new Date());
		}
		
		// don't change the changed by and date changed on field for
		// form field updates
		
		// set the uuid here because the RequiredDataAdvice only looks at child lists
		if (field.getUuid() == null) {
			field.setUuid(UUID.randomUUID().toString());
		}
		
		formField = dao.saveFormField(formField);
		
		//Include all formfields from all serializable complex obs handlers
		Concept concept = formField.getField().getConcept();
		if (concept != null && concept.isComplex()) {
			ComplexObsHandler handler = Context.getObsService().getHandler(((ConceptComplex) concept).getHandler());
			if (handler instanceof SerializableComplexObsHandler) {
				SerializableComplexObsHandler sHandler = (SerializableComplexObsHandler) handler;
				if (sHandler.getFormFields() != null) {
					for (FormField ff : sHandler.getFormFields()) {
						ff.setParent(formField);
						ff.setForm(formField.getForm());
						ff.setCreator(formField.getCreator());
						ff.setDateCreated(formField.getDateCreated());
						dao.saveFormField(ff);
					}
				}
			}
		}
		
		return formField;
	}
	
	/**
	 * @see org.openmrs.api.FormService#unretireField(org.openmrs.Field)
	 */
	public Field unretireField(Field field) throws APIException {
		if (field.getRetired()) {
			field.setRetired(false);
			return Context.getFormService().saveField(field);
		} else {
			return field;
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFields(java.lang.String)
	 */
	public List<Field> getFields(String fuzzySearchPhrase) throws APIException {
		return dao.getFields(fuzzySearchPhrase);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldsByConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Field> getFieldsByConcept(Concept concept) throws APIException {
		return Context.getFormService().getFields(null, null, Collections.singleton(concept), null, null, null, null, null,
		    null);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormsContainingConcept(org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<Form> getFormsContainingConcept(Concept concept) throws APIException {
		if (concept.getConceptId() == null) {
			return Collections.emptyList();
		}
		
		return dao.getFormsContainingConcept(concept);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeFieldType(org.openmrs.FieldType)
	 */
	public void purgeFieldType(FieldType fieldType) throws APIException {
		dao.deleteFieldType(fieldType);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveFieldType(org.openmrs.FieldType)
	 */
	public FieldType saveFieldType(FieldType fieldType) throws APIException {
		return dao.saveFieldType(fieldType);
	}
	
	/**
	 * @see org.openmrs.api.FormService#auditForm(org.openmrs.Form)
	 */
	@Override
	public int mergeDuplicateFields() throws APIException {
		
		List<Field> fields = dao.getAllFields(true);
		Set<Field> fieldsToDelete = new HashSet<Field>();
		
		Map<String, Integer> fieldNameAsKeyAndFieldIdAsValueMap = new HashMap<String, Integer>();
		
		for (Field field : fields) {
			if (fieldNameAsKeyAndFieldIdAsValueMap.containsKey(field.getName())) {
				Field fieldToCompareTo = dao.getField(fieldNameAsKeyAndFieldIdAsValueMap.get(field.getName()));
				if (fieldsAreSimilar(field, fieldToCompareTo)) {
					
					//get the formFields that use this duplicate field
					List<FormField> formFields = dao.getFormFieldsByField(field);
					
					//for each of the formFields that use this duplicate field
					//replace with field from outer loop
					for (FormField formField : formFields) {
						formField.setField(fieldToCompareTo);
						dao.saveFormField(formField);
						
						fieldsToDelete.add(field);
					}
				} else {
					fieldNameAsKeyAndFieldIdAsValueMap.put(field.getName(), field.getId());
				}
				
			} else {
				fieldNameAsKeyAndFieldIdAsValueMap.put(field.getName(), field.getId());
			}
			
		}
		
		for (Field field : fieldsToDelete) {
			dao.deleteField(field);
		}
		
		return fieldsToDelete.size();
	}
	
	private boolean fieldsAreSimilar(Field field, Field fieldToBeReplaced) {
		
		return (OpenmrsUtil.nullSafeEquals(field.getName(), fieldToBeReplaced.getName())
		        && OpenmrsUtil.nullSafeEquals(field.getSelectMultiple(), fieldToBeReplaced.getSelectMultiple())
		        && OpenmrsUtil.nullSafeEquals(field.getFieldType(), fieldToBeReplaced.getFieldType())
		        && OpenmrsUtil.nullSafeEquals(field.getConcept(), fieldToBeReplaced.getConcept())
		        && OpenmrsUtil.nullSafeEquals(field.getTableName(), fieldToBeReplaced.getTableName())
		        && OpenmrsUtil.nullSafeEquals(field.getDefaultValue(), fieldToBeReplaced.getDefaultValue())
		        && field.getRetired() != null && !field.getRetired());
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormResource(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormResource getFormResource(Integer formResourceId) throws APIException {
		return dao.getFormResource(formResourceId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormResourceByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormResource getFormResourceByUuid(String uuid) throws APIException {
		return dao.getFormResourceByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormResource(org.openmrs.Form, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormResource getFormResource(Form form, String name) throws APIException {
		return dao.getFormResource(form, name);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveFormResource(org.openmrs.FormResource)
	 */
	@Override
	public FormResource saveFormResource(FormResource formResource) throws APIException {
		if (formResource == null) {
			return null;
		}
		
		// If a form resource with same name exists, replace it with current value
		FormResource toPersist = formResource;
		FormResource original = Context.getFormService().getFormResource(formResource.getForm(), formResource.getName());
		if (original != null) {
			original.setName(formResource.getName());
			original.setValue(formResource.getValue());
			original.setDatatypeClassname(formResource.getDatatypeClassname());
			original.setDatatypeConfig(formResource.getDatatypeConfig());
			original.setPreferredHandlerClassname(formResource.getPreferredHandlerClassname());
			toPersist = original;
		}
		CustomDatatypeUtil.saveIfDirty(toPersist);
		return dao.saveFormResource(toPersist);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeFormResource(org.openmrs.FormResource)
	 */
	@Override
	public void purgeFormResource(FormResource formResource) throws APIException {
		dao.deleteFormResource(formResource);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormResourcesForForm(org.openmrs.Form)
	 */
	@Override
	@Transactional(readOnly = true)
	public Collection<FormResource> getFormResourcesForForm(Form form) throws APIException {
		return dao.getFormResourcesForForm(form);
	}
	
	/**
	 * duplicates form resources from one form to another
	 * 
	 * @param source the form to copy resources from
	 * @param destination the form to copy resources to
	 */
	private void duplicateFormResources(Form source, Form destination) {
		FormService service = Context.getFormService();
		for (FormResource resource : service.getFormResourcesForForm(source)) {
			FormResource newResource = new FormResource(resource);
			newResource.setForm(destination);
			service.saveFormResource(newResource);
		}
	}
	
	/*
	 * @see org.openmrs.api.FormService#checkIfFormsAreLocked()
	 * @see FormsLockedException
	 */
	public void checkIfFormsAreLocked() {
		String locked = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FORMS_LOCKED,
		    "false");
		if (locked.toLowerCase().equals("true")) {
			throw new FormsLockedException();
		}
	}
	
}
