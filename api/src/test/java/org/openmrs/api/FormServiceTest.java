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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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
import org.openmrs.test.Verifies;
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
	public void shouldFormCreateUpdateDelete() throws Exception {
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
		assertTrue(form2.isRetired());
		assertTrue(form2.getRetireReason().equals("reason"));
		
		formService.unretireForm(form2);
		assertFalse(form2.isRetired());
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
	public void shouldFieldCreateModifyDelete() throws Exception {
		
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
	 * @see {@link FormService#getFormField(Form,Concept,Collection<QFormField;>,null)}
	 */
	@Test
	@Verifies(value = "should ignore formFields passed to ignoreFormFields", method = "getFormField(Form,Concept,Collection<QFormField;>,null)")
	public void getFormField_shouldIgnoreFormFieldsPassedToIgnoreFormFields() throws Exception {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormField ff = Context.getFormService().getFormField(new Form(1), new Concept(1));
		assertNotNull(ff); // sanity check
		
		// test that the first formfield is ignored when a second fetch
		// is done on the same form and same concept
		List<FormField> ignoreFormFields = new Vector<FormField>();
		ignoreFormFields.add(ff);
		FormField ff2 = Context.getFormService().getFormField(new Form(1), new Concept(1), ignoreFormFields, false);
		assertNotNull(ff2);
		assertNotSame(ff, ff2);
		
	}
	
	/**
	 * @see {@link FormService#getFormField(Form,Concept,Collection<QFormField;>,null)}
	 */
	@Test
	@Verifies(value = "should not fail with null ignoreFormFields argument", method = "getFormField(Form,Concept,Collection<QFormField;>,null)")
	public void getFormField_shouldNotFailWithNullIgnoreFormFieldsArgument() throws Exception {
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
	@Verifies(value = "should return duplicate form when given fields included in form multiple times", method = "getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)")
	public void getForms_shouldReturnDuplicateFormWhenGivenFieldsIncludedInFormMultipleTimes() throws Exception {
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		List<Field> fields = new Vector<Field>();
		fields.add(new Field(1));
		
		List<Form> forms = formService.getForms(null, null, null, null, null, null, fields);
		
		assertEquals(3, forms.size());
	}
	
	/**
	 * @throws Exception 
	 * @see FormService#getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)
	 * @verifies return forms containing all form fields in containingAllFormFields
	 */
	@Test
	public void getForms_shouldReturnFormsContainingAllFormFieldsInContainingAllFormFields() throws Exception {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		Set<FormField> formFields = new HashSet<FormField>();
		formFields.add(new FormField(3));
		formFields.add(new FormField(5));
		formFields.add(new FormField(7));
		
		List<Form> forms = formService.getForms(null, null, null, null, null, formFields, null);
		assertEquals(1, forms.size());
		
		formFields = new HashSet<FormField>();
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
	@Verifies(value = "return forms that have any matching formFields in containingAnyFormField", method = "getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)")
	public void getForms_shouldReturnFormsThatHaveAnyMatchingFormFieldsInContainingAnyFormField() throws Exception {
		
		Integer numberOfExpectedForms = new Integer(2);
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(MULTIPLE_FORMS_FORM_FIELDS_XML);
		
		FormService formService = Context.getFormService();
		Collection<FormField> containingAnyFormField = makeFormFieldCollectionSample(formService);
		
		List<Form> formsReturned = formService.getForms(null, null, null, null, containingAnyFormField, null, null);
		
		Integer currentNumberOfForms = new Integer(formsReturned.size());
		
		assertEquals(numberOfExpectedForms, currentNumberOfForms);
		assertTrue(wasFormsSuccessfullyFilteredByMatchingFormFieldsInContainingAnyFormField(containingAnyFormField,
		    formsReturned));
		
	}
	
	private Collection<FormField> makeFormFieldCollectionSample(FormService formService) {
		int formFieldAIdentifier = 2;
		int formFieldBIdentifier = 9;
		
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
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
	 * @see {@link FormService#saveFieldType(FieldType)}
	 */
	@Test
	@Verifies(value = "should create new field type", method = "saveFieldType(FieldType)")
	public void saveFieldType_shouldCreateNewFieldType() throws Exception {
		FieldType fieldType = new FieldType();
		
		fieldType.setName("testing");
		fieldType.setDescription("desc");
		fieldType.setIsSet(true);
		
		FormService formService = Context.getFormService();
		
		formService.saveFieldType(fieldType);
		
		Assert.assertNotNull(formService.getFieldType(fieldType.getFieldTypeId()));
	}
	
	/**
	 * @see {@link FormService#saveFieldType(FieldType)}
	 */
	@Test
	@Verifies(value = "should update existing field type", method = "saveFieldType(FieldType)")
	public void saveFieldType_shouldUpdateExistingFieldType() throws Exception {
		FormService formService = Context.getFormService();
		
		FieldType fieldType = formService.getFieldType(1);
		Assert.assertNotNull(fieldType);
		
		fieldType.setName("SOME OTHER NEW NAME");
		
		formService.saveFieldType(fieldType);
		
		FieldType refetchedFieldType = formService.getFieldType(1);
		assertEquals("SOME OTHER NEW NAME", refetchedFieldType.getName());
	}
	
	/**
	 * @see {@link FormService#duplicateForm(Form)}
	 */
	@Test
	@Verifies(value = "should clear changed details and update creation details", method = "duplicateForm(Form)")
	public void duplicateForm_shouldClearChangedDetailsAndUpdateCreationDetails() throws Exception {
		FormService formService = Context.getFormService();
		Form form = formService.getForm(1);
		
		Form dupForm = formService.duplicateForm(form);
		
		// some of these assertions are affected by inserting resources after creating the form
		//Assert.assertNull(dupForm.getChangedBy());
		//Assert.assertNull(dupForm.getDateChanged());
		assertEquals(Context.getAuthenticatedUser(), dupForm.getCreator());
		long oneMinuteDelta = 60 * 1000;
		assertEquals(new Date().getTime(), dupForm.getDateCreated().getTime(), oneMinuteDelta);
	}
	
	/**
	 * @see {@link FormService#getFormField(Form,Concept,Collection<QFormField;>,null)}
	 */
	@Test
	@Verifies(value = "should simply return null for nonexistent concepts", method = "getFormField(Form,Concept,Collection<QFormField;>,null)")
	public void getFormField_shouldSimplyReturnNullForNonexistentConcepts() throws Exception {
		// test a non existent concept
		assertNull(Context.getFormService().getFormField(new Form(1), new Concept(293934)));
	}
	
	/**
	 * @see {@link FormService#getFormField(Form,Concept,Collection<QFormField;>,null)}
	 */
	@Test
	@Verifies(value = "should simply return null for nonexistent forms", method = "getFormField(Form,Concept,Collection<QFormField;>,null)")
	public void getFormField_shouldSimplyReturnNullForNonexistentForms() throws Exception {
		// test a non existent form
		assertNull(Context.getFormService().getFormField(new Form(12343), new Concept(293934)));
	}
	
	/**
	 * @see {@link FormService#duplicateForm(Form)}
	 */
	@Test
	@Verifies(value = "should give a new uuid to the duplicated form", method = "duplicateForm(Form)")
	public void duplicateForm_shouldGiveANewUuidToTheDuplicatedForm() throws Exception {
		FormService formService = Context.getFormService();
		Form form = formService.getForm(1);
		String originalUUID = form.getUuid();
		
		Form dupForm = formService.duplicateForm(form);
		Assert.assertNotNull(dupForm.getUuid());
		Assert.assertNotSame(originalUUID, dupForm.getUuid());
	}
	
	/**
	 * @see {@link FormService#getFieldAnswerByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getFieldAnswerByUuid(String)")
	public void getFieldAnswerByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getFormService().getFieldAnswerByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link FormService#getFieldByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getFieldByUuid(String)")
	public void getFieldByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "db016b7d-39a5-4911-89da-0eefbfef7cb2";
		Field field = Context.getFormService().getFieldByUuid(uuid);
		assertEquals(1, (int) field.getFieldId());
	}
	
	/**
	 * @see {@link FormService#getFieldByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getFieldByUuid(String)")
	public void getFieldByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getFormService().getFieldByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link FormService#getFieldTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getFieldTypeByUuid(String)")
	public void getFieldTypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "e7016b7d-39a5-4911-89da-0eefbfef7cb5";
		FieldType fieldType = Context.getFormService().getFieldTypeByUuid(uuid);
		assertEquals(2, (int) fieldType.getFieldTypeId());
	}
	
	/**
	 * @see {@link FormService#getFieldTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getFieldTypeByUuid(String)")
	public void getFieldTypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getFormService().getFieldTypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link FormService#getFormByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getFormByUuid(String)")
	public void getFormByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "d9218f76-6c39-45f4-8efa-4c5c6c199f50";
		Form form = Context.getFormService().getFormByUuid(uuid);
		assertEquals(1, (int) form.getFormId());
	}
	
	/**
	 * @see {@link FormService#getFormByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getFormByUuid(String)")
	public void getFormByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getFormService().getFormByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link FormService#getFormFieldByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getFormFieldByUuid(String)")
	public void getFormFieldByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "1c822b7b-7840-463d-ba70-e0c8338a4c2d";
		FormField formField = Context.getFormService().getFormFieldByUuid(uuid);
		assertEquals(2, (int) formField.getFormFieldId());
	}
	
	/**
	 * @see {@link FormService#getFormFieldByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getFormFieldByUuid(String)")
	public void getFormFieldByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getFormService().getFormFieldByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link FormService#saveFormField(FormField)}
	 */
	@Test
	@Verifies(value = "should propagate save to the Field property on the given FormField", method = "saveFormField(FormField)")
	public void saveFormField_shouldPropagateSaveToTheFieldPropertyOnTheGivenFormField() throws Exception {
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
	 * @see {@link FormService#getFormsContainingConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should get all forms for concept", method = "getFormsContainingConcept(Concept)")
	public void getFormsContainingConcept_shouldGetAllFormsForConcept() throws Exception {
		Concept concept = Context.getConceptService().getConcept(3);
		
		assertEquals(1, Context.getFormService().getFormsContainingConcept(concept).size());
	}
	
	/**
	 * @see {@link FormService#getFormsContainingConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should merge fields with similar attributes", method = "mergeDuplicateFields()")
	public void mergeDuplicateFields_shouldMergeDuplicateFieldsInFormFieldsAndThenPurgeTheDuplicateFields() throws Exception {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(FORM_FIELDS_XML);
		
		Context.getFormService().mergeDuplicateFields();
		
		// duplicateField should no longer be referenced
		Assert.assertNull(Context.getFormService().getFieldByUuid("b1843148-da2f-4349-c9c7-1164b98d91dd"));
		
		// duplicateField should be purged
		assertEquals(2, Context.getFormService().getAllFields().size());
	}
	
	/**
	 * @see FormService#saveFormResource(org.openmrs.FormResource)
	 */
	@Test
	@Verifies(value = "should persist a FormResource", method = "saveFormResource()")
	public void saveFormResource_shouldPersistAFormResource() throws Exception {
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
	 * @see {@link FormService#duplicateForm(Form)}
	 */
	@Test
	@Verifies(value = "should copy resources for old form to new form", method = "duplicateForm(Form)")
	public void duplicateForm_shouldCopyResourcesForOldFormToNewForm() throws Exception {
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
	 * @see {@link FormService#purgeFormResource(Form,String,String)}
	 */
	@Test
	@Verifies(value = "should delete a form resource", method = "purgeFormResource(Form,String,String)")
	public void purgeFormResource_shouldDeleteAFormResource() throws Exception {
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
	 * @see {@link FormService#saveFormResource(FormResource)}
	 */
	@Test
	@Verifies(value = "should overwrite an existing resource with same name", method = "saveFormResource(FormResource)")
	public void saveFormResource_shouldOverwriteAnExistingResourceWithSameName() throws Exception {
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
	 * @see {@link FormService#purgeForm(Form)}
	 */
	@Test
	@Verifies(value = "should delete form resources for deleted form", method = "purgeForm(Form)")
	public void purgeForm_shouldDeleteFormResourcesForDeletedForm() throws Exception {
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
	 * @see {@link FormService#saveFormResource(FormResource)}
	 */
	@Test
	@Verifies(value = "should be able to save an XSLT", method = "saveFormResource(FormResource)")
	public void saveFormResource_shouldBeAbleToSaveAnXSLT() throws Exception {
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
	 * @see {@link FormService#saveFormField(FormField)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Verifies(value = "should inject form fields from serializable complex obs handlers", method = "saveFormField(FormField)")
	public void saveFormField_shouldInjectFormFieldsFromSerializableComplexObsHandlers() throws Exception {
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
		
		public Set<FormField> getFormFields() {
			Set<FormField> formFields = new HashSet<FormField>();
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
		
		public Obs saveObs(Obs obs) throws APIException {
			return null;
		}
		
		public Obs getObs(Obs obs, String view) {
			return null;
		}
		
		public boolean purgeComplexData(Obs obs) {
			return false;
		}
		
		public String serializeFormData(String data) {
			return null;
		}
		
		public String[] getSupportedViews() {
			return new String[0];
		}
		
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
	 * @see {@link FormService#saveForm(Form)}
	 */
	@Test
	@Verifies(method = "saveForm(Form)", value = "should save given form successfully")
	public void saveForm_shouldSaveGivenFormSuccessfully() throws Exception {
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
	 * @see {@link FormService#saveForm(Form)}
	 */
	@Test
	@Verifies(method = "saveForm(Form)", value = "should update an existing form")
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
	 * @see {@link FormService#saveForm(Form)}
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	@Verifies(method = "saveForm(Form)", value = "should throw an error when trying to save an existing form while forms are locked")
	public void saveForm_shouldThrowAnErrorWhenTryingToSaveAnExistingFormWhileFormsAreLocked() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		form.setName("modified basic form");
		
		fs.saveForm(form);
	}
	
	/**
	 * @see {@link FormService#saveForm(Form)}
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	@Verifies(method = "saveForm(Form)", value = "should throw an error when trying to save a new form while forms are locked")
	public void saveForm_shouldThrowAnErrorWhenTryingToSaveANewFormWhileFormsAreLocked() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = new Form();
		form.setName("new form");
		form.setVersion("1.0");
		form.setDescription("testing TRUNK-4030");
		
		fs.saveForm(form);
	}
	
	/**
	 * @see {@link FormService#purgeForm(Form)}
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	@Verifies(method = "purgeForm(Form)", value = "should throw an error when trying to delete a form while forms are locked")
	public void purgeForm_shouldThrowAnErrorWhenTryingToDeleteFormWhileFormsAreLocked() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		
		fs.purgeForm(form);
	}
	
	/**
	 * @see {@link FormService#purgeForm(Form)}
	 */
	@Test
	@Verifies(method = "purgeForm(Form)", value = "should delete given form successfully")
	public void purgeForm_shouldDeleteGivenFormSuccessfully() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = fs.getForm(1);
		fs.purgeForm(form);
		
		assertNull(fs.getForm(1));
	}
	
	/**
	 * @see {@link FormService#duplicateForm(Form)}}
	 * @throws FormsLockedException
	 */
	@Test(expected = FormsLockedException.class)
	@Verifies(method = "duplicateForm(Form)", value = "should throw an error when trying to duplicate a form while forms are locked")
	public void duplicateForm_shouldThrowAnErrorWhenTryingToDuplicateFormWhileFormsAreLocked() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("true");
		
		Form form = fs.getForm(1);
		fs.duplicateForm(form);
	}
	
	/**
	 * @see {@link FormService#duplicateForm(Form)}
	 */
	@Test
	@Verifies(method = "duplicateForm(Form)", value = "should duplicate given form successfully")
	public void duplicateForm_shouldDuplicateGivenFormSuccessfully() throws Exception {
		FormService fs = Context.getFormService();
		createFormsLockedGPAndSetValue("false");
		
		Form form = fs.getForm(1);
		Form duplicateForm = fs.duplicateForm(form);
		assertEquals(form, duplicateForm);
	}
}
