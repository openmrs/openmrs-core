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
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.validator.PatientValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.person.PersonFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * This controller is used for the "mini"/"new"/"short" patient form. Only key/important attributes
 * for the patient are displayed and allowed to be edited
 * 
 * @see org.openmrs.web.controller.patient.PatientFormController
 */

@Controller
@SessionAttributes( { "patientModel", "relationshipsMap", "identifierTypes", "locations" })
public class ShortPatientFormController {
	
	private static final Log log = LogFactory.getLog(ShortPatientFormController.class);
	
	private static final String SHORT_PATIENT_FORM_URL = "/admin/patients/shortPatientForm";
	
	private static final String FIND_PATIENT_PAGE = "findPatient";
	
	private static final String PATIENT_DASHBOARD_URL = "/patientDashboard.form";
	
	@Autowired
	PatientValidator patientValidator;
	
	/**
	 * Sets up the modelObject and data to be displayed in the the short patient form
	 * 
	 * @param patientId the patientId of the patient to edited
	 * @param model modelObject to be used
	 * @param request
	 * @return the view to forward to
	 */
	@RequestMapping(method = RequestMethod.GET, value = SHORT_PATIENT_FORM_URL)
	public String showForm(@RequestParam(value = "patientId", required = false) Integer patientId, ModelMap model,
	        WebRequest request) {
		
		if (Context.isAuthenticated()) {
			Patient patient = null;
			if (patientId != null)
				patient = Context.getPatientService().getPatient(patientId);
			// if this is a redirect from the addPersonController
			else {
				patient = new Patient();
				String name = request.getParameter("addName");
				if (!StringUtils.isBlank(name)) {
					String gender = request.getParameter("addGender");
					String date = request.getParameter("addBirthdate");
					String age = request.getParameter("addAge");
					PersonFormController.getMiniPerson(patient, name, gender, date, age);
				}
			}
			
			ShortPatientModel patientModel = new ShortPatientModel(patient);
			model.addAttribute("patientModel", patientModel);
			model.addAttribute("relationshipsMap", getRelationshipsMap(patient, request));
			model.addAttribute("identifierTypes", Context.getPatientService().getAllPatientIdentifierTypes());
			model.addAttribute("locations", Context.getLocationService().getAllLocations());
			
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
		}
		
		return SHORT_PATIENT_FORM_URL;
	}
	
	/**
	 * Handles the form submission by validating the form fields and saving it to the DB
	 * 
	 * @param request the webRequest object
	 * @param patientModel the modelObject containing the patient info collected from the form
	 *            fields
	 * @param result
	 * @param status
	 * @return the view to forward to
	 */
	@RequestMapping(method = RequestMethod.POST, value = SHORT_PATIENT_FORM_URL)
	public String saveShortPatient(WebRequest request, @ModelAttribute("patientModel") ShortPatientModel patientModel,
	        BindingResult result, SessionStatus status) {
		
		if (Context.isAuthenticated()) {
			// First do form validation so that we can easily bind errors to
			// fields
			new ShortPatientFormValidator().validate(patientModel, result);
			if (result.hasErrors())
				return SHORT_PATIENT_FORM_URL;
			
			Patient patient = null;
			patient = getPatientFromFormData(patientModel);
			patientValidator.validate(patient, result);
			if (result.hasErrors())
				return SHORT_PATIENT_FORM_URL;
			
			patient = Context.getPatientService().savePatient(patient);
			
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService()
			        .getMessage("Patient.saved"), WebRequest.SCOPE_SESSION);
			
			// TODO do we really still need this, besides ensuring that the
			// cause of death is provided?
			// process and save the death info
			saveDeathInfo(patientModel, request);
			
			if (!patient.getVoided()) {
				// save the relationships to the database
				Map<String, Relationship> relationships = getRelationshipsMap(patient, request);
				for (Relationship relationship : relationships.values()) {
					// if the user added a person to this relationship, save it
					if (relationship.getPersonA() != null && relationship.getPersonB() != null)
						Context.getPersonService().saveRelationship(relationship);
				}
			}
			// clear the session attributes
			status.setComplete();
			
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
		patient.addName(patientModel.getPersonName());
		
		PersonAddress personAddress = patientModel.getPersonAddress();
		
		if (personAddress != null) {
			if (personAddress.isVoided() && StringUtils.isBlank(personAddress.getVoidReason())) {
				personAddress.setVoidReason(Context.getMessageSourceService().getMessage("general.default.voidReason"));
			}
			// don't add an address that is being created and at the
			// same time being removed
			else if (!(personAddress.isVoided() && personAddress.getPersonAddressId() == null))
				patient.addAddress(personAddress);
		}
		
		// add all the existing identifiers and any new ones.
		for (PatientIdentifier id : patientModel.getIdentifiers()) {
			//skip past the new ones removed from the user interface(may be they were invalid
			//and the user changed their mind about adding them and they removed them)
			if (id.getPatientIdentifierId() == null && id.isVoided())
				continue;
			
			patient.addIdentifier(id);
		}
		
		// ensure that there is only one identifier set as preferred in
		// case the database
		// records were already unclean
		boolean foundPreferredIdentifier = false;
		for (PatientIdentifier possiblePreferredId : patient.getActiveIdentifiers()) {
			if (possiblePreferredId.isPreferred() && !foundPreferredIdentifier)
				foundPreferredIdentifier = true;
			else if (possiblePreferredId.isPreferred()) {
				log.info("Found multiple preferred identifiers, " + possiblePreferredId.getIdentifier()
				        + " is being unset as preferred");
				possiblePreferredId.setPreferred(false);
			}
		}
		
		// add the person attributes
		for (PersonAttribute formAttribute : patientModel.getPersonAttributes())
			patient.addAttribute(formAttribute);
		
		return patient;
	}
	
	/**
	 * Creates a map of string of the form 3b, 3a and the actual person Relationships
	 * 
	 * @param person the patient/person whose relationships to return
	 * @param request the webRequest Object
	 * @return map of strings matched against actual relationships
	 */
	private Map<String, Relationship> getRelationshipsMap(Person person, WebRequest request) {
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
				// that matches to the user desired relation. Overwrite
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
							if (dateDeath == null)
								dateDeath = new Date();
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
									if (otherInfo == null)
										otherInfo = "";
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
							
							if (StringUtils.isBlank(obsDeath.getVoidReason()))
								obsDeath.setVoidReason(Context.getMessageSourceService().getMessage(
								    "general.default.changeReason"));
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
	
}
