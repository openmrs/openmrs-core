/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Concept;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	
	private static final String FORM_XML = "org/openmrs/api/db/hibernate/include/HibernateFormDAOTestDataSet.xml";
	
	@Autowired
	private HibernateFormDAO dao;
	
	@BeforeEach
	public void setUp() {
		executeDataSet(FORM_XML);
	}
		
	@Test
	public void shouldFilterAgainstFormFields() {
		List<FormField> formFields = Arrays.asList(new FormField(2), new FormField(3));
		assertEquals(1, (Object)dao.getForms(null, false, Collections.emptyList(), null, formFields, formFields, Collections.emptyList()).size());
		
		formFields = Arrays.asList(new FormField(2), new FormField(3), new FormField(5));
		assertEquals(0, (Object)dao.getForms(null, false, Collections.emptyList(), null, formFields, formFields, Arrays.asList(new Field(3))).size());
		
	}

	@Test
	public void shouldGetFormFieldsByForm() {
		Form form = new Form(2); 
		List<FormField> formFields = dao.getFormFields(form);

		assertNotNull(formFields);

		final int EXPECTED_SIZE = 2;
		assertEquals(EXPECTED_SIZE, formFields.size());
		for (FormField formField : formFields) {
			assertEquals(form.getFormId(), formField.getForm().getFormId());
		}
	}

	/**
	 * @see HibernateFormDAO#getFields(String)
	 */
	
	@Test
	public void getFields_shouldReturnCorrectFieldsByName() {
		List<Field> actualFields = dao.getFields("Some same concept");
		assertNotNull(actualFields);
		assertEquals(2, actualFields.size());
	}

	/**
	 * @see HibernateFormDAO#getFieldsByConcept(Concept)
	 */
	@Test
	public void getFieldsByConcept_shouldReturnCorrectFieldsByConcept() {
		Concept concept = new Concept(3);
		List<Field> actualFields = dao.getFieldsByConcept(concept);
		
		assertNotNull(actualFields);
		assertEquals(2, actualFields.size());
	}

	/**
	 * @see HibernateFormDAO#getAllFields(boolean)
	 */
	@Test
	public void getAllFields_shouldReturnCorrectFieldsByRetiredStatus() {
		List<Field> allFields = dao.getAllFields(true);
		assertNotNull(allFields);
		assertEquals(3, allFields.size()); 
		
		List<Field> nonRetiredFields = dao.getAllFields(false);
		assertNotNull(nonRetiredFields);
		assertEquals(3, nonRetiredFields.size());
		for (Field field : nonRetiredFields) {
			assertFalse(field.getRetired());
		}
	}

	/**
	 * @see HibernateFormDAO#getFormField(Form, Concept, Collection<FormField>, boolean)
	 */
	@Test
	public void getFormField_shouldReturnCorrectFormFieldBasedOnParameters() {
		Form form = new Form(1);
		Concept concept = new Concept(4);
		
		FormField result = dao.getFormField(form, concept, Collections.emptyList(), false);
		assertNotNull(result);
		assertEquals(1, result.getForm().getFormId());
		assertEquals(4, result.getField().getConcept().getConceptId());
		
		FormField nullResult = dao.getFormField(null, concept, Collections.emptyList(), false);
		assertNull(nullResult);
		
		Concept nonExistentConcept = new Concept(999);
		FormField noMatchResult = dao.getFormField(form, nonExistentConcept, Collections.emptyList(), false);
		assertNull(noMatchResult);
		
		FormField formField2 = dao.getFormField(2);
		FormField formField3 = dao.getFormField(3);
		List<FormField> ignoreAll = Arrays.asList(formField2, formField3);
		
		FormField ignoreResult = dao.getFormField(form, concept, ignoreAll, false);
		assertNotNull(ignoreResult);
		assertEquals(2,ignoreResult.getFormFieldId());
	
		assertNull(dao.getFormField(form, concept, ignoreAll, true));
	}

	/**
	 * @see HibernateFormDAO#getAllForms(boolean)
	 */
	@Test
	public void getAllForms_shouldReturnCorrectFormsByRetiredStatus() {
		List<Form> allForms = dao.getAllForms(true);
		assertNotNull(allForms);
		assertEquals(4, allForms.size()); 
		
		List<Form> nonRetiredForms = dao.getAllForms(false);
		assertNotNull(nonRetiredForms);
		assertEquals(3, nonRetiredForms.size()); 
		
		for (Form form : nonRetiredForms) {
			assertFalse(form.getRetired());
		}
	}
}
