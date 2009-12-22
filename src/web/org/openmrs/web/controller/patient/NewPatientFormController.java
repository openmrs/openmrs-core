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
package org.openmrs.web.controller.patient;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Attributable;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.user.UserFormController;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller is used for the "mini"/"new"/"short" patient form. Only key/important attributes
 * for the patient are displayed and allowed to be edited
 * 
 * @see org.openmrs.web.controller.patient.PatientFormController
 */
public class NewPatientFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	// identifiers submitted with the form.  Stored here so that they can
	// be redisplayed for the user after an error
	Set<PatientIdentifier> newIdentifiers = new HashSet<PatientIdentifier>();
	
	String pref = "";
	
	/**
	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
	 * are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
	}
	
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                             BindException errors) throws Exception {
		
		newIdentifiers = new HashSet<PatientIdentifier>();
		
		ShortPatientModel shortPatient = (ShortPatientModel) obj;
		
		log.debug("\nNOW GOING THROUGH PROCESSFORMSUBMISSION METHOD.......................................\n\n");
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			String action = request.getParameter("action");
			if (action == null || action.equals(msa.getMessage("general.save"))) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name.familyName", "error.name");
				
				String[] identifiers = request.getParameterValues("identifier");
				String[] types = request.getParameterValues("identifierType");
				String[] locs = request.getParameterValues("location");
				pref = request.getParameter("preferred");
				if (pref == null)
					pref = "";
				
				if (log.isDebugEnabled()) {
					log.debug("identifiers: " + identifiers);
					for (String s : identifiers)
						log.debug(s);
					log.debug("types: " + types);
					for (String s : types)
						log.debug(s);
					log.debug("locations: " + locs);
					for (String s : locs)
						log.debug(s);
					log.debug("preferred: " + pref);
				}
				
				// loop over the identifiers to create the patient.identifiers set
				if (identifiers != null) {
					for (int i = 0; i < identifiers.length; i++) {
						// arguments for the spring error messages
						String id = identifiers[i].trim();
						String[] args = { id };
						
						// add the new identifier only if they put in some identifier string
						if (id.length() > 0) {
							
							// set up the actual identifier java object
							PatientIdentifierType pit = null;
							if (types[i] == null || types[i].equals("")) {
								String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.identifierType.null",
								    args);
								errors.reject(msg);
							} else
								pit = ps.getPatientIdentifierType(Integer.valueOf(types[i]));
							
							Location loc = null;
							if (locs[i] == null || locs[i].equals("")) {
								String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.location.null", args);
								errors.reject(msg);
							} else
								loc = Context.getLocationService().getLocation(Integer.valueOf(locs[i]));
							
							PatientIdentifier pi = new PatientIdentifier(id, pit, loc);
							pi.setPreferred(pref.equals(id + types[i]));
							if (newIdentifiers.contains(pi))
								newIdentifiers.remove(pi);
							
//							pi.setUuid(null);
							newIdentifiers.add(pi);
							
							if (log.isDebugEnabled()) {
								log.debug("Creating patient identifier with identifier: " + id);
								log.debug("and type: " + types[i]);
								log.debug("and location: " + locs[i]);
							}
							
						}
					}
				}
			}
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "error.null");
			
			if (shortPatient.getBirthdate() == null) {
				Object[] args = { "Birthdate" };
				errors.rejectValue("birthdate", "error.required", args, "");
			} else {
				// check patients birthdate against future dates and really old dates
				if (shortPatient.getBirthdate().after(new Date()))
					errors.rejectValue("birthdate", "error.date.future");
				else {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
					if (shortPatient.getBirthdate().before(c.getTime())) {
						errors.rejectValue("birthdate", "error.date.nonsensical");
					}
				}
			}
			
		}
		
		// skip calling super.processFormSubmission so that setting up the page is done
		// again in the onSubmit method
		
		return onSubmit(request, response, shortPatient, errors);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@SuppressWarnings("unchecked")
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		log.debug("\nNOW GOING THROUGH ONSUBMIT METHOD.......................................\n\n");
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			PersonService personService = Context.getPersonService();
			
			ShortPatientModel shortPatient = (ShortPatientModel) obj;
			String view = getSuccessView();
			boolean isError = errors.hasErrors(); // account for possible errors in the processFormSubmission method
			
			String action = request.getParameter("action");
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (action != null && action.equals(msa.getMessage("general.cancel"))) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
				return new ModelAndView(new RedirectView("addPerson.htm?personType=patient"));
			}
			
			Patient patient = null;
			if (shortPatient.getPatientId() != null) {
				patient = ps.getPatient(shortPatient.getPatientId());
				if (patient == null) {
					try {
						Person p = personService.getPerson(shortPatient.getPatientId());
						Context.clearSession(); // so that this Person doesn't cause hibernate to think the new Patient is in the cache already (only needed until #725 is fixed)
						patient = new Patient(p);
					}
					catch (ObjectRetrievalFailureException noUserEx) {
						// continue;
					}
				}
			}
			
			if (patient == null)
				patient = new Patient();
			
			boolean duplicate = false;
			PersonName newName = shortPatient.getName();
			
			if (log.isDebugEnabled())
				log.debug("Checking new name: " + newName.toString());
			
			for (PersonName pn : patient.getNames()) {
				if (((pn.getGivenName() == null && newName.getGivenName() == null) || OpenmrsUtil.nullSafeEquals(pn
				        .getGivenName(), newName.getGivenName()))
				        && ((pn.getMiddleName() == null && newName.getMiddleName() == null) || OpenmrsUtil.nullSafeEquals(pn
				                .getMiddleName(), newName.getMiddleName()))
				        && ((pn.getFamilyName() == null && newName.getFamilyName() == null) || OpenmrsUtil.nullSafeEquals(pn
				                .getFamilyName(), newName.getFamilyName())))
					duplicate = true;
			}
			
			// if this is a new name, add it to the patient
			if (!duplicate) {
				// set the current name to "non-preferred"
				if (patient.getPersonName() != null)
					patient.getPersonName().setPreferred(false);
				
				// add the new name
				newName.setPersonNameId(null);
				newName.setPreferred(true);
				newName.setUuid(null);
				patient.addName(newName);
			}
			
			if (log.isDebugEnabled())
				log.debug("The address to add/check: " + shortPatient.getAddress());
			
			if (shortPatient.getAddress() != null && !shortPatient.getAddress().isBlank()) {
				duplicate = false;
				for (PersonAddress pa : patient.getAddresses()) {
					if (pa.toString().equals(shortPatient.getAddress().toString())) {
						duplicate = true;
						pa.setPreferred(true);
					} else {
						pa.setPreferred(false);
					}
				}
				
				if (log.isDebugEnabled())
					log.debug("The duplicate address:  " + duplicate);
				
				if (!duplicate) {
					PersonAddress newAddress = (PersonAddress) shortPatient.getAddress().clone();
					newAddress.setPersonAddressId(null);
					newAddress.setPreferred(true);
					newAddress.setUuid(null);
					patient.addAddress(newAddress);
				}
			}
			if (log.isDebugEnabled())
				log.debug("patient addresses: " + patient.getAddresses());
			
			// set or unset the preferred bit for the old identifiers if needed
			if (patient.getIdentifiers() == null)
				patient.setIdentifiers(new TreeSet<PatientIdentifier>());
			
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				pi.setPreferred(pref.equals(pi.getIdentifier() + pi.getIdentifierType().getPatientIdentifierTypeId()));
			}
			
			// look for person attributes in the request and save to patient
			for (PersonAttributeType type : personService.getPersonAttributeTypes(PERSON_TYPE.PATIENT,
			    ATTR_VIEW_TYPE.VIEWING)) {
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
						errors.rejectValue("attributeMap[" + type.getName() + "]", "Invalid value for " + type.getName()
						        + ": '" + value + "'");
						log
						        .warn("Got an invalid value: " + value + " while setting personAttributeType id #"
						                + paramName, e);
						
						// setting the value to empty so that the user can reset the value to something else
						attribute.setValue("");
						
					}
					patient.addAttribute(attribute);
				}
			}
			
			// add the new identifiers.  First remove them so that things like
			// changes to preferred status and location are persisted 
			for (PatientIdentifier identifier : newIdentifiers) {
				// this loop is used instead of just using removeIdentifier because
				// the identifier set on patient is a TreeSet which will use .compareTo
				identifier.setPatient(patient);
				for (PatientIdentifier currentIdentifier : patient.getActiveIdentifiers()) {
					if (currentIdentifier.equals(identifier)) {
						patient.removeIdentifier(currentIdentifier);
						Context.evictFromSession(currentIdentifier);
					}
				}
			}
			patient.addIdentifiers(newIdentifiers);
			
			// find which identifiers they removed and void them
			// must create a new list so that the updated identifiers in
			// the newIdentifiers list are hashed correctly
			List<PatientIdentifier> newIdentifiersList = new Vector<PatientIdentifier>();
			newIdentifiersList.addAll(newIdentifiers);
			for (PatientIdentifier identifier : patient.getIdentifiers()) {
				if (!newIdentifiersList.contains(identifier)) {
					// mark the "removed" identifiers as voided
					identifier.setVoided(true);
					identifier.setVoidReason("Removed from new patient screen");
				}
			}
			
			// set the other patient attributes
			patient.setBirthdate(shortPatient.getBirthdate());
			patient.setBirthdateEstimated(shortPatient.getBirthdateEstimated());
			patient.setGender(shortPatient.getGender());
			
			patient.setDead(shortPatient.getDead());
			if (patient.isDead()) {
				patient.setDeathDate(shortPatient.getDeathDate());
				patient.setCauseOfDeath(shortPatient.getCauseOfDeath());
			} else {
				patient.setDeathDate(null);
				patient.setCauseOfDeath(null);
			}
			
			Patient newPatient = null;
			
			if (!isError) {
				// save or add the patient
				try {
					newPatient = ps.savePatient(patient);
				}
				catch (InvalidIdentifierFormatException iife) {
					log.error(iife);
					patient.removeIdentifier(iife.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
					//errors = new BindException(new InvalidIdentifierFormatException(msa.getMessage("PatientIdentifier.error.formatInvalid")), "givenName");
					isError = true;
				}
				catch (InvalidCheckDigitException icde) {
					log.error(icde);
					patient.removeIdentifier(icde.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.checkDigit");
					//errors = new BindException(new InvalidCheckDigitException(msa.getMessage("PatientIdentifier.error.checkDigit")), "givenName");
					isError = true;
				}
				catch (IdentifierNotUniqueException inue) {
					log.error(inue);
					patient.removeIdentifier(inue.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
					//errors = new BindException(new IdentifierNotUniqueException(msa.getMessage("PatientIdentifier.error.notUnique")), "givenName");
					isError = true;
				}
				catch (DuplicateIdentifierException die) {
					log.error(die);
					patient.removeIdentifier(die.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
					//errors = new BindException(new DuplicateIdentifierException(msa.getMessage("PatientIdentifier.error.duplicate")), "givenName");
					isError = true;
				}
				catch (InsufficientIdentifiersException iie) {
					log.error(iie);
					patient.removeIdentifier(iie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					    "PatientIdentifier.error.insufficientIdentifiers");
					//errors = new BindException(new InsufficientIdentifiersException(msa.getMessage("PatientIdentifier.error.insufficientIdentifiers")), "givenName");
					isError = true;
				}
				catch (PatientIdentifierException pie) {
					log.error(pie);
					patient.removeIdentifier(pie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, pie.getMessage());
					//errors = new BindException(new PatientIdentifierException(msa.getMessage("PatientIdentifier.error.general")), "givenName");
					isError = true;
				}
				
				// update the death reason
				if (patient.getDead()) {
					log.debug("Patient is dead, so let's make sure there's an Obs for it");
					// need to make sure there is an Obs that represents the patient's cause of death, if applicable
					
					String codProp = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
					Concept causeOfDeath = Context.getConceptService().getConcept(codProp);
					
					if (causeOfDeath != null) {
						List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient,
						    causeOfDeath);
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
									obsDeath.setPerson(patient);
									obsDeath.setConcept(causeOfDeath);
									
									// Get default location
									Location loc = Context.getLocationService().getDefaultLocation();
									
									// TODO person healthcenter if ( loc == null ) loc = patient.getHealthCenter();
									if (loc != null)
										obsDeath.setLocation(loc);
									else
										log.error("Could not find a suitable location for which to create this new Obs");
								}
								
								// put the right concept and (maybe) text in this obs
								Concept currCause = patient.getCauseOfDeath();
								if (currCause == null) {
									// set to NONE
									log.debug("Current cause is null, attempting to set to NONE");
									String noneConcept = Context.getAdministrationService()
									        .getGlobalProperty("concept.none");
									currCause = Context.getConceptService().getConcept(noneConcept);
								}
								
								if (currCause != null) {
									log.debug("Current cause is not null, setting to value_coded");
									obsDeath.setValueCoded(currCause);
									obsDeath.setValueCodedName(currCause.getName()); // ABKTODO: presume current locale?
									
									Date dateDeath = patient.getDeathDate();
									if (dateDeath == null)
										dateDeath = new Date();
									obsDeath.setObsDatetime(dateDeath);
									
									// check if this is an "other" concept - if so, then we need to add value_text
									String otherConcept = Context.getAdministrationService().getGlobalProperty(
									    "concept.otherNonCoded");
									Concept conceptOther = Context.getConceptService().getConcept(otherConcept);
									if (conceptOther != null) {
										if (conceptOther.equals(currCause)) {
											// seems like this is an other concept - let's try to get the "other" field info
											String otherInfo = ServletRequestUtils.getStringParameter(request,
											    "causeOfDeath_other", "");
											log.debug("Setting value_text as " + otherInfo);
											obsDeath.setValueText(otherInfo);
										} else {
											log.debug("New concept is NOT the OTHER concept, so setting to blank");
											obsDeath.setValueText("");
										}
									} else {
										log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
										obsDeath.setValueText("");
									}
									
									Context.getObsService().saveObs(obsDeath, null);
								} else {
									log.debug("Current cause is still null - aborting mission");
								}
							}
						}
					} else {
						log
						        .debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
					}
					
				}
				
			}
			
			// save the relationships to the database
			if (!isError && !errors.hasErrors()) {
				Map<String, Relationship> relationships = getRelationshipsMap(patient, request);
				for (Relationship relationship : relationships.values()) {
					// if the user added a person to this relationship, save it 
					if (relationship.getPersonA() != null && relationship.getPersonB() != null)
						personService.saveRelationship(relationship);
				}
			}
			
			// redirect if an error occurred
			if (isError || errors.hasErrors()) {
				log.error("Had an error during processing. Redirecting to " + this.getFormView());
				
				Map<String, Object> model = new HashMap<String, Object>();
				model.put(getCommandName(), new ShortPatientModel(patient));
				
				// evict from session so that nothing temporarily added here is saved
				Context.evictFromSession(patient);
				
				return this.showForm(request, response, errors, model);
				//return new ModelAndView(new RedirectView(getFormView()));
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
				return new ModelAndView(new RedirectView(view + "?patientId=" + newPatient.getPatientId()));
			}
		} else {
			return new ModelAndView(new RedirectView(getFormView()));
		}
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		newIdentifiers = new HashSet<PatientIdentifier>();
		Patient p = null;
		Integer id = null;
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			String patientId = request.getParameter("patientId");
			if (patientId != null) {
				try {
					id = Integer.valueOf(patientId);
					p = ps.getPatient(id);
				}
				catch (NumberFormatException numberError) {
					log.warn("Invalid patientId supplied: '" + patientId + "'", numberError);
				}
				catch (ObjectRetrievalFailureException noUserEx) {
					// continue
				}
			}
			
			if (p == null) {
				try {
					Person person = Context.getPersonService().getPerson(id);
					if (person != null)
						p = new Patient(person);
				}
				catch (ObjectRetrievalFailureException noPersonEx) {
					log.warn("There is no patient or person with id: '" + id + "'", noPersonEx);
					throw new ServletException("There is no patient or person with id: '" + id + "'");
				}
			}
		}
		
		ShortPatientModel patient = new ShortPatientModel(p);
		
		String name = request.getParameter("addName");
		if (p == null && name != null) {
			String gender = request.getParameter("addGender");
			String date = request.getParameter("addBirthdate");
			String age = request.getParameter("addAge");
			
			p = new Patient();
			UserFormController.getMiniPerson(p, name, gender, date, age);
			
			patient = new ShortPatientModel(p);
		}
		
		if (patient.getAddress() == null) {
			PersonAddress pa = new PersonAddress();
			pa.setPreferred(true);
			patient.setAddress(pa);
		}
		
		return patient;
	}
	
	/**
	 * Called prior to form display. Allows for data to be put in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// the list of identifiers to display
		// this is a hashset so that the comparison is one with .equals() instead of .compareTo
		Set<PatientIdentifier> identifiers = new HashSet<PatientIdentifier>();
		
		Patient patient = null;
		String causeOfDeathOther = "";
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			String patientId = request.getParameter("patientId");
			if (patientId != null && !patientId.equals("")) {
				
				// our current patient
				patient = ps.getPatient(Integer.valueOf(patientId));
				
				if (patient != null) {
					// only show non-voided identifiers
					identifiers.addAll(patient.getActiveIdentifiers());
					
					// get 'other' cause of death
					String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
					Concept conceptCause = Context.getConceptService().getConcept(propCause);
					if (conceptCause != null && patient.getPatientId() != null) {
						List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient,
						    conceptCause);
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
						log.debug("No concept cause found");
					}
					// end get 'other' cause of death
				}
			}
			
			// set up the property for the relationships
			
			// {'3a':Relationship#234, '7b':Relationship#9488} 
			Map<String, Relationship> relationships = getRelationshipsMap(patient, request);
			map.put("relationships", relationships);
		}
		
		// give them both the just-entered identifiers and the patient's current identifiers
		for (PatientIdentifier identifier : newIdentifiers) {
			// add the patient object to the new identifier list so
			// that the .equals method works correctly in the next loop
			identifier.setPatient(patient);
		}
		identifiers.addAll(newIdentifiers);
		
		if (pref.length() > 0)
			for (PatientIdentifier pi : identifiers)
				pi.setPreferred(pref.equals(pi.getIdentifier() + pi.getIdentifierType().getPatientIdentifierTypeId()));
		
		if (Context.isAuthenticated())
			map.put("defaultLocation", Context.getAuthenticatedUser().getUserProperty(
			    OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION));
		map.put("identifiers", identifiers);
		map.put("causeOfDeathOther", causeOfDeathOther);
		
		return map;
	}
	
	/**
	 * Convenience method to fetch the relationships to display on the page. First the database is
	 * queried for the user demanded relationships to show on the new patient form. @see
	 * OpenmrsConstants#GLOBAL_PROPERTY_NEWPATIENTFORM_RELATIONSHIPS Each 3a, 6b relationship
	 * defined there is pulled from the db and put into the map. If one doesn't exist in the db yet,
	 * a relationship stub is created. If '3a' or '6b' exist as parameters in the given
	 * <code>request</code>, that parameter value is put into the returned map
	 * 
	 * @param person The person to match against
	 * @param request the current request with or without 3a-named parameters in it
	 * @return Map from relation string to defined relationship object {'3a':obj, '7b':obj}
	 */
	private Map<String, Relationship> getRelationshipsMap(Person person, HttpServletRequest request) {
		Map<String, Relationship> relationshipMap = new LinkedHashMap<String, Relationship>();
		
		// gp is in the form "3a, 7b, 4a"
		String relationshipsString = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_NEWPATIENTFORM_RELATIONSHIPS, "");
		relationshipsString = relationshipsString.trim();
		if (relationshipsString.length() > 0) {
			String[] showRelations = relationshipsString.split(",");
			// iterate over strings like "3a"
			for (String showRelation : showRelations) {
				showRelation = showRelation.trim();
				
				boolean aIsToB = true;
				if (showRelation.endsWith("b")) {
					aIsToB = false;
				}
				
				// trim out the trailing a or b char
				String showRelationId = showRelation.replace("a", "");
				showRelationId = showRelationId.replace("b", "");
				
				RelationshipType relationshipType = Context.getPersonService().getRelationshipType(
				    Integer.valueOf(showRelationId));
				
				// flag to know if we need to create a stub relationship 
				boolean relationshipFound = false;
				
				if (person != null && person.getPersonId() != null) {
					if (aIsToB) {
						List<Relationship> relationships = Context.getPersonService().getRelationships(null, person,
						    relationshipType);
						if (relationships.size() > 0) {
							relationshipMap.put(showRelation, relationships.get(0));
							relationshipFound = true;
						}
					} else {
						List<Relationship> relationships = Context.getPersonService().getRelationships(person, null,
						    relationshipType);
						if (relationships.size() > 0) {
							relationshipMap.put(showRelation, relationships.get(0));
							relationshipFound = true;
						}
					}
				}
				
				// if no relationship was found, create a stub one now
				if (relationshipFound == false) {
					Relationship relationshipStub = new Relationship();
					relationshipStub.setRelationshipType(relationshipType);
					if (aIsToB)
						relationshipStub.setPersonB(person);
					else
						relationshipStub.setPersonA(person);
					
					relationshipMap.put(showRelation, relationshipStub);
				}
				
				// check the request to see if a parameter exists in there
				// that matches to the user desired relation.  Overwrite
				// any previous data if found
				String submittedPersonId = request.getParameter(showRelation);
				if (submittedPersonId != null && submittedPersonId.length() > 0) {
					Person submittedPerson = Context.getPersonService().getPerson(Integer.valueOf(submittedPersonId));
					if (aIsToB)
						relationshipMap.get(showRelation).setPersonA(submittedPerson);
					else
						relationshipMap.get(showRelation).setPersonB(submittedPerson);
				}
				
			}
			
		}
		
		return relationshipMap;
	}
	
}
