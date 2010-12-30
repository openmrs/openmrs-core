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
package org.openmrs.web;

import java.text.NumberFormat;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.propertyeditor.PersonEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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
		wdb.registerCustomEditor(Concept.class, new ConceptEditor());
		wdb.registerCustomEditor(Person.class, new PersonEditor());
		wdb.registerCustomEditor(Patient.class, new PatientEditor());
		wdb.registerCustomEditor(Location.class, new LocationEditor());
		wdb.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, NumberFormat
		        .getInstance(Context.getLocale()), true));
		wdb.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
		wdb.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
		// TODO everything else
	}
	
}
