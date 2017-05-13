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
package org.openmrs.api.db.hibernate;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	
	private HibernateFormDAO dao = null;
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-initialFieldTypes.xml";
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (HibernateFormDAO) applicationContext.getBean("formDAO");
	}
	
	@Test
	public void getFormCriteria_shouldReturnOneFormIfContainingAnyFormFieldIsEmpty()throws Exception{
		String partialName = null;
		Boolean published = null;
		Boolean retired = null;
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();
		
		List<Form> formsReturned = dao.getForms(partialName, published, encounterTypes, retired, containingAnyFormField,
					containingAllFormFields, fields);

		Assert.assertEquals(1, formsReturned.size());
	}
		
	@Test
	public void getFormCriteria_shouldReturnValuesWithMatchingFormFields()throws Exception{
		String partialName = null;
		Boolean published = null;
		Boolean retired = null;
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();

		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet("org/openmrs/api/include/FormServiceTest-formFields.xml");
		containingAnyFormField.add(new FormField(3));
		
		try {
			List<Form> formsReturned = dao.getForms(partialName, published, encounterTypes, retired, containingAnyFormField,
			    containingAllFormFields, fields);
			ArrayList<Integer> formFieldIds = new ArrayList<Integer>();
			
			if (containingAnyFormField.isEmpty()) {
				Assert.assertEquals(1, formsReturned.size());
			} 
			else {
				for (FormField formField : containingAnyFormField) {
					formFieldIds.add(formField.getId());
				}
				for (Form form : formsReturned) {
					Assert.assertEquals(true, formFieldIds.contains(form.getFormId()));
				}
			}
		}
		catch (DAOException ex) {
			System.out.println(ex.getMessage());
		}
	}
}
