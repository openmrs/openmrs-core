/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.LocationBehavior;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocationUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.validator.PatientValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.person.PersonFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 * This controller is used for the "mini"/"new"/"short" patient form. Only key/important attributes
 * for the patient are displayed and allowed to be edited
 * 
 * @see org.openmrs.web.controller.patient.PatientFormController
 */

@Controller
public class ShortPatientFormController {
	
	private static final Log log = LogFactory.getLog(ShortPatientFormController.class);
	
	private static final String SHORT_PATIENT_FORM_URL = "/admin/patients/shortPatientForm";
	
	private static final String FIND_PATIENT_PAGE = "findPatient";
	
	private static final String PATIENT_DASHBOARD_URL = "/patientDashboard.form";
	
	@Autowired
	PatientValidator patientValidator;
	
	/**
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = SHORT_PATIENT_FORM_URL)
	public void showForm() {
	}
	
	@ModelAttribute("patientModel")
	public ShortPatientModel getPatientModel(@RequestParam(value = "patientId", required = false) Integer patientId,
	        ModelMap model, WebRequest request) {
		Patient patient;
		if (patientId != null) {
			patient = Context.getPatientService().getPatientOrPromotePerson(patientId);
			if (patient == null) {
				throw new IllegalArgumentException("No patient or person with the given id");
			}
		} else {
			// we may have some details to add to a blank patient
			patient = new Patient();
			String name = request.getParameter("addName");
			if (!StringUtils.isBlank(name)) {
				String gender = request.getParameter("addGender");
				String date = request.getParameter("addBirthdate");
				String age = request.getParameter("addAge");
				PersonFormController.getMiniPerson(patient, name, gender, date, age);
			}
		}
		
		// if we have an existing personName, cache the original name so that we
		// can use it to
		// track changes in givenName, middleName, familyName, will also use
		// it to restore the original values
		if (patient.getPersonName() != null && patient.getPersonName().getId() != null) {
			model.addAttribute("personNameCache", PersonName.newInstance(patient.getPersonName()));
		} else {
			model.addAttribute("personNameCache", new PersonName());
		}
		
		// cache a copy of the person address for comparison in case the name is
		// edited
		if (patient.getPersonAddress() != null && patient.getPersonAddress().getId() != null) {
			model.addAttribute("personAddressCache", patient.getPersonAddress().clone());
		} else {
			model.addAttribute("personAddressCache", new PersonAddress());
		}
		
		String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
		Concept conceptCause = Context.getConceptService().getConcept(propCause);
		String causeOfDeathOther = "";
		if (conceptCause != null && patient.getPatientId() != null) {
			List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient, conceptCause);
			
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
		model.addAttribute("causeOfDeathOther", causeOfDeathOther);
		
		return new ShortPatientModel(patient);
	}
	
	@ModelAttribute("locations")
	public List<Location> getLocations() {
		return Context.getLocationService().getAllLocations();
	}
	
	@ModelAttribute("defaultLocation")
	public Location getDefaultLocation() {
		return (LocationUtility.getUserDefaultLocation() != null) ? LocationUtility.getUserDefaultLocation()
		        : LocationUtility.getDefaultLocation();
	}
	
	@ModelAttribute("identifierTypes")
	public List<PatientIdentifierType> getIdentifierTypes() {
		final List<PatientIdentifierType> list = Context.getPatientService().getAllPatientIdentifierTypes();
		return list;
	}
	
	@ModelAttribute("identifierLocationUsed")
	public boolean getIdentifierLocationUsed() {
		List<PatientIdentifierType> pits = Context.getPatientService().getAllPatientIdentifierTypes();
		boolean identifierLocationUsed = false;
		for (PatientIdentifierType pit : pits) {
			if (pit.getLocationBehavior() == null || pit.getLocationBehavior() == LocationBehavior.REQUIRED) {
				identifierLocationUsed = true;
			}
		}
		return identifierLocationUsed;
	}
	
	/**
	 * Handles the form submission by validating the form fields and saving it to the DB
	 * 
	 * @param request the webRequest object
	 * @param relationshipsMap
	 * @param patientModel the modelObject containing the patient info collected from the form
	 *            fields
	 * @param result
	 * @param status
	 * @return the view to forward to
	 * @should pass if all the form data is valid
	 * @should create a new patient
	 * @should send the user back to the form in case of validation errors
	 * @should void a name and replace it with a new one if it is changed to a unique value
	 * @should void an address and replace it with a new one if it is changed to a unique value
	 * @should add a new name if the person had no names
	 * @should add a new address if the person had none
	 * @should ignore a new address that was added and voided at same time
	 * @should set the cause of death as none a coded concept
	 * @should set the cause of death as a none coded concept
	 * @should void the cause of death obs that is none coded
	 * @should add a new person attribute with a non empty value
	 * @should not add a new person attribute with an empty value
	 * @should void an existing person attribute with an empty value
	 * @should should replace an existing attribute with a new one when edited
	 * @should not void address if it was not changed
	 * @should void address if it was changed
	 */
	@RequestMapping(method = RequestMethod.POST, value = SHORT_PATIENT_FORM_URL)
	public String saveShortPatient(WebRequest request, @ModelAttribute("personNameCache") PersonName personNameCache,
	        @ModelAttribute("personAddressCache") PersonAddress personAddressCache,
	        @ModelAttribute("relationshipsMap") Map<String, Relationship> relationshipsMap,
	        @ModelAttribute("patientModel") ShortPatientModel patientModel, BindingResult result) {
		
		if (Context.isAuthenticated()) {
			// First do form validation so that we can easily bind errors to
			// fields
			new ShortPatientFormValidator().validate(patientModel, result);
			if (result.hasErrors()) {
				return SHORT_PATIENT_FORM_URL;
			}
			
			Patient patient = null;
			patient = getPatientFromFormData(patientModel);
			
			Errors patientErrors = new BindException(patient, "patient");
			patientValidator.validate(patient, patientErrors);
			if (patientErrors.hasErrors()) {
				// bind the errors to the patientModel object by adding them to
				// result since this is not a patient object
				// so that spring doesn't try to look for getters/setters for
				// Patient in ShortPatientModel
				for (ObjectError error : patientErrors.getAllErrors()) {
					result.reject(error.getCode(), error.getArguments(), "Validation errors found");
				}
				
				return SHORT_PATIENT_FORM_URL;
			}
			
			// check if name/address were edited, void them and replace them
			boolean foundChanges = hasPersonNameOrAddressChanged(patient, personNameCache, personAddressCache);
			
			try {
				patient = Context.getPatientService().savePatient(patient);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
				    "Patient.saved"), WebRequest.SCOPE_SESSION);
				
				// TODO do we really still need this, besides ensuring that the
				// cause of death is provided?
				// process and save the death info
				saveDeathInfo(patientModel, request);
				
				if (!patient.getVoided() && relationshipsMap != null) {
					for (Relationship relationship : relationshipsMap.values()) {
						// if the user added a person to this relationship, save
						// it
						if (relationship.getPersonA() != null && relationship.getPersonB() != null) {
							Context.getPersonService().saveRelationship(relationship);
						}
					}
				}
			}
			catch (APIException e) {
				log.error("Error occurred while attempting to save patient", e);
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
				    "Patient.save.error"), WebRequest.SCOPE_SESSION);
				// TODO revert the changes and send them back to the form
				
				// don't send the user back to the form because the created
				// person name/addresses
				// will be recreated over again if the user attempts to resubmit
				if (!foundChanges) {
					return SHORT_PATIENT_FORM_URL;
				}
			}
			
			return "redirect:" + PATIENT_DASHBOARD_URL + "?patientId=" + patient.getPatientId();
			
		}
		
		return FIND_PATIENT_PAGE;
	}
	
	/**
	 * Convenience method that gets the data from the patientModel
	 * 
	 * @param patientModel the modelObject holding the form data
	 * @return the patient object that has been populated with input from the form
	 */
	private Patient getPatientFromFormData(ShortPatientModel patientModel) {
		
		Patient patient = patientModel.getPatient();
		PersonName personName = patientModel.getPersonName();
		if (personName != null) {
			personName.setPreferred(true);
			patient.addName(personName);
		}
		
		PersonAddress personAddress = patientModel.getPersonAddress();
		
		if (personAddress != null) {
			if (personAddress.isVoided() && StringUtils.isBlank(personAddress.getVoidReason())) {
				personAddress.setVoidReason(Context.getMessageSourceService().getMessage("general.default.voidReason"));
			}
			// don't add an address that is being created and at the
			// same time being removed
			else if (!(personAddress.isVoided() && personAddress.getPersonAddressId() == null)) {
				personAddress.setPreferred(true);
				patient.addAddress(personAddress);
			}
		}
		
		// add all the existing identifiers and any new ones.
		if (patientModel.getIdentifiers() != null) {
			for (PatientIdentifier id : patientModel.getIdentifiers()) {
				// skip past the new ones removed from the user interface(may be
				// they were invalid
				// and the user changed their mind about adding them and they
				// removed them)
				if (id.getPatientIdentifierId() == null && id.isVoided()) {
					continue;
				}
				
				patient.addIdentifier(id);
			}
		}
		
		// add the person attributes
		if (patientModel.getPersonAttributes() != null) {
			for (PersonAttribute formAttribute : patientModel.getPersonAttributes()) {
				//skip past new attributes with no values, because the user left them blank
				if (formAttribute.getPersonAttributeId() == null && StringUtils.isBlank(formAttribute.getValue())) {
					continue;
				}
				
				//if the value has been changed for an existing attribute, void it and create a new one
				if (formAttribute.getPersonAttributeId() != null
				        && !OpenmrsUtil.nullSafeEquals(formAttribute.getValue(), patient.getAttribute(
				            formAttribute.getAttributeType()).getValue())) {
					//As per the logic in Person.addAttribute, the old edited attribute will get voided 
					//as this new one is getting added 
					formAttribute = new PersonAttribute(formAttribute.getAttributeType(), formAttribute.getValue());
					//AOP is failing to set these in unit tests, just set them here for the tests to pass
					formAttribute.setDateCreated(new Date());
					formAttribute.setCreator(Context.getAuthenticatedUser());
				}
				
				patient.addAttribute(formAttribute);
			}
		}
		
		return patient;
	}
	
	/**
	 * Creates a map of string of the form 3b, 3a and the actual person Relationships
	 * 
	 * @param result
	 * @param person the patient/person whose relationships to return
	 * @param request the webRequest Object
	 * @return map of strings matched against actual relationships
	 */
	@ModelAttribute("relationshipsMap")
	private Map<String, Relationship> getRelationshipsMap(
	        @RequestParam(value = "patientId", required = false) Integer patientId, WebRequest request) {
		Map<String, Relationship> relationshipMap = new LinkedHashMap<String, Relationship>();
		
		if (patientId == null) {
			return relationshipMap;
		}
		
		Person person = Context.getPersonService().getPerson(patientId);
		if (person == null) {
			throw new IllegalArgumentException("Patient does not exist: " + patientId);
		}
		
		// Check if relationships must be shown
		String showRelationships = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_NEWPATIENTFORM_SHOW_RELATIONSHIPS, "false");
		
		if ("false".equals(showRelationships)) {
			return relationshipMap;
		}
		
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
				
				if (person.getPersonId() != null) {
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
				if (!relationshipFound) {
					Relationship relationshipStub = new Relationship();
					relationshipStub.setRelationshipType(relationshipType);
					if (aIsToB) {
						relationshipStub.setPersonB(person);
					} else {
						relationshipStub.setPersonA(person);
					}
					
					relationshipMap.put(showRelation, relationshipStub);
				}
				
				// check the request to see if a parameter exists in there
				// that matches to the user desired relation. Overwrite
				// any previous data if found
				String submittedPersonId = request.getParameter(showRelation);
				if (submittedPersonId != null && submittedPersonId.length() > 0) {
					Person submittedPerson = Context.getPersonService().getPerson(Integer.valueOf(submittedPersonId));
					if (aIsToB) {
						relationshipMap.get(showRelation).setPersonA(submittedPerson);
					} else {
						relationshipMap.get(showRelation).setPersonB(submittedPerson);
					}
				}
			}
		}
		
		return relationshipMap;
	}
	
	/**
	 * Processes the death information for a deceased patient and save it to the database
	 * 
	 * @param patientModel the modelObject containing the patient info collected from the form
	 *            fields
	 * @param request webRequest object
	 */
	private void saveDeathInfo(ShortPatientModel patientModel, WebRequest request) {
		// update the death reason
		if (patientModel.getPatient().getDead()) {
			log.debug("Patient is dead, so let's make sure there's an Obs for it");
			// need to make sure there is an Obs that represents the
			// patient's cause of death, if applicable
			
			String codProp = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept causeOfDeath = Context.getConceptService().getConcept(codProp);
			
			if (causeOfDeath != null) {
				List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patientModel.getPatient(),
				    causeOfDeath);
				if (obssDeath != null) {
					if (obssDeath.size() > 1) {
						log.warn("Multiple causes of death (" + obssDeath.size() + ")?  Shouldn't be...");
					} else {
						Obs obsDeath = null;
						if (obssDeath.size() == 1) {
							// already has a cause of death - let's edit
							// it.
							log.debug("Already has a cause of death, so changing it");
							
							obsDeath = obssDeath.iterator().next();
							
						} else {
							// no cause of death obs yet, so let's make
							// one
							log.debug("No cause of death yet, let's create one.");
							
							obsDeath = new Obs();
							obsDeath.setPerson(patientModel.getPatient());
							obsDeath.setConcept(causeOfDeath);
						}
						
						// put the right concept and (maybe) text in this obs
						Concept currCause = patientModel.getPatient().getCauseOfDeath();
						if (currCause == null) {
							// set to NONE
							log.debug("Current cause is null, attempting to set to NONE");
							String noneConcept = Context.getAdministrationService().getGlobalProperty("concept.none");
							currCause = Context.getConceptService().getConcept(noneConcept);
						}
						
						if (currCause != null) {
							log.debug("Current cause is not null, setting to value_coded");
							obsDeath.setValueCoded(currCause);
							obsDeath.setValueCodedName(currCause.getName());
							
							Date dateDeath = patientModel.getPatient().getDeathDate();
							if (dateDeath == null) {
								dateDeath = new Date();
							}
							obsDeath.setObsDatetime(dateDeath);
							
							// check if this is an "other" concept - if
							// so, then we need to add value_text
							String otherConcept = Context.getAdministrationService().getGlobalProperty(
							    "concept.otherNonCoded");
							Concept conceptOther = Context.getConceptService().getConcept(otherConcept);
							if (conceptOther != null) {
								if (conceptOther.equals(currCause)) {
									// seems like this is an other
									// concept - let's try to get the
									// "other" field info
									String otherInfo = request.getParameter("patient.causeOfDeath_other");
									if (otherInfo == null) {
										otherInfo = "";
									}
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
							
							if (StringUtils.isBlank(obsDeath.getVoidReason())) {
								obsDeath.setVoidReason(Context.getMessageSourceService().getMessage(
								    "general.default.changeReason"));
							}
							Context.getObsService().saveObs(obsDeath, obsDeath.getVoidReason());
						} else {
							log.debug("Current cause is still null - aborting mission");
						}
					}
				}
			} else {
				log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
			}
		}
		
	}
	
	/**
	 * Convenience method that checks if the person name or person address have been changed, should
	 * void the old person name/address and create a new one with the changes.
	 * 
	 * @param patient the patient
	 * @param personNameCache the cached copy of the person name
	 * @param personAddressCache the cached copy of the person address
	 * @return true if the personName or personAddress was edited otherwise false
	 */
	private boolean hasPersonNameOrAddressChanged(Patient patient, PersonName personNameCache,
	        PersonAddress personAddressCache) {
		boolean foundChanges = false;
		PersonName personName = patient.getPersonName();
		if (personNameCache.getId() != null) {
			// if the existing persoName has been edited
			if (!getPersonNameString(personName).equalsIgnoreCase(getPersonNameString(personNameCache))) {
				if (log.isDebugEnabled()) {
					log.debug("Voiding person name with id: " + personName.getId() + " and replacing it with a new one: "
					        + personName.getFullName());
				}
				foundChanges = true;
				// create a new one and copy the changes to it
				PersonName newName = PersonName.newInstance(personName);
				newName.setPersonNameId(null);
				newName.setUuid(null);
				newName.setChangedBy(null);// just in case it had a value
				newName.setDateChanged(null);
				newName.setCreator(Context.getAuthenticatedUser());
				newName.setDateCreated(new Date());
				
				// restore the given,middle and familyName, then void the old
				// name
				personName.setGivenName(personNameCache.getGivenName());
				personName.setMiddleName(personNameCache.getMiddleName());
				personName.setFamilyName(personNameCache.getFamilyName());
				personName.setPreferred(false);
				personName.setVoided(true);
				personName.setVoidReason(Context.getMessageSourceService().getMessage("general.voidReasonWithArgument",
				    new Object[] { newName.getFullName() }, "Voided because it was edited to: " + newName.getFullName(),
				    Context.getLocale()));
				
				// add the created name
				patient.addName(newName);
			}
		}
		
		PersonAddress personAddress = patient.getPersonAddress();
		if (personAddress != null) {
			if (personAddressCache.getId() != null) {
				// if the existing personAddress has been edited
				if (!personAddress.isBlank() && !personAddressCache.isBlank()
				        && !personAddress.equalsContent(personAddressCache)) {
					if (log.isDebugEnabled()) {
						log.debug("Voiding person address with id: " + personAddress.getId()
						        + " and replacing it with a new one: " + personAddress.toString());
					}
					
					foundChanges = true;
					// create a new one and copy the changes to it
					PersonAddress newAddress = (PersonAddress) personAddress.clone();
					newAddress.setPersonAddressId(null);
					newAddress.setUuid(null);
					newAddress.setChangedBy(null);// just in case it had a value
					newAddress.setDateChanged(null);
					newAddress.setCreator(Context.getAuthenticatedUser());
					newAddress.setDateCreated(new Date());
					
					// restore address fields that are checked for changes and
					// void the address
					personAddress.setAddress1(personAddressCache.getAddress1());
					personAddress.setAddress2(personAddressCache.getAddress2());
					personAddress.setAddress3(personAddressCache.getAddress3());
					personAddress.setCityVillage(personAddressCache.getCityVillage());
					personAddress.setCountry(personAddressCache.getCountry());
					personAddress.setCountyDistrict(personAddressCache.getCountyDistrict());
					personAddress.setStateProvince(personAddressCache.getStateProvince());
					personAddress.setPostalCode(personAddressCache.getPostalCode());
					personAddress.setLatitude(personAddressCache.getLatitude());
					personAddress.setLongitude(personAddressCache.getLongitude());
					personAddress.setPreferred(false);
					
					personAddress.setVoided(true);
					personAddress.setVoidReason(Context.getMessageSourceService().getMessage(
					    "general.voidReasonWithArgument", new Object[] { newAddress.toString() },
					    "Voided because it was edited to: " + newAddress.toString(), Context.getLocale()));
					
					// Add the created one
					patient.addAddress(newAddress);
				}
			}
		}
		
		return foundChanges;
	}
	
	/**
	 * Convenience method that transforms a person name to a string while ignoring null and blank
	 * values, the returned string only contains the givenName, middleName and familyName
	 * 
	 * @param name the person name to transform
	 * @return the transformed string ignoring blanks and nulls
	 */
	public static String getPersonNameString(PersonName name) {
		ArrayList<String> tempName = new ArrayList<String>();
		if (StringUtils.isNotBlank(name.getGivenName())) {
			tempName.add(name.getGivenName().trim());
		}
		if (StringUtils.isNotBlank(name.getMiddleName())) {
			tempName.add(name.getMiddleName().trim());
		}
		if (StringUtils.isNotBlank(name.getFamilyName())) {
			tempName.add(name.getFamilyName().trim());
		}
		
		return StringUtils.join(tempName, " ");
	}
}
