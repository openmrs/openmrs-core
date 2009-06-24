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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO clean up and finish this test for all methods in FormService
 * 
 * @see FormService
 */
public class FormServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-initialFieldTypes.xml";
	
	/**
	 * Creates then updates a form
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
		FormField ff = Context.getFormService().getFormField(new Form(1), new Concept(1), null, false);
		assertNotNull(ff);
	}
	
	/**
	 * Make sure that multiple forms are returned if a field is on a form more than once
	 * 
	 * @see {@link FormService#getForms(String, Boolean, java.util.Collection, Boolean, java.util.Collection, java.util.Collection, java.util.Collection) 
	 */
	@Test
	@Verifies(value = "should get multiple of the same form by field", method = "getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)")
	public void getForms_shouldGetMultipleOfTheSameFormByField() throws Exception {
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		List<Field> fields = new Vector<Field>();
		fields.add(new Field(1));
		
		List<Form> forms = formService.getForms(null, null, null, null, null, null, fields);
		
		Assert.assertEquals(3, forms.size());
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
		Assert.assertEquals("SOME OTHER NEW NAME", refetchedFieldType.getName());
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
		Assert.assertNull(dupForm.getChangedBy());
		Assert.assertNull(dupForm.getDateChanged());
		Assert.assertEquals(Context.getAuthenticatedUser(), dupForm.getCreator());
		long oneMinuteDelta = 60 * 1000;
		Assert.assertEquals(new Date().getTime(), dupForm.getDateCreated().getTime(), oneMinuteDelta);
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
		Assert.assertEquals(1, (int) field.getFieldId());
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
		Assert.assertEquals(2, (int) fieldType.getFieldTypeId());
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
		Assert.assertEquals(1, (int) form.getFormId());
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
		Assert.assertEquals(2, (int) formField.getFormFieldId());
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
}
