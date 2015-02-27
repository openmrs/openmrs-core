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
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	 */
	@Test
	public void getFormCriteria_shouldReturnThreeFormsIfContainingAnyFormFieldIsEmpty() throws Exception {
		
		List<Form> formsReturned = formService.getForms(null, null, null, null, null, null, null);
		Assert.assertEquals(3, formsReturned.size());
		
	}
	
	/**
	 * @throws Exception
	 * @see HibernateFormDAO#getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)
	 * @verifies return forms containing any form fields in containingAnyFormFields
	 */
	
	@Test
	public void getFormCriteriaWithContainingAnyFormParameter_shouldReturnValuesWithMatchingFormFields() throws Exception {
		
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		
		FormField ffield = formService.getFormField(8);
		FormField ffield2 = formService.getFormField(9);
		containingAnyFormField.add(ffield);
		containingAnyFormField.add(ffield2);
		
		List<Form> formsReturned = formService.getForms(null, null, null, null, containingAnyFormField, null, null);
		
		Assert.assertEquals(2, formsReturned.size());
		
		for (Form form : formsReturned) {
			boolean match = false;
			for (FormField ff : containingAnyFormField) {
				if (form.getFormFields().contains(ff))
					match = true;
			}
			Assert.assertEquals(true, match);
		}
	}
	
	/**
	 * @throws Exception
	 * @see HibernateFormDAO#getForms(String,Boolean,Collection,Boolean,Collection,Collection,Collection)
	 * @verifies return form containing all form fields in containingAllFormFields
	 */
	@Test
	public void getFormCriteriaWithContainingAllFormParameter_shouldReturnValuesWithMatchingFormFields() throws Exception {
		
		Collection<FormField> containingAllFormField = new ArrayList<FormField>();
		//Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		
		FormField ffield = formService.getFormField(8);
		containingAllFormField.add(ffield);
		
		List<Form> formsReturned = formService.getForms(null, null, null, null, null, containingAllFormField, null);
		
		Assert.assertEquals(1, formsReturned.size());
		
		containingAllFormField.add(new FormField(2));
		containingAllFormField.add(new FormField(4));
		
		for (Form form : formsReturned) {
			boolean match = false;
			
			for (FormField ff : form.getFormFields()) {
				if (form.getFormFields().contains(ff)) {
					Assert.assertEquals(form.getFormId(), ff.getForm().getFormId());
					match = true;
				} else {
					match = false;
					break;
				}
			}
			Assert.assertEquals(true, match);
		}
		containingAllFormField.add(new FormField(8));
		formsReturned = formService.getForms(null, null, null, null, null, containingAllFormField, null);
		
		Assert.assertEquals(0, formsReturned.size());
		
	}
}
