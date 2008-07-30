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
package org.openmrs.test.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO clean up and finish this test for all methods in FormService
 */
public class FormServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/test/api/include/FormServiceTest-initialFieldTypes.xml";
	
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
	}
	
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
		
		formService.createForm(form1);
		
		//testing get form 
		
		Form form2 = formService.getForm(form1.getFormId());
		
		String name2 = "form name2";
		String version2 = "2.0";
		String descript2 = "descript2";
		
		form2.setName(name2);
		form2.setVersion(version2);
		form2.setDescription(descript2);
		
		formService.updateForm(form2);
		
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
		
		formService.deleteForm(form2);
		//formService.deleteForm(form1); //deleting a deleted form
	}
	
	/**
	 * Creates then updates a form field
	 * 
	 * TODO fix and activate this test method
	 * 
	 * @throws Exception
	 */
	public void xtestFormFieldCreateUpdateDelete() throws Exception {
		FormService formService = Context.getFormService();
		
		//testing creation
		
		List<Form> forms = formService.getForms();
		assertNotNull(forms);
		assertTrue(forms.size() > 2);
		
		List<Field> fields = formService.getFields();
		assertNotNull(fields);
		assertTrue(fields.size() > 2);
		
		FormField subFormField1 = null;
		Form form1 = forms.get(1);
		Field field1 = fields.get(1);
		Integer fieldNumber1 = new Integer(1);
		String fieldPart1 = "part1";
		Integer pageNumber1 = new Integer(1);
		Integer minOccurs1 = new Integer(1);
		Integer maxOccurs1 = new Integer(1);
		Boolean required1 = true;
		
		FormField formField1 = new FormField();
		
		formField1.setParent(subFormField1);
		formField1.setForm(form1);
		formField1.setField(field1);
		formField1.setFieldNumber(fieldNumber1);
		formField1.setFieldPart(fieldPart1);
		formField1.setPageNumber(pageNumber1);
		formField1.setMinOccurs(minOccurs1);
		formField1.setMaxOccurs(maxOccurs1);
		formField1.setRequired(required1);
		
		//formService.createFormField(formField1);
		
		//testing update
		
		FormField formField2 = null; //formService.getFormField(formField1.getFormFieldId());
		
		FormField subFormField2 = null;
		Form form2 = forms.get(2);
		Field field2 = fields.get(2);
		Integer fieldNumber2 = new Integer(2);
		String fieldPart2 = "part2";
		Integer pageNumber2 = new Integer(2);
		Integer minOccurs2 = new Integer(2);
		Integer maxOccurs2 = new Integer(2);
		Boolean required2 = false;
		
		formField2.setParent(subFormField2);
		formField2.setForm(form2);
		formField2.setField(field2);
		formField2.setFieldNumber(fieldNumber2);
		formField2.setFieldPart(fieldPart2);
		formField2.setPageNumber(pageNumber2);
		formField2.setMinOccurs(minOccurs2);
		formField2.setMaxOccurs(maxOccurs2);
		formField2.setRequired(required2);
		
		//formService.updateFormField(formField2);
		
		//testing correct update
		
		FormField formField3 = null; //formService.getFormField(formField2.getFormFieldId());
		
		assertTrue(formField3.equals(formField2));
		
		assertFalse(formField3.getParent().equals(formField1.getParent()));
		assertFalse(formField3.getForm().equals(formField1.getForm()));
		assertFalse(formField3.getField().equals(formField1.getField()));
		assertFalse(formField3.getFieldNumber().equals(formField1.getFieldNumber()));
		assertFalse(formField3.getFieldPart().equals(formField1.getFieldPart()));
		assertFalse(formField3.getPageNumber().equals(formField1.getPageNumber()));
		assertFalse(formField3.getMinOccurs().equals(formField1.getMinOccurs()));
		assertFalse(formField3.getMaxOccurs().equals(formField1.getMaxOccurs()));
		assertFalse(formField3.isRequired().equals(formField1.isRequired()));
		
		//testing deletion
		
		//formService.deleteFormField(formField3);
		//formService.deleteFormField(formField3);
		//assertNull(formService.getFormField(formField3.getFormFieldId()));
		
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
		
		Concept concept1  = conceptService.getConcept(1);
		String  name1     = "name1";
		String  descript1 = "descript1";
		FieldType fieldtype1 = formService.getFieldTypes().get(0);
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
		
		formService.updateField(field1);
		
		//testing update
		
		Field field2 = formService.getField(field1.getFieldId());
		
		Concept concept2  = conceptService.getConcept(2);
		String  name2     = "name2";
		String  descript2 = "descript2";
		FieldType fieldtype2 = formService.getFieldTypes().get(1);
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
		
		formService.updateField(field2);
		
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
		
		formService.deleteField(field3);
		//formService.deleteField(field1);
		
		assertNull(formService.getField(field3.getFieldId()));
	}
	
	/**
	 * Tests the FormService.getFormFields(Form, Concept) and 
	 * getFormFields(Form,Concept,Collection) methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetFormFieldsByFormAndConcept() throws Exception {
		
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/test/api/include/FormServiceTest-formFields.xml");
		
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
	
}
