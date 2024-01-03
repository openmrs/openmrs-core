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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
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
}
