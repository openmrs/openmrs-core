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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {

	protected static final String INITIAL_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-initialFieldTypes.xml";

	protected static final String MULTIPLE_FORMS_FORM_FIELDS_XML = "org/openmrs/api/include/FormServiceTest-multipleForms-formFields.xml";
	
	@Autowired
	private HibernateFormDAO dao;
	
	@Test
	public void getFormCriteria_shouldReturnTrueIfFieldsIsNotEmpty() {

		executeDataSet(INITIAL_FIELDS_XML);
		executeDataSet(MULTIPLE_FORMS_FORM_FIELDS_XML);
		
		int formFieldAIdentifier = 2;
		int formFieldBIdentifier = 9;
		
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterTypeA = encounterService.getEncounterType(formFieldAIdentifier);
		EncounterType encounterTypeB = encounterService.getEncounterType(formFieldBIdentifier);

		Collection<EncounterType> encounterTypes = new ArrayList<>();
		encounterTypes.add(encounterTypeA);
		encounterTypes.add(encounterTypeB);

		FormService formService = Context.getFormService();
		FormField formFieldA = formService.getFormField(formFieldAIdentifier);
		FormField formFieldB = formService.getFormField(formFieldBIdentifier);

		Collection<FormField> containingAnyFormField = new ArrayList<>();
		containingAnyFormField.add(formFieldA);
		containingAnyFormField.add(formFieldB);
		
		Collection<FormField> containingAllFormFields = new ArrayList<>();
		containingAllFormFields.add(formFieldA);
		containingAllFormFields.add(formFieldB);
		
		ConceptService conceptService = Context.getConceptService();

		Concept concept = conceptService.getConcept(1);
		String name = "name";
		String description = "description";
		FieldType fieldtype = formService.getAllFieldTypes().get(0);
		String table = "table";
		String attr = "attr";
		Boolean multi = true;
		
		Field field = new Field();
		field.setConcept(concept);
		field.setName(name);
		field.setDescription(description);
		field.setFieldType(fieldtype);
		field.setTableName(table);
		field.setAttributeName(attr);
		field.setSelectMultiple(multi);
		
		Collection<Field> fields = new ArrayList<>();
		fields.add(field);
		
		dao.getFormCriteria("FormName", false, encounterTypes,
			true, containingAnyFormField, containingAllFormFields, fields);
		
		assertTrue(!encounterTypes.isEmpty());
		assertTrue(!containingAnyFormField.isEmpty());
		assertTrue(!containingAllFormFields.isEmpty());
		assertTrue(!fields.isEmpty());
	}
	
}
