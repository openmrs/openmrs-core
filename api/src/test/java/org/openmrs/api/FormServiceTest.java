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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.FormResource;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.SerializableComplexObsHandler;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;

/**
 * TODO clean up and finish this test for all methods in FormService
 * 
 * @see FormService
 */
public class FormServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-initialFieldTypes.xml";
	
	protected static final String FORM_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-formFields.xml";
	
	protected static final String MULTIPLE_FORMS_FORM_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-multipleForms-formFields.xml";
	
	protected static final String FORM_SAMPLE_RESOURCE = "org/openmrs/api/include/FormServiceTest-sampleResource.xslt";
	
	/**
	 * Creates then updates a form FIXME Break this test case into separate tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFormCreateUpdateDelete() {
		FormService formService = Context.getFormService();
		
		//testing Form creation
		
		Form form1 = new Form();
		
		String name1 = "form name1";
		String version1 = "1.0";
		String descript1 = "descript1";
		
		form1.setName(name1);
		form1.setVersion(version1);
		form1.setDescription(descript1);
		
		formService.saveForm(form1);
		
		//testing get form
		
		Form form2 = formService.getForm(form1.getFormId());
		
		String name2 = "form name2";
		String version2 = "2.0";
		String descript2 = "descript2";
		
		form2.setName(name2);
		form2.setVersion(version2);
		form2.setDescription(descript2);
		
		formService.saveForm(form2);
		
		//testing correct updation
		
		Form form3 = formService.getForm(form2.getFormId());
		
		assertTrue(form1.equals(form3));
		
		assertTrue(form3.getName().equals(name2));
		assertTrue(form3.getVersion().equals(version2));
		assertTrue(form3.getDescription().equals(descript2));
		
		//testing (un)retiration
		
		formService.retireForm(form2, "reason");
		assertTrue(form2.getRetired());
		assertTrue(form2.getRetireReason().equals("reason"));
		
		formService.unretireForm(form2);
		assertFalse(form2.getRetired());
		assertNull(form2.getRetireReason());
		
		//testing deletion
		
		formService.purgeForm(form2);
		//formService.deleteForm(form1); //deleting a deleted form
	}
	
	/**
	 * Test create then update a field
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFieldCreateModifyDelete() {
		
		executeDataSet(INITIAL_FIELDS_XML);
		
		FormService formService = Context.getFormService();
		ConceptService conceptService = Context.getConceptService();
		
		//testing creation
		
		Concept concept1 = conceptService.getConcept(1);
		String name1 = "name1";
		String descript1 = "descript1";
		FieldType fieldtype1 = formService.getAllFieldTypes().get(0);
		String table1 = "table1";
		String attr1 = "attr1";
		Boolean multi1 = true;
		
		Field field1 = new Field();
		
		field1.setConcept(concept1);
		field1.setName(name1);
		field1.setDescription(descript1);
		field1.setFieldType(fieldtype1);
		field1.setTableName(table1);
		field1.setAttributeName(attr1);
		field1.setSelectMultiple(multi1);
		
		formService.saveField(field1);
		
		//testing update
		
		Field field2 = formService.getField(field1.getFieldId());
		
		Concept concept2 = conceptService.getConcept(2);
		String name2 = "name2";
		String descript2 = "descript2";
		FieldType fieldtype2 = formService.getAllFieldTypes().get(1);
		String table2 = "table2";
		String attr2 = "attr2";
		Boolean multi2 = false;
		
		field2.setConcept(concept2);
		field2.setName(name2);
		field2.setDescription(descript2);
		field2.setFieldType(fieldtype2);
		field2.setTableName(table2);
		field2.setAttributeName(attr2);
		field2.setSelectMultiple(multi2);
		
		formService.saveField(field2);
		
		//testing differences
		
		Field field3 = formService.getField(field2.getFieldId());
		
		assertTrue(field3.equals(field1));
		
		assertTrue(field1.getConcept().equals(concept2));
		assertTrue(field1.getName().equals(name2));
		assertTrue(field1.getDescription().equals(descript2));
		assertTrue(field1.getFieldType().equals(fieldtype2));
		assertTrue(field1.getTableName().equals(table2));
		assertTrue(field1.getAttributeName().equals(attr2));
		assertTrue(field1.getSelectMultiple().equals(multi2));
		
		//testing deletion
		
		formService.saveField(field3);
		formService.purgeField(field3);
		
		assertNull(formService.getField(field3.getFieldId()));
	}
	
	/**
	 * @see FormService#getFormField(Form,Concept,Collection<QFormField;>,null)
	 */
	@Test
	public void getFormField_shouldIgnoreFormFieldsPassedToIgnoreFormFields() {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormField ff = Context.getFormService().getFormField(new Form(1), new Concept(1), null, false);
		assertNotNull(ff); // sanity check
		
		// test that the first formfield is ignored when a second fetch
		// is done on the same form and same concept
		List<FormField> ignoreFormFields = new ArrayList<>();
		ignoreFormFields.add(ff);
		FormField ff2 = Context.getFormService().getFormField(new Form(1), new Concept(1), ignoreFormFields, false);
		assertNotNull(ff2);
		assertNotSame(ff, ff2);
		
	}
	
	/**
	 * @see FormService#getFormField(Form,Concept,Collection<QFormField;>,null)
	 */
	@Test
	public void getFormField_shouldNotFailWithNullIgnoreFormFieldsArgument() {
		// test that a null ignoreFormFields doesn't error out
		FormField ff = Context.getFormService().getFormField(new Form(1), new Concept(3), null, false);
		assertNotNull(ff);
	}
	
	/**
	 * Make sure that multiple forms are returned if a field is on a form more than once
	 * 
	 * @see {@link FormService#getForms(String, Boolean, java.util.Collection, Boolean, java.util.Collection, java.util.Collection, java.util.Collection)

	 */
	@Test
	public void getForms_shouldReturnDuplicateFormWhenGivenFieldsIncludedInFormMultipleTimes() {
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(1));
		
		List<Form> forms = formService.getForms(null, null, null, null, null, null, fields);
		
		assertEquals(3, forms.size());
	}
	
	/**
	 * @
	 * @see FormService#getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)
	 */
	@Test
	public void getForms_shouldReturnFormsContainingAllFormFieldsInContainingAllFormFields() {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		Set<FormField> formFields = new HashSet<>();
		formFields.add(new FormField(3));
		formFields.add(new FormField(5));
		formFields.add(new FormField(7));
		
		List<Form> forms = formService.getForms(null, null, null, null, null, formFields, null);
		assertEquals(1, forms.size());
		
		formFields = new HashSet<>();
		formFields.add(new FormField(2));
		formFields.add(new FormField(4));
		formFields.add(new FormField(6));
		
		forms = formService.getForms(null, null, null, null, null, formFields, null);
		assertEquals(0, forms.size());
	}
	
	/**
	 * ensure that FormFields in containingAnyFormField parameter are considered when filtering the results
	 * 
	 * @see {@link FormService#getForms(String, Boolean, java.util.Collection, Boolean, java.util.Collection, java.util.Collection, java.util.Collection)

	 */
	@Test
	public void getForms_shouldReturnFormsThatHaveAnyMatchingFormFieldsInContainingAnyFormField() {
		
		Integer numberOfExpectedForms = 2;
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(MULTIPLE_FORMS_FORM_FIELDS_XML);
		
		FormService formService = Context.getFormService();
		Collection<FormField> containingAnyFormField = makeFormFieldCollectionSample(formService);
		
		List<Form> formsReturned = formService.getForms(null, null, null, null, containingAnyFormField, null, null);
		
		Integer currentNumberOfForms = formsReturned.size();
		
		assertEquals(numberOfExpectedForms, currentNumberOfForms);
		assertTrue(wasFormsSuccessfullyFilteredByMatchingFormFieldsInContainingAnyFormField(containingAnyFormField,
		    formsReturned));
		
	}
	
	private Collection<FormField> makeFormFieldCollectionSample(FormService formService) {
		int formFieldAIdentifier = 2;
		int formFieldBIdentifier = 9;
		
		Collection<FormField> containingAnyFormField = new ArrayList<>();
		FormField formFieldA = formService.getFormField(formFieldAIdentifier);
		FormField formFieldB = formService.getFormField(formFieldBIdentifier);
		containingAnyFormField.add(formFieldA);
		containingAnyFormField.add(formFieldB);
		return containingAnyFormField;
	}
	
	private boolean wasFormsSuccessfullyFilteredByMatchingFormFieldsInContainingAnyFormField(
	        Collection<FormField> containingAnyFormField, List<Form> formsReturned) {
		
		for (Form form : formsReturned) {
			if (!doesFormContainAnyFormField(form, containingAnyFormField)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean doesFormContainAnyFormField(Form form, Collection<FormField> containingAnyFormField) {
		
		Collection<FormField> formFieldsWithinForm = form.getFormFields();
		
		for (FormField formField : containingAnyFormField) {
			
			if (formFieldsWithinForm.contains(formField)) {
				return true;
			}
			
		}
		
		return false;
	}
	
	/**
	 * @see FormService#saveFieldType(FieldType)
	 */
	@Test
	public void saveFieldType_shouldCreateNewFieldType() {
		FieldType fieldType = new FieldType();
		
		fieldType.setName("testing");
		fieldType.setDescription("desc");
		fieldType.setIsSet(true);
		
		FormService formService = Context.getFormService();
		
		formService.saveFieldType(fieldType);
		
		Assert.assertNotNull(formService.getFieldType(fieldType.getFieldTypeId()));
	}
	
	/**
	 * @see FormService#saveFieldType(FieldType)
	 */
	@Test
	public void saveFieldType_shouldUpdateExistingFieldType() {
		FormService formService = Context.getFormService();
		
		FieldType fieldType = formService.getFieldType(1);
		Assert.assertNotNull(fieldType);
		
		fieldType.setName("SOME OTHER NEW NAME");
		
		formService.saveFieldType(fieldType);
		
		FieldType refetchedFieldType = formService.getFieldType(1);
		assertEquals("SOME OTHER NEW NAME", refetchedFieldType.getName());
	}
	
	/**
	 * @see FormService#duplicateForm(Form)
	 */
	@Test
	public void duplicateForm_shouldClearChangedDetailsAndUpdateCreationDetails() {
		Date startOfTest = DateUtil.truncateToSeconds(new Date());
		FormService formService = Context.getFormService();
		Form form = formService.getForm(1);
		
		Form dupForm = formService.duplicateForm(form);
		
		Assert.assertNull(dupForm.getChangedBy());
		Assert.assertNull(dupForm.getDateChanged());
		assertEquals(Context.getAuthenticatedUser(), dupForm.getCreator());
		assertFalse(dupForm.getDateCreated().before(startOfTest));
	}
	
	/**
	 * @see FormService#getFormField(Form,Concept,Collection<QFormField;>,null)
	 */
	@Test
	public void getFormField_shouldSimplyReturnNullForNonexistentConcepts() {
		// test a non existent concept
		assertNull(Context.getFormService().getFormField(new Form(1), new Concept(293934), null, false));
	}
	
	/**
	 * @see FormService#getFormField(Form,Concept,Collection<QFormField;>,null)
	 */
	@Test
	public void getFormField_shouldSimplyReturnNullForNonexistentForms() {
		// test a non existent form
		assertNull(Context.getFormService().getFormField(new Form(12343), new Concept(293934), null, false));
	}
	
	/**
	 * @see FormService#duplicateForm(Form)
	 */
	@Test
	public void duplicateForm_shouldGiveANewUuidToTheDuplicatedForm() {
		FormService formService = Context.getFormService();
		Form form = formService.getForm(1);
		String originalUUID = form.getUuid();
		
		Form dupForm = formService.duplicateForm(form);
		Assert.assertNotNull(dupForm.getUuid());
		Assert.assertNotSame(originalUUID, dupForm.getUuid());
	}
	
	/**
	 * @see FormService#getFieldAnswerByUuid(String)
	 */
	@Test
	public void getFieldAnswerByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getFormService().getFieldAnswerByUuid("some invalid uuid"));
	}
	
	/**
	 * @see FormService#getFieldByUuid(String)
	 */
	@Test
	public void getFieldByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "db016b7d-39a5-4911-89da-0eefbfef7cb2";
		Field field = Context.getFormService().getFieldByUuid(uuid);
		assertEquals(1, (int) field.getFieldId());
	}
	
	/**
	 * @see FormService#getFieldByUuid(String)
	 */
	@Test
	public void getFieldByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getFormService().getFieldByUuid("some invalid uuid"));
	}
	
	/**
	 * @see FormService#getFieldTypeByUuid(String)
	 */
	@Test
	public void getFieldTypeByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "e7016b7d-39a5-4911-89da-0eefbfef7cb5";
		FieldType fieldType = Context.getFormService().getFieldTypeByUuid(uuid);
		assertEquals(2, (int) fieldType.getFieldTypeId());
	}
	
	/**
	 * @see FormService#getFieldTypeByUuid(String)
	 */
	@Test
	public void getFieldTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getFormService().getFieldTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see FormService#getFormByUuid(String)
	 */
	@Test
	public void getFormByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "d9218f76-6c39-45f4-8efa-4c5c6c199f50";
		Form form = Context.getFormService().getFormByUuid(uuid);
		assertEquals(1, (int) form.getFormId());
	}
	
	/**
	 * @see FormService#getFormByUuid(String)
	 */
	@Test
	public void getFormByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getFormService().getFormByUuid("some invalid uuid"));
	}
	
	/**
	 * @see FormService#getFormFieldByUuid(String)
	 */
	@Test
	public void getFormFieldByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "1c822b7b-7840-463d-ba70-e0c8338a4c2d";
		FormField formField = Context.getFormService().getFormFieldByUuid(uuid);
		assertEquals(2, (int) formField.getFormFieldId());
	}
	
	/**
	 * @see FormService#getFormFieldByUuid(String)
	 */
	@Test
	public void getFormFieldByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getFormService().getFormFieldByUuid("some invalid uuid"));
	}
	
	/**
	 * @see FormService#saveFormField(FormField)
	 */
	@Test
	public void saveFormField_shouldPropagateSaveToTheFieldPropertyOnTheGivenFormField() {
		// create a new Field
		Field field = new Field();
		field.setName("This is a new field");
		field.setDescription("It should be saved along with the formField");
		
		// put that field on a new FormField.
		FormField formField = new FormField();
		formField.setField(field);
		formField.setForm(new Form(1));
		
		// save the FormField
		Context.getFormService().saveFormField(formField);
		
		// the uuid should be set by this method so that the field can be saved successfully
		Assert.assertNotNull(field.getUuid());
	}
	
	/**
	 * @see FormService#getFormsContainingConcept(Concept)
	 */
	@Test
	public void getFormsContainingConcept_shouldGetAllFormsForConcept() {
		Concept concept = Context.getConceptService().getConcept(3);
		
		assertEquals(1, Context.getFormService().getFormsContainingConcept(concept).size());
	}
	
	/**
	 * @see FormService#getFormsContainingConcept(Concept)
	 */
	@Test
	public void mergeDuplicateFields_shouldMergeDuplicateFieldsInFormFieldsAndThenPurgeTheDuplicateFields() {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(FORM_FIELDS_XML);
		
		Context.getFormService().mergeDuplicateFields();
		
		// duplicateField should no longer be referenced
		Assert.assertNull(Context.getFormService().getFieldByUuid("b1843148-da2f-4349-c9c7-1164b98d91dd"));
		
		// duplicateField should be purged
		assertEquals(2, Context.getFormService().getAllFields().size());
	}
	
	/**
	 * @throws ParseException
	 * @see FormService#saveFormResource(org.openmrs.FormResource)
	 */
	@Test
	public void saveFormResource_shouldPersistAFormResource() throws ParseException {
		Form form = Context.getFormService().getForm(1);
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName("Start Date");
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-16");
		resource.setValue(expected);
		
		Context.getFormService().saveFormResource(resource);
		Integer resourceId = resource.getFormResourceId();
		
		Context.clearSession();
		
		FormResource actual = Context.getFormService().getFormResource(resourceId);
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual.getValue());
	}
	
	/**
	 * @throws ParseException
	 * @see FormService#duplicateForm(Form)
	 */
	@Test
	public void duplicateForm_shouldCopyResourcesForOldFormToNewForm() throws ParseException {
		// save an original resource
		Form form = Context.getFormService().getForm(1);
		String name = "Start Date";
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-16");
		resource.setValue(expected);
		
		resource = Context.getFormService().saveFormResource(resource);
		Integer resourceId = resource.getFormResourceId();
		
		// duplicate the form
		Form newForm = Context.getFormService().duplicateForm(form);
		
		// get the resource
		FormResource actual = Context.getFormService().getFormResource(newForm, name);
		
		// check it
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected, actual.getValue());
	}
	
	/**
	 * @throws ParseException
	 * @see FormService#purgeFormResource(Form,String,String)
	 */
	@Test
	public void purgeFormResource_shouldDeleteAFormResource() throws ParseException {
		// save an original resource
		Form form = Context.getFormService().getForm(1);
		String name = "Start Date";
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date previous = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-16");
		resource.setValue(previous);
		
		resource = Context.getFormService().saveFormResource(resource);
		Integer resourceId = resource.getFormResourceId();
		
		// clear the session
		Context.clearSession();
		
		// find and delete the resource
		resource = Context.getFormService().getFormResource(resourceId);
		Context.getFormService().purgeFormResource(resource);
		
		// clear the session
		Context.flushSession();
		
		// try to find the resource
		resource = Context.getFormService().getFormResource(resourceId);
		Assert.assertNull(resource);
	}
	
	/**
	 * @throws ParseException
	 * @see FormService#saveFormResource(FormResource)
	 */
	@Test
	public void saveFormResource_shouldOverwriteAnExistingResourceWithSameName() throws ParseException {
		String name = "Start Date";
		
		// save an original resource
		Form form = Context.getFormService().getForm(1);
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date previous = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-16");
		resource.setValue(previous);
		
		Context.getFormService().saveFormResource(resource);
		
		// clear the session
		Context.flushSession();
		
		// save a new resource with the same name
		form = Context.getFormService().getForm(1);
		resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2010-10-16");
		resource.setValue(expected);
		Context.getFormService().saveFormResource(resource);
		
		// get the current value
		FormResource actual = Context.getFormService().getFormResource(form, name);
		
		Assert.assertFalse(previous.equals(actual.getValue()));
		Assert.assertEquals(expected, actual.getValue());
	}
	
	/**
	 * @throws ParseException
	 * @see FormService#purgeForm(Form)
	 */
	@Test
	public void purgeForm_shouldDeleteFormResourcesForDeletedForm() throws ParseException {
		// create a new form
		Form form = new Form();
		form.setName("form resource test form");
		form.setVersion("42");
		form.setDescription("bleh");
		form = Context.getFormService().saveForm(form);
		
		// save a resource
		String name = "Start Date";
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-16");
		resource.setValue(expected);
		
		Context.getFormService().saveFormResource(resource);
		
		// make sure the resource is saved
		FormResource actual = Context.getFormService().getFormResource(form, name);
		assertEquals(expected, actual.getValue());
		
		// retain the resource id
		Integer savedId = actual.getFormResourceId();
		
		// delete the form
		Context.getFormService().purgeForm(form);
		
		// check for the resource
		Assert.assertNull(Context.getFormService().getFormResource(savedId));
	}
	
	/**
	 * @throws IOException
	 * @see FormService#saveFormResource(FormResource)
	 */
	@Test
	public void saveFormResource_shouldBeAbleToSaveAnXSLT() throws IOException {
		// set up new form
		Form form = new Form();
		form.setName("form resource test form");
		form.setVersion("42");
		form.setDescription("bleh");
		form = Context.getFormService().saveForm(form);
		
		// save a resource
		String name = "org.openmrs.module.formentry.xslt";
		String expected = getResourceAsString(FORM_SAMPLE_RESOURCE);
		
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(name);
		resource.setDatatypeClassname("org.openmrs.customdatatype.datatype.LongFreeTextDatatype");
		resource.setValue(expected);
		Context.getFormService().saveFormResource(resource);
		
		// make sure the resource is saved
		Collection<FormResource> formResourcesForForm = Context.getFormService().getFormResourcesForForm(form);
		Assert.assertEquals(1, formResourcesForForm.size());
		FormResource actual = formResourcesForForm.iterator().next();
		Assert.assertEquals(expected, actual.getValue());
	}
	
	/**
	 * convert a resource path to a file into a string containing the file's contents
	 * 
	 * @param filename resource path to the file
	 * @return the contents of the file in a String
	 * @throws IOException 
	 */
	private String getResourceAsString(String filename) throws IOException {
		InputStream resource = this.getClass().getClassLoader().getResourceAsStream(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		while ((line = reader.readLine()) != null)
			sb.append(line).append("\n");
		
		reader.close();
		return sb.toString();
	}
	
	/**
	 * @see FormService#saveFormField(FormField)
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void saveFormField_shouldInjectFormFieldsFromSerializableComplexObsHandlers() {
		executeDataSet("org/openmrs/api/include/ConceptComplex.xml");
		Context.getObsService().registerHandler("NeigborHandler", new NeighborHandler());
		Concept concept = Context.getConceptService().getConcept(6043);
		Field field = new Field();
		field.setName("neighbor");
		field.setConcept(concept);
		
		FormField formField = new FormField();
		formField.setField(field);
		FormService fs = Context.getFormService();
		formField.setForm(fs.getForm(1));
		
		List<FormField> originalFormFields = fs.getAllFormFields();
		int initialFormFieldCount = originalFormFields.size();
		formField = fs.saveFormField(formField);
		List<FormField> updatedFormFields = fs.getAllFormFields();
		//should have this and the two form fields from the handler
		Assert.assertEquals(initialFormFieldCount += 3, updatedFormFields.size());
		//get the formfields added by the handler and check their parent
		List<FormField> childFormFields = ListUtils.subtract(updatedFormFields, originalFormFields);
		childFormFields.remove(formField);//exclude this form field
		for (FormField ff : childFormFields) {
			Assert.assertEquals(formField, ff.getParent());
		}
	}
	
	/**
	 * This is a test complex obs handler that adds 2 form fields
	 */
	private class NeighborHandler implements SerializableComplexObsHandler {
		
		@Override
		public Set<FormField> getFormFields() {
			Set<FormField> formFields = new HashSet<>();
			Field firstName = new Field();
			firstName.setName("firstName");
			Field lastName = new Field();
			lastName.setName("lastName");
			
			FormField firstNameFormField = new FormField();
			firstNameFormField.setField(firstName);
			FormField lastNameFormField = new FormField();
			lastNameFormField.setField(lastName);
			
			formFields.add(firstNameFormField);
			formFields.add(lastNameFormField);
			
			return formFields;
		}
		
		@Override
		public Obs saveObs(Obs obs) throws APIException {
			return null;
		}
		
		@Override
		public Obs getObs(Obs obs, String view) {
			return null;
		}
		
		@Override
		public boolean purgeComplexData(Obs obs) {
			return false;
		}
		
		@Override
		public String serializeFormData(String data) {
			return null;
		}
		
		@Override
		public String[] getSupportedViews() {
			return new String[0];
		}
		
		@Override
		public boolean supportsView(String view) {
			return false;
		}
	}
	
	/**
	 * Creates a new Global Property to lock forms by setting its value
	 * @param propertyValue value for forms locked GP
	 */
	public void createFormsLockedGPAndSetValue(String propertyValue) {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FORMS_LOCKED);
		gp.setPropertyValue(propertyValue);
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
	
	/**
	 * @see FormService#saveForm(Form)
	 */
	@Test
	public void saveForm_shouldSaveGivenFormSuccessfully() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = new Form();
		form.setName("new form");
		form.setVersion("1.0");
		form.setDescription("testing TRUNK-4030");
		
		Form formSave = fs.saveForm(form);
		
		assertTrue(form.equals(formSave));
	}
	
	/**
	 * @see FormService#saveForm(Form)
	 */
	@Test
	public void saveForm_shouldUpdateAnExistingForm() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = fs.getForm(1);
		form.setName("modified basic form");
		fs.saveForm(form);
		
		Form formUpdate = fs.getForm(1);
		
		assertTrue(form.equals(formUpdate));
	}
	
	/**
	 * @see FormService#saveForm(Form)
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	public void saveForm_shouldThrowAnErrorWhenTryingToSaveAnExistingFormWhileFormsAreLocked() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		form.setName("modified basic form");
		
		fs.saveForm(form);
	}
	
	/**
	 * @see FormService#saveForm(Form)
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	public void saveForm_shouldThrowAnErrorWhenTryingToSaveANewFormWhileFormsAreLocked() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = new Form();
		form.setName("new form");
		form.setVersion("1.0");
		form.setDescription("testing TRUNK-4030");
		
		fs.saveForm(form);
	}
	
	/**
	 * @see FormService#purgeForm(Form)
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	public void purgeForm_shouldThrowAnErrorWhenTryingToDeleteFormWhileFormsAreLocked() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		
		fs.purgeForm(form);
	}
	
	/**
	 * @see FormService#purgeForm(Form)
	 */
	@Test
	public void purgeForm_shouldDeleteGivenFormSuccessfully() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = fs.getForm(1);
		fs.purgeForm(form);
		
		assertNull(fs.getForm(1));
	}
	
	/**
	 * @see FormService#duplicateForm(Form)}
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	public void duplicateForm_shouldThrowAnErrorWhenTryingToDuplicateFormWhileFormsAreLocked() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		fs.duplicateForm(form);
	}
	
	/**
	 * @see FormService#duplicateForm(Form)
	 */
	@Test
	public void duplicateForm_shouldDuplicateGivenFormSuccessfully() {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = fs.getForm(1);
		Form duplicateForm = fs.duplicateForm(form);
		assertEquals(form, duplicateForm);
	}

	private Form createMockForm(Boolean retried) {
		Form form1 = new Form();
		form1.setName("form_name_2");
		form1.setVersion("2.0");
		form1.setDescription("description_2");
		if (retried) {
			form1.setRetired(true);
			form1.setRetireReason("For testing");
		}
		else {
			form1.setRetired(false);
		}
		return form1;
	}

	/**
	 * @see FormService#getAllForms()
	 */
	@Test
	public void getAllForms_shouldReturnAllForms() {
		List<Form> forms = Context.getFormService().getAllForms();
		int currentFormsSize = forms.size();
		assertEquals(1, currentFormsSize);
		
		Context.getFormService().saveForm(createMockForm(false));
		
		forms = Context.getFormService().getAllForms();
		assertEquals(currentFormsSize + 1, forms.size());
	}

	/**
	 * @see FormService#getAllForms()
	 */
	@Test
	public void getAllForms_shouldReturnAllFormsWithRetiredForms() {
		List<Form> forms = Context.getFormService().getAllForms();
		int currentFormsSize = forms.size();
		assertEquals(1, currentFormsSize);

		Context.getFormService().saveForm(createMockForm(true));

		forms = Context.getFormService().getAllForms();
		assertEquals(currentFormsSize + 1, forms.size());
	}

	/**
	 * @see FormService#getAllForms(boolean)
	 */
	@Test
	public void getAllForms_shouldReturnAllFormsWithRetiredIfParameterMentionedAsTrue() {
		List<Form> forms = Context.getFormService().getAllForms(true);
		int currentFormsSize = forms.size();
		assertEquals(1, currentFormsSize);

		Context.getFormService().saveForm(createMockForm(true));

		forms = Context.getFormService().getAllForms(true);
		assertEquals(currentFormsSize + 1 , forms.size());
	}

	/**
	 * @see FormService#getAllForms(boolean)
	 */
	@Test
	public void getAllForms_shouldReturnAllFormsWithOutRetiredIfParameterMentionedAsFalse() {
		List<Form> forms = Context.getFormService().getAllForms(false);
		int currentFormsSize = forms.size();
		assertEquals(1, currentFormsSize);

		Context.getFormService().saveForm(createMockForm(true));

		forms = Context.getFormService().getAllForms(false);
		assertEquals(currentFormsSize , forms.size());
	}

	/**
	 * @see FormService#getForm(String)
	 */
	@Test
	public void getForm_shouldReturnNullIfFormNotFound() {
		List<Form> forms = Context.getFormService().getAllForms();
		boolean formNameFound = false;
		final String formName = "Sample_Form_Not_In_List";
		for (Form node:forms) {
			if (node.getName().equals(formName)) {
				formNameFound = true;
			}
		}
		assertFalse(formNameFound);
		
		Form form = Context.getFormService().getForm(formName);
		assertNull(form);
	}

	/**
	 * @see FormService#getForm(String)
	 */
	@Test
	public void getForm_shouldReturnFormIfFormFound() {
		Form form = Context.getFormService().getForm("form_name_2");
		assertNull(form);

		// create Form with form_name_2 and version 2.0
		Context.getFormService().saveForm(createMockForm(false));
		form = Context.getFormService().getForm("form_name_2");
		assertNotNull(form);
	}

	@Test
	public void getForm_shouldReturnFormIfFormFoundWithNameAndVersion() {
		Form form = Context.getFormService().getForm("form_name_2", "2.0");
		assertNull(form);

		// create Form with form_name_2 and version 2.0
		Context.getFormService().saveForm(createMockForm(false));
		form = Context.getFormService().getForm("form_name_2", "2.0");
		assertNotNull(form);
	}

	@Test
	public void getForm_shouldReturnNullIfFormNotFoundWithNameOrVersion() {
		Form form = Context.getFormService().getForm("form_name_2", "2.0");
		assertNull(form);

		// create Form with form_name_2 and version 2.0
		Context.getFormService().saveForm(createMockForm(false));
		form = Context.getFormService().getForm("form_name_2", "2.0");
		assertNotNull(form);

		form = Context.getFormService().getForm("form_name_3", "2.0");
		assertNull(form);

		form = Context.getFormService().getForm("form_name_2", "3.0");
		assertNull(form);

		form = Context.getFormService().getForm("form_name_3", "3.0");
		assertNull(form);
	}
}
