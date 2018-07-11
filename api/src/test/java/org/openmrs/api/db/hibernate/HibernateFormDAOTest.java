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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/db/hibernate/include/HibernateFormDAOTest-initialFieldTypes.xml";
	
	protected static final String FORM_XML = "org/openmrs/api/db/hibernate/include/HibernateFormDAOTest-formDataset.xml";
	
	@Autowired
	HibernateFormDAO hibernateFormDAO;
	
	@Before
	public void setUp() {
		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(FORM_XML);
	}
	
	@Test
	public void shouldReturnAllFormsWhenGivenNoCriteria() {
		FormService formService = Context.getFormService();
		
		String partialName = "";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(2));
		expectedForms.add(formService.getForm(3));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(3), formCountActual);
	}
	
	@Test
	public void shouldReturn1FormWhenGivenPublishedIsFalse() {
		FormService formService = Context.getFormService();
		
		String partialName = "";
		Boolean published = Boolean.FALSE;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(2));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(1), formCountActual);
	}
	
	@Test
	public void shouldReturn2FormsWhenGivenPublishedIsTrue() {
		FormService formService = Context.getFormService();
		
		String partialName = "";
		Boolean published = Boolean.TRUE;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(3));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturn2FormsWhenGivenAnEncounterTypeThatHasMatches() {
		FormService formService = Context.getFormService();
		EncounterService encounterService = Context.getEncounterService();
		
		// Not sure how to assign these in the xml, so we do it here using the services
		formService.getForm(1).setEncounterType(encounterService.getEncounterType(2));
		formService.getForm(2).setEncounterType(encounterService.getEncounterType(2));
		
		String partialName = "";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		encounterTypes.add(encounterService.getEncounterType(2));
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(2));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturnNoFormsWhenGivenAnEncounterTypeThatDoesNotHaveMatches() {
		Context.getFormService();
		EncounterService encounterService = Context.getEncounterService();
		
		String partialName = "";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		encounterTypes.add(encounterService.getEncounterType(3));
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(0), formCountActual);
	}
	
	@Test
	public void shouldReturn1FormWhenGivenRetiredIsFalse() {
		FormService formService = Context.getFormService();
		
		String partialName = "";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = Boolean.FALSE;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(1), formCountActual);
	}
	
	@Test
	public void shouldReturn2FormsWhenGivenRetiredIsTrue() {
		FormService formService = Context.getFormService();
		
		String partialName = "";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = Boolean.TRUE;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(2));
		expectedForms.add(formService.getForm(3));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturnTwoFormsWhenGivenAPartialNameThatHasMatches() {
		FormService formService = Context.getFormService();
		
		String partialName = "Basic Form";
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(2));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturnNoFormsWhenGivenAPartialNameThatDoesNotHaveMatches() {
		
		String partialName = "example form";
		Boolean published = Boolean.FALSE;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = Boolean.FALSE;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(0), formCountActual);
	}
	
	@Test
	public void shouldReturnTwoFormsWhenMatchingOnAnyFormFieldWithMatches() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		containingAnyFormField.add(formService.getFormField(3));
		containingAnyFormField.add(formService.getFormField(8));
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(2));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturnOneFormWhenMatchingOnAnyFormFieldWithOnlyOneMatch() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		containingAnyFormField.add(formService.getFormField(9));
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(3));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(1), formCountActual);
	}
	
	@Test
	public void shouldReturnOneFormWhenMatchingOnAllFormFieldsWithASingleMatch() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		containingAllFormFields.add(formService.getFormField(8));
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(2));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(1), formCountActual);
	}
	
	@Test
	public void shouldReturnZeroFormsWhenMatchingOnAllFormFieldsWithNoMatch() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		containingAllFormFields.add(formService.getFormField(3));
		containingAllFormFields.add(formService.getFormField(8));
		Collection<Field> fields = new HashSet<Field>();
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(0), formCountActual);
	}
	
	@Test
	public void shouldReturnZeroFormsWhenMatchingOnNoFields() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		fields.add(formService.getField(4));
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(0), formCountActual);
	}
	
	@Test
	public void shouldReturnTwoFormsWhenMatchingOnFields() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		fields.add(formService.getField(5));
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(2));
		expectedForms.add(formService.getForm(3));
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(2), formCountActual);
	}
	
	@Test
	public void shouldReturnTheSameFormMultipleTimesWhenMatchingOnRepeatedFields() {
		FormService formService = Context.getFormService();
		
		String partialName = null;
		Boolean published = null;
		Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
		Boolean retired = null;
		Collection<FormField> containingAnyFormField = new HashSet<FormField>();
		Collection<FormField> containingAllFormFields = new HashSet<FormField>();
		Collection<Field> fields = new HashSet<Field>();
		fields.add(formService.getField(1));
		
		List<Form> formsActual = hibernateFormDAO.getForms(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		Integer formCountActual = hibernateFormDAO.getFormCount(partialName, published, encounterTypes, retired,
		    containingAnyFormField, containingAllFormFields, fields);
		
		ArrayList<Form> expectedForms = new ArrayList<Form>();
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(1));
		expectedForms.add(formService.getForm(1));
		
		Assert.assertEquals(expectedForms, formsActual);
		Assert.assertEquals(Integer.valueOf(3), formCountActual);
	}
	
}
