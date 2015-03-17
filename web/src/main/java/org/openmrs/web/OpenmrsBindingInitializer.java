/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.text.NumberFormat;
import java.util.Date;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.ConceptAnswerEditor;
import org.openmrs.propertyeditor.ConceptClassEditor;
import org.openmrs.propertyeditor.ConceptDatatypeEditor;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.ConceptMapTypeEditor;
import org.openmrs.propertyeditor.ConceptNameEditor;
import org.openmrs.propertyeditor.ConceptNumericEditor;
import org.openmrs.propertyeditor.ConceptReferenceTermEditor;
import org.openmrs.propertyeditor.ConceptSourceEditor;
import org.openmrs.propertyeditor.DateOrDatetimeEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterEditor;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.propertyeditor.LocationAttributeTypeEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.LocationTagEditor;
import org.openmrs.propertyeditor.OrderEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.propertyeditor.PersonAttributeEditor;
import org.openmrs.propertyeditor.PersonAttributeTypeEditor;
import org.openmrs.propertyeditor.PersonEditor;
import org.openmrs.propertyeditor.PrivilegeEditor;
import org.openmrs.propertyeditor.ProgramEditor;
import org.openmrs.propertyeditor.ProgramWorkflowEditor;
import org.openmrs.propertyeditor.ProgramWorkflowStateEditor;
import org.openmrs.propertyeditor.ProviderEditor;
import org.openmrs.propertyeditor.RoleEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.propertyeditor.VisitEditor;
import org.openmrs.propertyeditor.VisitTypeEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

/**
 * Shared WebBindingInitializer that allows all OpenMRS annotated controllers to use our custom
 * editors.
 */
public class OpenmrsBindingInitializer implements WebBindingInitializer {
	
	/**
	 * @see org.springframework.web.bind.support.WebBindingInitializer#initBinder(org.springframework.web.bind.WebDataBinder,
	 *      org.springframework.web.context.request.WebRequest)
	 */
	@Override
	public void initBinder(WebDataBinder wdb, WebRequest request) {
		wdb.registerCustomEditor(Cohort.class, new CohortEditor());
		wdb.registerCustomEditor(Concept.class, new ConceptEditor());
		wdb.registerCustomEditor(ConceptAnswer.class, new ConceptAnswerEditor());
		wdb.registerCustomEditor(ConceptClass.class, new ConceptClassEditor());
		wdb.registerCustomEditor(ConceptDatatype.class, new ConceptDatatypeEditor());
		wdb.registerCustomEditor(ConceptName.class, new ConceptNameEditor());
		wdb.registerCustomEditor(ConceptNumeric.class, new ConceptNumericEditor());
		wdb.registerCustomEditor(ConceptSource.class, new ConceptSourceEditor());
		wdb.registerCustomEditor(Drug.class, new DrugEditor());
		wdb.registerCustomEditor(Encounter.class, new EncounterEditor());
		wdb.registerCustomEditor(Form.class, new FormEditor());
		wdb.registerCustomEditor(Location.class, new LocationEditor());
		wdb.registerCustomEditor(LocationTag.class, new LocationTagEditor());
		wdb.registerCustomEditor(LocationAttributeType.class, new LocationAttributeTypeEditor());
		wdb.registerCustomEditor(Order.class, new OrderEditor());
		wdb.registerCustomEditor(Patient.class, new PatientEditor());
		wdb.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
		wdb.registerCustomEditor(PersonAttribute.class, new PersonAttributeEditor());
		wdb.registerCustomEditor(PersonAttributeType.class, new PersonAttributeTypeEditor());
		wdb.registerCustomEditor(Person.class, new PersonEditor());
		wdb.registerCustomEditor(Privilege.class, new PrivilegeEditor());
		wdb.registerCustomEditor(Program.class, new ProgramEditor());
		wdb.registerCustomEditor(ProgramWorkflow.class, new ProgramWorkflowEditor());
		wdb.registerCustomEditor(ProgramWorkflowState.class, new ProgramWorkflowStateEditor());
		wdb.registerCustomEditor(Provider.class, new ProviderEditor());
		wdb.registerCustomEditor(Role.class, new RoleEditor());
		wdb.registerCustomEditor(User.class, new UserEditor());
		wdb.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, NumberFormat
		        .getInstance(Context.getLocale()), true));
		wdb.registerCustomEditor(Date.class, new DateOrDatetimeEditor());
		wdb.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
		wdb.registerCustomEditor(ConceptMapType.class, new ConceptMapTypeEditor());
		wdb.registerCustomEditor(ConceptSource.class, new ConceptSourceEditor());
		wdb.registerCustomEditor(ConceptReferenceTerm.class, new ConceptReferenceTermEditor());
		wdb.registerCustomEditor(VisitType.class, new VisitTypeEditor());
		wdb.registerCustomEditor(Visit.class, new VisitEditor());
		
		// can't really do this because PropertyEditors are not told what type of class they are changing :-(
		//wdb.registerCustomEditor(OpenmrsObject.class, new OpenmrsObjectByUuidEditor());
	}
	
}
