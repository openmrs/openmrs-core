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
package org.openmrs.web.controller.person;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Attributable;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * This class controls the generic person properties (address, name, attributes). The Patient and
 * User form controllers extend this class.
 * 
 * @see org.openmrs.web.controller.user.UserFormController
 * @see org.openmrs.web.controller.patient.PatientFormController
 */
public class PersonFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected static final Log log = LogFactory.getLog(PersonFormController.class);
	
	/**
	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
	 * are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true));
		binder.registerCustomEditor(org.openmrs.Concept.class, new ConceptEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                             BindException errors) throws Exception {
		Person person = (Person) obj;
		
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		} else {
			// Make sure they assign a name
			if (person.getPersonName().getGivenName() == "")
				errors.rejectValue("names[0].givenName", "Person.name.required");
			if (person.getPersonName().getFamilyName() == "")
				errors.rejectValue("names[0].familyName", "Person.name.required");
			
			// Make sure they choose a gender
			if (person.getGender() == null || person.getGender().equals(""))
				errors.rejectValue("gender", "Person.gender.required");
			
			// look for person attributes in the request and save to person
			for (PersonAttributeType type : Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PERSON, null)) {
				String paramName = type.getPersonAttributeTypeId().toString();
				String value = request.getParameter(paramName);
				
				// if there is an error displaying the attribute, the value will be null
				if (value != null) {
					PersonAttribute attribute = new PersonAttribute(type, value);
					try {
						Object hydratedObject = attribute.getHydratedObject();
						if (hydratedObject == null || "".equals(hydratedObject.toString())) {
							// if null is returned, the value should be blanked out
							attribute.setValue("");
						} else if (hydratedObject instanceof Attributable) {
							attribute.setValue(((Attributable) hydratedObject).serialize());
						} else if (!hydratedObject.getClass().getName().equals(type.getFormat()))
							// if the classes doesn't match the format, the hydration failed somehow
							// TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
							throw new APIException();
					}
					catch (APIException e) {
						errors.rejectValue("attributes", "Invalid value for " + type.getName() + ": '" + value + "'");
						log
						        .warn("Got an invalid value: " + value + " while setting personAttributeType id #"
						                + paramName, e);
						
						// setting the value to empty so that the user can reset the value to something else
						attribute.setValue("");
						
					}
					person.addAttribute(attribute);
				}
			}
			
			// check patients birthdate against future dates and really old dates
			if (person.getBirthdate() != null) {
				if (person.getBirthdate().after(new Date()))
					errors.rejectValue("birthdate", "error.date.future");
				else {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
					if (person.getBirthdate().before(c.getTime())) {
						errors.rejectValue("birthdate", "error.date.nonsensical");
					}
				}
			}
			
			//	 Patient Info 
			//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "birthdate", "error.null");
			if (person.isPersonVoided())
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
			if (person.isDead() && (person.getCauseOfDeath() == null))
				errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");
			
		}
		
		if (log.isDebugEnabled())
			log.debug("Person Attributes: \n" + person.printAttributes());
		
		return super.processFormSubmission(request, response, person, errors);
	}
	
	/**
	 * Setup the person object. Should be called by the
	 * PersonFormController/UserFormController.formBackingObject(request)
	 * 
	 * @param person
	 * @return
	 */
	protected Person setupFormBackingObject(Person person) {
		
		// set a default name and address for the person.  This allows us to use person.names[0] binding in the jsp
		if (person.getNames().size() < 1)
			person.addName(new PersonName());
		
		if (person.getAddresses().size() < 1)
			person.addAddress(new PersonAddress());
		
		// initialize the user/person sets
		// hibernate seems to have an issue with empty lists/sets if they aren't initialized
		
		person.getAttributes().size();
		
		return person;
	}
	
	/**
	 * Setup the reference map object. Should be called by the
	 * PersonFormController/UserFormController.referenceData(...)
	 * 
	 * @param person
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map setupReferenceData(Map map, Person person) throws Exception {
		
		String causeOfDeathOther = "";
		
		if (Context.isAuthenticated()) {

			//String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			//Concept conceptCause = Context.getConceptService().getConceptByIdOrName(propCause);
			
			/*
			if ( conceptCause != null ) {
				// TODO add back in for persons
				Set<Obs> obssDeath = Context.getObsService().getObservations(person, conceptCause);
				if ( obssDeath.size() == 1 ) {
					Obs obsDeath = obssDeath.iterator().next();
					causeOfDeathOther = obsDeath.getValueText();
					if ( causeOfDeathOther == null ) {
						log.debug("cod is null, so setting to empty string");
						causeOfDeathOther = "";
					} else {
						log.debug("cod is valid: " + causeOfDeathOther);
					}
				} else {
					log.debug("obssDeath is wrong size: " + obssDeath.size());
				}
			} else {
				log.warn("No concept death cause found");
			}
			*/
		}
		
		map.put("causeOfDeathOther", causeOfDeathOther);
		
		return map;
	}
	
	/**
	 * Add the given name, gender, and birthdate/age to the given Person
	 * 
	 * @param <P> Should be a Patient or User object
	 * @param person
	 * @param name
	 * @param gender
	 * @param date birthdate
	 * @param age
	 */
	public static <P extends Person> void getMiniPerson(P person, String name, String gender, String date, String age) {
		
		person.addName(Context.getPersonService().splitPersonName(name));
		
		person.setGender(gender);
		Date birthdate = null;
		boolean birthdateEstimated = false;
		if (date != null && !date.equals("")) {
			try {
				// only a year was passed as parameter
				if (date.length() < 5) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, Integer.valueOf(date));
					c.set(Calendar.MONTH, 0);
					c.set(Calendar.DATE, 1);
					birthdate = c.getTime();
					birthdateEstimated = true;
				}
				// a full birthdate was passed as a parameter
				else {
					birthdate = Context.getDateFormat().parse(date);
					birthdateEstimated = false;
				}
			}
			catch (ParseException e) {
				log.debug("Error getting date from birthdate", e);
			}
		} else if (age != null && !age.equals("")) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			Integer d = c.get(Calendar.YEAR);
			d = d - Integer.parseInt(age);
			try {
				birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + d);
				birthdateEstimated = true;
			}
			catch (ParseException e) {
				log.debug("Error getting date from age", e);
			}
		}
		if (birthdate != null)
			person.setBirthdate(birthdate);
		person.setBirthdateEstimated(birthdateEstimated);
		
	}
	
}
