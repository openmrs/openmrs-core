package org.openmrs.api.db.hibernate;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests methods in the {@link HibernateFormDAO} class.
 */
public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	
	private FormService formService;
	
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-initialFieldTypes.xml";
	
	protected static final String FORM_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-formFields.xml";
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeTest() throws Exception {
		formService = Context.getFormService();
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(FORM_FIELDS_XML);
	}
	
	/**
	 * Make sure that all forms are returned if containingAnyFormField is empty
	 * 
	 */
	@Test
	public void getFormCriteria_shouldReturnOneFormIfContainingAnyFormFieldIsEmpty() throws Exception {
		String partialName = null;
		Boolean published = null;
		Boolean retired = null;
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();
		
		List<Form> formsReturned = formService.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Assert.assertEquals(1, formsReturned.size());
		
	}
	
	/**
	 * getFormCriteria() should return all forms that have any one or more matching form fields in the containingAnyFormField parameter collection
	 * 
	 */
	@Test
	public void getFormCriteria_shouldReturnValuesWithMatchingFormFields() throws Exception {
		String partialName = null;
		Boolean published = null;
		Boolean retired = null;
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();
		
		FormField ffield = formService.getFormField(5);
		FormField ffield2 = formService.getFormField(3);
		containingAnyFormField.add(ffield);
		containingAnyFormField.add(ffield2);
		
		List<Form> formsReturned = formService.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		if (containingAnyFormField.isEmpty()) {
			Assert.assertEquals(1, formsReturned.size());
		} else {
			for (Form form : formsReturned) {
				boolean match = false;
				for (FormField ff : containingAnyFormField) {
					if (form.getFormFields().contains(ff))
						match = true;
				}
				Assert.assertEquals(true, match);
			}
		}
	}
}
