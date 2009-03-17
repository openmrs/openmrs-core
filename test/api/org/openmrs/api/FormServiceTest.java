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
		assertTrue(form2.getRetiredReason().equals("reason"));
		
		formService.unretireForm(form2);
		assertFalse(form2.isRetired());
		assertNull(form2.getRetiredReason());
		
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
	 * Tests the FormService.getFormFields(Form, Concept) and getFormFields(Form,Concept,Collection)
	 * methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetFormFieldsByFormAndConcept() throws Exception {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		
		FormService formService = Context.getFormService();
		
		Form form = new Form(1);
		Concept concept = new Concept(1);
		List<FormField> ignoreFormFields = new Vector<FormField>();
		
		// test that a null ignoreFormFields doens't error out
		FormField ff = formService.getFormField(form, concept, null, false);
		assertNotNull(ff);
		
		ff = formService.getFormField(form, concept);
		assertNotNull(ff);
		
		// test a non existent concept
		assertNull(formService.getFormField(form, new Concept(293934)));
		
		// test a non existent form
		assertNull(formService.getFormField(new Form(12343), new Concept(293934)));
		
		// test that the first formfield is ignored when a second fetch
		// is done on the same form and same concept
		ignoreFormFields.add(ff);
		FormField ff2 = formService.getFormField(form, concept, ignoreFormFields, false);
		assertNotNull(ff2);
		assertNotSame(ff, ff2);
		
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
	
}
