/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.person;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Attributable;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.validator.PersonAddressValidator;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class controls the generic person properties (address, name, attributes). The Patient and
 * User form controllers extend this class.
 * 
 * @see org.openmrs.web.controller.patient.PatientFormController
 */
public class PersonFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	private static final Log log = LogFactory.getLog(PersonFormController.class);
	
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
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		Person person = null;
		
		if (Context.isAuthenticated()) {
			PersonService ps = Context.getPersonService();
			String personId = request.getParameter("personId");
			Integer id;
			if (personId != null) {
				try {
					id = Integer.valueOf(personId);
					person = ps.getPerson(id);
					if (person == null) {
						throw new ServletException("There is no person with id: '" + personId + "'");
					}
				}
				catch (NumberFormatException numberError) {
					throw new ServletException("Invalid personId supplied: '" + personId + "'", numberError);
				}
			}
		}
		
		if (person == null) {
			person = new Person();
			
			String name = request.getParameter("addName");
			if (name != null) {
				String gender = request.getParameter("addGender");
				String date = request.getParameter("addBirthdate");
				String age = request.getParameter("addAge");
				
				getMiniPerson(person, name, gender, date, age);
			}
		}
		
		setupFormBackingObject(person);
		
		return person;
	}
	
	/**
	 * Redirects to the patient form if the given personId points to a patient.
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#showForm(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
	        throws Exception {
		Object person = errors.getTarget();
		if (person instanceof Patient) {
			Patient patient = (Patient) person;
			// Redirect if we are not already in the patient form
			if (!getFormView().contains("patient")) {
				return new ModelAndView(new RedirectView("../patients/patient.form?patientId=" + patient.getId()));
			}
		}
		
		return super.showForm(request, response, errors);
	}
	
	/**
	 * Redirects to the patient form if the given personId points to a patient.
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#showForm(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException,
	 *      java.util.Map)
	 */
	@Override
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors,
	        Map controlModel) throws Exception {
		Object person = errors.getTarget();
		if (person instanceof Patient) {
			Patient patient = (Patient) person;
			// Redirect if we are not already in the patient form
			if (!getFormView().contains("patient")) {
				return new ModelAndView(new RedirectView("../patients/patient.form?patientId=" + patient.getId()));
			}
		}
		
		return super.showForm(request, response, errors, controlModel);
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		}
		
		if (errors.hasErrors()) {
			return showForm(request, response, errors);
		}
		
		Person person = (Person) obj;
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		String action = request.getParameter("action");
		
		if (action.equals(msa.getMessage("Person.save"))) {
			updatePersonAddresses(request, person, errors);
			
			updatePersonNames(request, person);
			
			updatePersonAttributes(request, errors, person);
		}
		
		if (errors.hasErrors()) {
			return showForm(request, response, errors);
		}
		
		return super.processFormSubmission(request, response, person, errors);
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	        BindException errors) throws Exception {
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		}
		
		if (errors.hasErrors()) {
			return showForm(request, response, errors);
		}
		
		HttpSession httpSession = request.getSession();
		
		Person person = (Person) command;
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		String action = request.getParameter("action");
		PersonService ps = Context.getPersonService();
		
		StringBuilder linkedProviders = new StringBuilder();
		if (action.equals(msa.getMessage("Person.delete")) || action.equals(msa.getMessage("Person.void"))) {
			Collection<Provider> providerCollection = Context.getProviderService().getProvidersByPerson(person);
			if (providerCollection != null && !providerCollection.isEmpty()) {
				for (Provider provider : providerCollection) {
					linkedProviders.append(provider.getName() + ", ");
				}
				linkedProviders = new StringBuilder(linkedProviders.substring(0, linkedProviders.length() - 2));
			}
		}
		String linkedProvidersString = linkedProviders.toString();
		if (action.equals(msa.getMessage("Person.delete"))) {
			try {
				if (!linkedProvidersString.isEmpty()) {
					errors.reject(Context.getMessageSourceService().getMessage("Person.cannot.delete.linkedTo.providers")
					        + " " + linkedProviders);
				}
				
				Collection<User> userCollection = Context.getUserService().getUsersByPerson(person, true);
				String linkedUsers = "";
				if (userCollection != null && !userCollection.isEmpty()) {
					for (User user : userCollection) {
						linkedUsers = linkedUsers + user.getSystemId() + ", ";
					}
					linkedUsers = linkedUsers.substring(0, linkedUsers.length() - 2);
				}
				if (!linkedUsers.isEmpty()) {
					errors.reject(Context.getMessageSourceService().getMessage("Person.cannot.delete.linkedTo.users") + " "
					        + linkedUsers);
				}
				
				if (errors.hasErrors()) {
					return showForm(request, response, errors);
				} else {
					ps.purgePerson(person);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Person.deleted");
					
					return new ModelAndView(new RedirectView("index.htm"));
				}
			}
			catch (DataIntegrityViolationException e) {
				log.error("Unable to delete person because of database FK errors: " + person, e);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Person.cannot.delete");
				
				return new ModelAndView(new RedirectView(getSuccessView() + "?personId=" + person.getPersonId().toString()));
			}
		} else if (action.equals(msa.getMessage("Person.void"))) {
			String voidReason = request.getParameter("voidReason");
			if (StringUtils.isBlank(voidReason)) {
				voidReason = msa.getMessage("PersonForm.default.voidReason", null, "Voided from person form", Context
				        .getLocale());
			}
			if (linkedProvidersString.isEmpty()) {
				ps.voidPerson(person, voidReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Person.voided");
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
				    "Person.cannot.void.linkedTo.providers")
				        + " " + linkedProviders);
			}
			return new ModelAndView(new RedirectView(getSuccessView() + "?personId=" + person.getPersonId()));
		} else if (action.equals(msa.getMessage("Person.unvoid"))) {
			ps.unvoidPerson(person);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Person.unvoided");
			
			return new ModelAndView(new RedirectView(getSuccessView() + "?personId=" + person.getPersonId()));
		} else {
			ps.savePerson(person);
			
			// If person is dead
			if (person.getDead()) {
				log.debug("Person is dead, so let's make sure there's an Obs for it");
				// need to make sure there is an Obs that represents the patient's cause of death, if applicable
				
				String causeOfDeathConceptId = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
				Concept causeOfDeath = Context.getConceptService().getConcept(causeOfDeathConceptId);
				
				if (causeOfDeath != null) {
					List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(person, causeOfDeath);
					if (obssDeath != null) {
						if (obssDeath.size() > 1) {
							log.error("Multiple causes of death (" + obssDeath.size() + ")?  Shouldn't be...");
						} else {
							Obs obsDeath = null;
							if (obssDeath.size() == 1) {
								// already has a cause of death - let's edit it.
								log.debug("Already has a cause of death, so changing it");
								
								obsDeath = obssDeath.iterator().next();
								
							} else {
								// no cause of death obs yet, so let's make one
								log.debug("No cause of death yet, let's create one.");
								
								obsDeath = new Obs();
								obsDeath.setPerson(person);
								obsDeath.setConcept(causeOfDeath);
								Location location = Context.getLocationService().getDefaultLocation();
								// TODO person healthcenter //if ( loc == null ) loc = patient.getHealthCenter();
								if (location != null) {
									obsDeath.setLocation(location);
								} else {
									log.error("Could not find a suitable location for which to create this new Obs");
								}
							}
							
							// put the right concept and (maybe) text in this obs
							Concept currCause = person.getCauseOfDeath();
							if (currCause == null) {
								// set to NONE
								log.debug("Current cause is null, attempting to set to NONE");
								String noneConcept = Context.getAdministrationService().getGlobalProperty("concept.none");
								currCause = Context.getConceptService().getConcept(noneConcept);
							}
							
							if (currCause != null) {
								log.debug("Current cause is not null, setting to value_coded");
								obsDeath.setValueCoded(currCause);
								obsDeath.setValueCodedName(currCause.getName()); // ABKTODO: presume current locale?
								
								Date dateDeath = person.getDeathDate();
								if (dateDeath == null) {
									dateDeath = new Date();
								}
								
								obsDeath.setObsDatetime(dateDeath);
								
								// check if this is an "other" concept - if so, then we need to add value_text
								String otherConcept = Context.getAdministrationService().getGlobalProperty(
								    "concept.otherNonCoded");
								Concept conceptOther = Context.getConceptService().getConcept(otherConcept);
								boolean deathReasonChanged = false;
								if (conceptOther != null) {
									String otherInfo = ServletRequestUtils.getStringParameter(request, "causeOfDeath_other",
									    "");
									if (conceptOther.equals(currCause)) {
										// seems like this is an other concept - let's try to get the "other" field info
										deathReasonChanged = !otherInfo.equals(obsDeath.getValueText());
										log.debug("Setting value_text as " + otherInfo);
										obsDeath.setValueText(otherInfo);
									} else {
										// non empty text value implies concept changed from OTHER NON CODED to NONE
										deathReasonChanged = !otherInfo.equals("");
										log.debug("New concept is NOT the OTHER concept, so setting to blank");
										obsDeath.setValueText("");
									}
								} else {
									log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
									obsDeath.setValueText("");
								}
								boolean shouldSaveObs = (null == obsDeath.getId()) || deathReasonChanged;
								if (shouldSaveObs) {
									if (null == obsDeath.getVoidReason()) {
										obsDeath.setVoidReason("Changed in patient demographics editor");
									}
									Context.getObsService().saveObs(obsDeath, obsDeath.getVoidReason());
								}
							} else {
								log.debug("Current cause is still null - aborting mission");
							}
						}
					}
				} else {
					log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
				}
				
			}
			
			String view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Person.saved");
			view = view + "?personId=" + person.getPersonId();
			
			return new ModelAndView(new RedirectView(view));
		}
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		Person person = (Person) obj;
		Map<String, Object> map = new HashMap<String, Object>();
		
		// empty objects used to create blank template in the view
		map.put("emptyName", new PersonName());
		map.put("emptyAddress", new PersonAddress());
		
		setupReferenceData(map, person);
		
		return map;
	}
	
	/**
	 * Updates person attributes based on request parameters
	 * 
	 * @param request
	 * @param errors
	 * @param person
	 */
	protected void updatePersonAttributes(HttpServletRequest request, BindException errors, Person person) {
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
					} else if (!hydratedObject.getClass().getName().equals(type.getFormat())) {
						// if the classes doesn't match the format, the hydration failed somehow
						// TODO change the PersonAttribute.getHydratedObject() to not swallow all errors?
						throw new APIException();
					}
				}
				catch (APIException e) {
					errors.rejectValue("attributes", "Invalid value for " + type.getName() + ": '" + value + "'");
					log.warn("Got an invalid value: " + value + " while setting personAttributeType id #" + paramName, e);
					
					// setting the value to empty so that the user can reset the value to something else
					attribute.setValue("");
					
				}
				person.addAttribute(attribute);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Person Attributes: \n" + person.printAttributes());
		}
	}
	
	/**
	 * Updates person names based on request parameters
	 * 
	 * @param request
	 * @param person
	 */
	protected void updatePersonNames(HttpServletRequest request, Person person) {
		Object[] objs = null;
		objs = person.getNames().toArray();
		for (int i = 0; i < objs.length; i++) {
			if (request.getParameter("names[" + i + "].givenName") == null) {
				person.removeName((PersonName) objs[i]);
			}
		}
		
		//String[] prefs = request.getParameterValues("preferred");  (unreliable form info)
		String[] gNames = ServletRequestUtils.getStringParameters(request, "givenName");
		String[] mNames = ServletRequestUtils.getStringParameters(request, "middleName");
		String[] fNamePrefixes = ServletRequestUtils.getStringParameters(request, "familyNamePrefix");
		String[] fNames = ServletRequestUtils.getStringParameters(request, "familyName");
		String[] fName2s = ServletRequestUtils.getStringParameters(request, "familyName2");
		String[] fNameSuffixes = ServletRequestUtils.getStringParameters(request, "familyNameSuffix");
		String[] degrees = ServletRequestUtils.getStringParameters(request, "degree");
		String[] namePrefStatus = ServletRequestUtils.getStringParameters(request, "preferred");
		
		if (gNames != null) {
			for (int i = 0; i < gNames.length; i++) {
				if (!"".equals(gNames[i])) { //skips invalid and blank address data box
					PersonName pn = new PersonName();
					if (namePrefStatus != null && namePrefStatus.length > i) {
						pn.setPreferred(new Boolean(namePrefStatus[i]));
					}
					if (gNames.length >= i + 1) {
						pn.setGivenName(gNames[i]);
					}
					if (mNames.length >= i + 1) {
						pn.setMiddleName(mNames[i]);
					}
					if (fNamePrefixes.length >= i + 1) {
						pn.setFamilyNamePrefix(fNamePrefixes[i]);
					}
					if (fNames.length >= i + 1) {
						pn.setFamilyName(fNames[i]);
					}
					if (fName2s.length >= i + 1) {
						pn.setFamilyName2(fName2s[i]);
					}
					if (fNameSuffixes.length >= i + 1) {
						pn.setFamilyNameSuffix(fNameSuffixes[i]);
					}
					if (degrees.length >= i + 1) {
						pn.setDegree(degrees[i]);
					}
					person.addName(pn);
				}
			}
			Iterator<PersonName> names = person.getNames().iterator();
			PersonName currentName = null;
			PersonName preferredName = null;
			while (names.hasNext()) {
				currentName = names.next();
				if (currentName.isPreferred()) {
					if (preferredName != null) { // if there's a preferred name already exists, make it preferred=false
						preferredName.setPreferred(false);
					}
					preferredName = currentName;
				}
			}
			if ((preferredName == null) && (currentName != null)) { // No preferred name. Make the last name entry as preferred.
				currentName.setPreferred(true);
			}
		}
	}
	
	/**
	 * Updates person addresses based on request parameters
	 * 
	 * @param request
	 * @param person
	 * @param errors
	 * @throws ParseException
	 */
	protected void updatePersonAddresses(HttpServletRequest request, Person person, BindException errors)
	        throws ParseException {
		String[] add1s = ServletRequestUtils.getStringParameters(request, "address1");
		String[] add2s = ServletRequestUtils.getStringParameters(request, "address2");
		String[] cities = ServletRequestUtils.getStringParameters(request, "cityVillage");
		String[] states = ServletRequestUtils.getStringParameters(request, "stateProvince");
		String[] countries = ServletRequestUtils.getStringParameters(request, "country");
		String[] lats = ServletRequestUtils.getStringParameters(request, "latitude");
		String[] longs = ServletRequestUtils.getStringParameters(request, "longitude");
		String[] pCodes = ServletRequestUtils.getStringParameters(request, "postalCode");
		String[] counties = ServletRequestUtils.getStringParameters(request, "countyDistrict");
		String[] add3s = ServletRequestUtils.getStringParameters(request, "address3");
		String[] addPrefStatus = ServletRequestUtils.getStringParameters(request, "preferred");
		String[] add6s = ServletRequestUtils.getStringParameters(request, "address6");
		String[] add5s = ServletRequestUtils.getStringParameters(request, "address5");
		String[] add4s = ServletRequestUtils.getStringParameters(request, "address4");
		String[] startDates = ServletRequestUtils.getStringParameters(request, "startDate");
		String[] endDates = ServletRequestUtils.getStringParameters(request, "endDate");
		
		if (add1s != null || add2s != null || cities != null || states != null || countries != null || lats != null
		        || longs != null || pCodes != null || counties != null || add3s != null || add6s != null || add5s != null
		        || add4s != null || startDates != null || endDates != null) {
			int maxAddrs = 0;
			
			if (add1s != null && add1s.length > maxAddrs) {
				maxAddrs = add1s.length;
			}
			if (add2s != null && add2s.length > maxAddrs) {
				maxAddrs = add2s.length;
			}
			if (cities != null && cities.length > maxAddrs) {
				maxAddrs = cities.length;
			}
			if (states != null && states.length > maxAddrs) {
				maxAddrs = states.length;
			}
			if (countries != null && countries.length > maxAddrs) {
				maxAddrs = countries.length;
			}
			if (lats != null && lats.length > maxAddrs) {
				maxAddrs = lats.length;
			}
			if (longs != null && longs.length > maxAddrs) {
				maxAddrs = longs.length;
			}
			if (pCodes != null && pCodes.length > maxAddrs) {
				maxAddrs = pCodes.length;
			}
			if (counties != null && counties.length > maxAddrs) {
				maxAddrs = counties.length;
			}
			if (add3s != null && add3s.length > maxAddrs) {
				maxAddrs = add3s.length;
			}
			if (add6s != null && add6s.length > maxAddrs) {
				maxAddrs = add6s.length;
			}
			if (add5s != null && add5s.length > maxAddrs) {
				maxAddrs = add5s.length;
			}
			if (add4s != null && add4s.length > maxAddrs) {
				maxAddrs = add4s.length;
			}
			if (startDates != null && startDates.length > maxAddrs) {
				maxAddrs = startDates.length;
			}
			if (endDates != null && endDates.length > maxAddrs) {
				maxAddrs = endDates.length;
			}
			
			log.debug("There appears to be " + maxAddrs + " addresses that need to be saved");
			
			for (int i = 0; i < maxAddrs; i++) {
				PersonAddress pa = new PersonAddress();
				if (add1s.length >= i + 1) {
					pa.setAddress1(add1s[i]);
				}
				if (add2s.length >= i + 1) {
					pa.setAddress2(add2s[i]);
				}
				if (cities.length >= i + 1) {
					pa.setCityVillage(cities[i]);
				}
				if (states.length >= i + 1) {
					pa.setStateProvince(states[i]);
				}
				if (countries.length >= i + 1) {
					pa.setCountry(countries[i]);
				}
				if (lats.length >= i + 1) {
					pa.setLatitude(lats[i]);
				}
				if (longs.length >= i + 1) {
					pa.setLongitude(longs[i]);
				}
				if (pCodes.length >= i + 1) {
					pa.setPostalCode(pCodes[i]);
				}
				if (counties.length >= i + 1) {
					pa.setCountyDistrict(counties[i]);
				}
				if (add3s.length >= i + 1) {
					pa.setAddress3(add3s[i]);
				}
				if (addPrefStatus != null && addPrefStatus.length > i) {
					pa.setPreferred(new Boolean(addPrefStatus[i]));
				}
				if (add6s.length >= i + 1) {
					pa.setAddress6(add6s[i]);
				}
				if (add5s.length >= i + 1) {
					pa.setAddress5(add5s[i]);
				}
				if (add4s.length >= i + 1) {
					pa.setAddress4(add4s[i]);
				}
				if (startDates.length >= i + 1 && StringUtils.isNotBlank(startDates[i])) {
					pa.setStartDate(Context.getDateFormat().parse(startDates[i]));
				}
				if (endDates.length >= i + 1 && StringUtils.isNotBlank(endDates[i])) {
					pa.setEndDate(Context.getDateFormat().parse(endDates[i]));
				}
				
				//check if all required addres fields are filled
				Errors addressErrors = new BindException(pa, "personAddress");
				new PersonAddressValidator().validate(pa, addressErrors);
				if (addressErrors.hasErrors()) {
					for (ObjectError error : addressErrors.getAllErrors()) {
						errors.reject(error.getCode(), error.getArguments(), "");
					}
				}
				if (errors.hasErrors()) {
					return;
				}
				
				person.addAddress(pa);
			}
			Iterator<PersonAddress> addresses = person.getAddresses().iterator();
			PersonAddress currentAddress = null;
			PersonAddress preferredAddress = null;
			while (addresses.hasNext()) {
				currentAddress = addresses.next();
				
				//check if all required addres fields are filled
				Errors addressErrors = new BindException(currentAddress, "personAddress");
				new PersonAddressValidator().validate(currentAddress, addressErrors);
				if (addressErrors.hasErrors()) {
					for (ObjectError error : addressErrors.getAllErrors()) {
						errors.reject(error.getCode(), error.getArguments(), "");
					}
				}
				if (errors.hasErrors()) {
					return;
				}
				
				if (currentAddress.isPreferred()) {
					if (preferredAddress != null) { // if there's a preferred address already exists, make it preferred=false
						preferredAddress.setPreferred(false);
					}
					preferredAddress = currentAddress;
				}
			}
			if ((preferredAddress == null) && (currentAddress != null)) { // No preferred address. Make the last address entry as preferred.
				currentAddress.setPreferred(true);
			}
		}
	}
	
	/**
	 * Setup the person object. Should be called by the
	 * PersonFormController.formBackingObject(request)
	 * 
	 * @param person
	 * @return
	 */
	protected Person setupFormBackingObject(Person person) {
		
		// set a default name and address for the person.  This allows us to use person.names[0] binding in the jsp
		if (person.getNames().size() < 1) {
			person.addName(new PersonName());
		}
		
		if (person.getAddresses().size() < 1) {
			person.addAddress(new PersonAddress());
		}
		
		// initialize the user/person sets
		// hibernate seems to have an issue with empty lists/sets if they aren't initialized
		
		person.getAttributes().size();
		
		return person;
	}
	
	/**
	 * Setup the reference map object. Should be called by the
	 * PersonFormController.referenceData(...)
	 * 
	 * @param person
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map setupReferenceData(Map map, Person person) throws Exception {
		
		String causeOfDeathOther = "";
		
		if (Context.isAuthenticated()) {
			
			String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept conceptCause = Context.getConceptService().getConcept(propCause);
			
			if (conceptCause != null) {
				// TODO add back in for persons
				List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(person, conceptCause);
				if (obssDeath.size() == 1) {
					Obs obsDeath = obssDeath.iterator().next();
					causeOfDeathOther = obsDeath.getValueText();
					if (causeOfDeathOther == null) {
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
		
		person.addName(Context.getPersonService().parsePersonName(name));
		
		person.setGender(gender);
		Date birthdate = null;
		boolean birthdateEstimated = false;
		if (StringUtils.isNotEmpty(date)) {
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
		} else if (age != null && !"".equals(age)) {
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
		if (birthdate != null) {
			person.setBirthdate(birthdate);
		}
		person.setBirthdateEstimated(birthdateEstimated);
		
	}
	
}
