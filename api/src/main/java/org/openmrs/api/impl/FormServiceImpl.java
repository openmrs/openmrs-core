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

import org.hibernate.exception.ConstraintViolationException;
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
import org.openmrs.api.InvalidFileTypeException;
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
	 * @see org.openmrs.api.FormService#getForm(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Form getForm(Integer formId) throws APIException {
		return dao.getForm(formId);
	}
	
	/**
	 * Duplicate this form and form_fields associated with this form
	 * 
	 * @param form
	 * @return New duplicated form
	 * @throws APIException
	 * @see org.openmrs.api.FormService#duplicateForm(org.openmrs.Form)
	 */
	@Override
	public Form duplicateForm(Form form) throws APIException {
		checkIfFormsAreLocked();
		// get original form id for reference later
		Integer originalFormId = form.getFormId();
		
		for (FormField formField : form.getFormFields()) {
			formField.setUuid(null);
			formField.setFormFieldId(null);
		}
		// this is required because Hibernate would recognize the original collection
		form.setFormFields(new HashSet<>(form.getFormFields()));
		
		form.setUuid(null);
		form.setFormId(null);
		form.setCreator(null);
		form.setDateCreated(null);
		form.setChangedBy(null);
		form.setDateChanged(null);
		
		Context.clearSession();
		
		Form originalForm = Context.getFormService().getForm(originalFormId);
		//On upgrading from hibernate 4.3.10.Final to 4.3.11.Final, 
		//calling getFormResourcesForForm results into a flush which finds the form as dirty,
		//resulting into the failure of this test
		//FormServiceTest.duplicateForm_shouldClearChangedDetailsAndUpdateCreationDetails:401 expected null, but was:<admin>
		//That is why we call getFormResourcesForForm before dao.duplicateForm(form) below.
		Collection<FormResource> formResources = Context.getFormService().getFormResourcesForForm(originalForm);
		
		RequiredDataAdvice.recursivelyHandle(SaveHandler.class, form, null);
		Form newForm = dao.duplicateForm(form);
		
		// duplicate form resources from the old form to the new one
		duplicateFormResources(originalForm, newForm, formResources);
		
		return newForm;
	}
	
	/**
	 * @see org.openmrs.api.FormService#retireForm(org.openmrs.Form, java.lang.String)
	 */
	@Override
	public void retireForm(Form form, String reason) throws APIException {
		form.setRetired(true);
		form.setRetireReason(reason);
		Context.getFormService().saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#unretireForm(org.openmrs.Form)
	 */
	@Override
	public void unretireForm(Form form) throws APIException {
		form.setRetired(false);
		Context.getFormService().saveForm(form);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFieldTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FieldType> getAllFieldTypes() throws APIException {
		return Context.getFormService().getAllFieldTypes(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFieldTypes(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FieldType> getAllFieldTypes(boolean includeRetired) throws APIException {
		return dao.getAllFieldTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public FieldType getFieldType(Integer fieldTypeId) throws APIException {
		return dao.getFieldType(fieldTypeId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getField(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Field getField(Integer fieldId) throws APIException {
		return dao.getField(fieldId);
	}
		
	/**
	 * @see org.openmrs.api.FormService#getFormField(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormField getFormField(Integer formFieldId) throws APIException {
		return dao.getFormField(formFieldId);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormField(org.openmrs.Form, org.openmrs.Concept,
	 *      java.util.Collection, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormField getFormField(Form form, Concept concept, Collection<FormField> ignoreFormFields, boolean force)
	        throws APIException {
		// create an empty ignoreFormFields list if none was passed in
		Collection<FormField> tmpIgnoreFormFields = ignoreFormFields;
		if (tmpIgnoreFormFields == null) {
			tmpIgnoreFormFields = Collections.emptyList();
		}
		
		return dao.getFormField(form, concept, tmpIgnoreFormFields, force);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Field getFieldByUuid(String uuid) throws APIException {
		return dao.getFieldByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public FieldAnswer getFieldAnswerByUuid(String uuid) throws APIException {
		return dao.getFieldAnswerByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public FieldType getFieldTypeByUuid(String uuid) throws APIException {
		return dao.getFieldTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldTypeByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public FieldType getFieldTypeByName(String name) throws APIException {
		return dao.getFieldTypeByName(name);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Form getFormByUuid(String uuid) throws APIException {
		return dao.getFormByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormFieldByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public FormField getFormFieldByUuid(String uuid) throws APIException {
		return dao.getFormFieldByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFields()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Field> getAllFields() throws APIException {
		return Context.getFormService().getAllFields(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFields(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Field> getAllFields(boolean includeRetired) throws APIException {
		return dao.getAllFields(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllFormFields()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<FormField> getAllFormFields() throws APIException {
		return dao.getAllFormFields();
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllForms()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Form> getAllForms() throws APIException {
		return Context.getFormService().getAllForms(true);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getAllForms(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Form> getAllForms(boolean includeRetired) throws APIException {
		return dao.getAllForms(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFields(java.util.Collection, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean,
	 *      java.util.Collection, java.util.Collection, java.lang.Boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Field> getFields(Collection<Form> forms, Collection<FieldType> fieldTypes, Collection<Concept> concepts,
	        Collection<String> tableNames, Collection<String> attributeNames, Boolean selectMultiple,
	        Collection<FieldAnswer> containsAllAnswers, Collection<FieldAnswer> containsAnyAnswer, Boolean retired)
	        throws APIException {

		Collection<Form> tmpForms = forms == null ? Collections.emptyList() : forms;
		Collection<Concept> tmpConcepts = concepts == null ? Collections.emptyList() : concepts;
		Collection<FieldType> tmpFieldTypes = fieldTypes == null ? Collections.emptyList() : fieldTypes;
		Collection<String> tmpTableNames = tableNames == null ? Collections.emptyList() : tableNames;
		Collection<String> tmpAttributeNames = attributeNames == null ? Collections.emptyList() : attributeNames;
		Collection<FieldAnswer> tmpContainsAllAnswers = containsAllAnswers == null ? Collections.emptyList() : containsAllAnswers;
		Collection<FieldAnswer> tmpContainsAnyAnswer = containsAnyAnswer == null ? Collections.emptyList() : containsAnyAnswer;
		
		return dao.getFields(tmpForms, tmpFieldTypes, tmpConcepts, tmpTableNames, tmpAttributeNames, selectMultiple,
				tmpContainsAllAnswers, tmpContainsAnyAnswer, retired);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.String)
	 * <strong>Should</strong> return the form with the highest version, if more than one form with the given name
	 *         exists
	 */
	@Override
	@Transactional(readOnly = true)
	public Form getForm(String name) throws APIException {
		List<Form> forms = dao.getFormsByName(name);
		if (forms == null || forms.isEmpty()) {
			return null;
		} else {
			return forms.get(0);
		}
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForm(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Form getForm(String name, String version) throws APIException {
		return dao.getForm(name, version);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getForms(java.lang.String, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Form> getForms(String fuzzyName, boolean onlyLatestVersion) {
		// get all forms including unpublished and including retired
		List<Form> forms = Context.getFormService().getForms(fuzzyName, null, null, null, null, null, null);
		
		Set<String> namesAlreadySeen = new HashSet<>();
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
	 * @see org.openmrs.api.FormService#getForms(java.lang.String, java.lang.Boolean,
	 *      java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection,
	 *      java.util.Collection)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Form> getForms(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) {

		Collection<EncounterType> tmpEncounterTypes = encounterTypes == null ? Collections.emptyList() : encounterTypes;
		Collection<FormField> tmpContainingAllFormFields = containingAllFormFields == null ? Collections.emptyList() : containingAllFormFields;
		Collection<FormField> tmpContainingAnyFormField = containingAnyFormField == null ? Collections.emptyList() : containingAnyFormField;
		Collection<Field> tmpFields = fields == null ? Collections.emptyList() : fields;
		
		return dao.getForms(partialName, published, tmpEncounterTypes, retired, tmpContainingAnyFormField,
		    tmpContainingAllFormFields, tmpFields);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormCount(java.lang.String, java.lang.Boolean,
	 *      java.util.Collection, java.lang.Boolean, java.util.Collection, java.util.Collection,
	 *      java.util.Collection)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getFormCount(String partialName, Boolean published, Collection<EncounterType> encounterTypes,
	        Boolean retired, Collection<FormField> containingAnyFormField, Collection<FormField> containingAllFormFields,
	        Collection<Field> fields) {

		Collection<EncounterType> tmpEncounterTypes = encounterTypes == null ? Collections.emptyList() : encounterTypes;
		Collection<FormField> tmpContainingAllFormFields = containingAllFormFields == null ? Collections.emptyList() : containingAllFormFields;
		Collection<FormField> tmpContainingAnyFormField = containingAnyFormField == null ? Collections.emptyList() : containingAnyFormField;
		Collection<Field> tmpFields = fields == null ? Collections.emptyList() : fields;
		
		return dao.getFormCount(partialName, published, tmpEncounterTypes, retired, tmpContainingAnyFormField,
		    tmpContainingAllFormFields, tmpFields);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getPublishedForms()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Form> getPublishedForms() throws APIException {
		return Context.getFormService().getForms(null, true, null, false, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeField(org.openmrs.Field)
	 */
	@Override
	public void purgeField(Field field) throws APIException {
		Context.getFormService().purgeField(field, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeField(org.openmrs.Field, boolean)
	 */
	@Override
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
	@Override
	public void purgeForm(Form form) throws APIException {
		checkIfFormsAreLocked();
		Context.getFormService().purgeForm(form, false);
	}
	
	/**
	 * @see org.openmrs.api.FormService#purgeForm(org.openmrs.Form, boolean)
	 */
	@Override
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
	@Override
	public void purgeFormField(FormField formField) throws APIException {
		dao.deleteFormField(formField);
	}
	
	/**
	 * @see org.openmrs.api.FormService#retireField(org.openmrs.Field)
	 */
	@Override
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
	@Override
	public Field saveField(Field field) throws APIException {
		return dao.saveField(field);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveForm(org.openmrs.Form)
	 */
	@Override
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
	@Override
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

		FormField tmpFormField = dao.saveFormField(formField);
		
		//Include all formfields from all serializable complex obs handlers
		Concept concept = tmpFormField.getField().getConcept();
		if (concept != null && concept.isComplex()) {
			ComplexObsHandler handler = Context.getObsService().getHandler(((ConceptComplex) concept).getHandler());
			if (handler instanceof SerializableComplexObsHandler) {
				SerializableComplexObsHandler sHandler = (SerializableComplexObsHandler) handler;
				if (sHandler.getFormFields() != null) {
					for (FormField ff : sHandler.getFormFields()) {
						ff.setParent(tmpFormField);
						ff.setForm(tmpFormField.getForm());
						ff.setCreator(tmpFormField.getCreator());
						ff.setDateCreated(tmpFormField.getDateCreated());
						dao.saveFormField(ff);
					}
				}
			}
		}
		
		return tmpFormField;
	}
	
	/**
	 * @see org.openmrs.api.FormService#unretireField(org.openmrs.Field)
	 */
	@Override
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
	@Override
	public List<Field> getFields(String fuzzySearchPhrase) throws APIException {
		return dao.getFields(fuzzySearchPhrase);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFieldsByConcept(org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Field> getFieldsByConcept(Concept concept) throws APIException {
		return Context.getFormService().getFields(null, null, Collections.singleton(concept), null, null, null, null, null,
		    null);
	}
	
	/**
	 * @see org.openmrs.api.FormService#getFormsContainingConcept(org.openmrs.Concept)
	 */
	@Override
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
	@Override
	public void purgeFieldType(FieldType fieldType) throws APIException {
		dao.deleteFieldType(fieldType);
	}
	
	/**
	 * @see org.openmrs.api.FormService#saveFieldType(org.openmrs.FieldType)
	 */
	@Override
	public FieldType saveFieldType(FieldType fieldType) throws APIException {
		return dao.saveFieldType(fieldType);
	}
	
	/**
	 * @see FormService#mergeDuplicateFields()
	 */
	@Override
	public int mergeDuplicateFields() throws APIException {
		
		List<Field> fields = dao.getAllFields(true);
		Set<Field> fieldsToDelete = new HashSet<>();
		
		Map<String, Integer> fieldNameAsKeyAndFieldIdAsValueMap = new HashMap<>();
		
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
		try {
		    CustomDatatypeUtil.saveIfDirty(toPersist);
		}
		catch (ConstraintViolationException ex) {
		    throw new InvalidFileTypeException(ex.getMessage(), ex);
		}
		
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
	 * @param formResources the form resources from the source form
	 */
	private void duplicateFormResources(Form source, Form destination, Collection<FormResource> formResources) {
		FormService service = Context.getFormService();
		for (FormResource resource : formResources) {
			FormResource newResource = new FormResource(resource);
			newResource.setForm(destination);
			service.saveFormResource(newResource);
		}
	}
	
	/*
	 * @see org.openmrs.api.FormService#checkIfFormsAreLocked()
	 * @see FormsLockedException
	 */
	@Override
	public void checkIfFormsAreLocked() {
		String locked = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FORMS_LOCKED,
		    "false");
		if (Boolean.valueOf(locked)) {
			throw new FormsLockedException();
		}
	}
	
}
