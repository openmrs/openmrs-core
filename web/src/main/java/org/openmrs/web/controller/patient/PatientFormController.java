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

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.LocationBehavior;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.PatientIdentifierValidator;
import org.openmrs.validator.PatientValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.person.PersonFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Patient-specific form controller. Creates the model/view etc for editing patients.
 *
 * @see org.openmrs.web.controller.person.PersonFormController
 */
public class PatientFormController extends PersonFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	PatientValidator patientValidator;
	
	/**
	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
	 * are expected
	 *
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
		binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor());
		binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	        BindException errors) throws Exception {
		
		Patient patient = (Patient) object;
		
		if (Context.isAuthenticated()) {
			
			PatientService ps = Context.getPatientService();
			LocationService ls = Context.getLocationService();
			Object[] objs = null;
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			
			if (action.equals(msa.getMessage("Patient.save"))) {
				
				patientValidator.validate(patient, errors);
				
				if (errors.hasErrors()) {
					return showForm(request, response, errors);
				}
				
				updatePersonNames(request, patient);
				
				updatePersonAddresses(request, patient, errors);
				
				updatePersonAttributes(request, errors, patient);
				
				// Patient Identifiers
				objs = patient.getIdentifiers().toArray();
				for (int i = 0; i < objs.length; i++) {
					if (request.getParameter("identifiers[" + i + "].identifier") == null) {
						patient.removeIdentifier((PatientIdentifier) objs[i]);
					}
				}
				
				String[] ids = request.getParameterValues("identifier");
				String[] idTypes = request.getParameterValues("identifierType");
				String[] locs = request.getParameterValues("location");
				String[] idPrefStatus = ServletRequestUtils.getStringParameters(request, "preferred");
				
				if (ids != null) {
					for (int i = 0; i < ids.length; i++) {
						String id = ids[i].trim();
						if (!"".equals(id) && !"".equals(idTypes[i])) { //skips invalid and blank identifiers/identifierTypes
							PatientIdentifier pi = new PatientIdentifier();
							pi.setIdentifier(id);
							pi.setIdentifierType(ps.getPatientIdentifierType(Integer.valueOf(idTypes[i])));
							if (StringUtils.isNotEmpty(locs[i])) {
								pi.setLocation(ls.getLocation(Integer.valueOf(locs[i])));
							}
							if (idPrefStatus != null && idPrefStatus.length > i) {
								pi.setPreferred(new Boolean(idPrefStatus[i]));
							}
							new PatientIdentifierValidator().validate(pi, errors);
							if (errors.hasErrors()) {
								return showForm(request, response, errors);
							}
							patient.addIdentifier(pi);
						}
					}
				}
				
				Iterator<PatientIdentifier> identifiers = patient.getIdentifiers().iterator();
				PatientIdentifier currentId = null;
				PatientIdentifier preferredId = null;
				while (identifiers.hasNext()) {
					currentId = identifiers.next();
					if (currentId.isPreferred()) {
						if (preferredId != null) { // if there's a preferred identifier already exists, make it preferred=false
							preferredId.setPreferred(false);
						}
						preferredId = currentId;
					}
				}
				if ((preferredId == null) && (currentId != null)) { // No preferred identifiers. Make the last identifier entry as preferred.
					currentId.setPreferred(true);
				}
				
				// check patient identifier formats
				for (PatientIdentifier pi : patient.getIdentifiers()) {
					// skip voided identifiers
					if (pi.isVoided()) {
						continue;
					}
					PatientIdentifierType pit = pi.getIdentifierType();
					String identifier = pi.getIdentifier();
					String format = pit.getFormat();
					String formatDescription = pit.getFormatDescription();
					String formatStr = format;
					if (format == null) {
						formatStr = "";
					}
					if (formatDescription != null && formatDescription.length() > 0) {
						formatStr = formatDescription;
					}
					String[] args = { identifier, formatStr };
					try {
						if (format != null && format.length() > 0 && !identifier.matches(format)) {
							log.error("Identifier format is not valid: (" + format + ") " + identifier);
							String msg = getMessageSourceAccessor().getMessage("error.identifier.formatInvalid", args);
							errors.rejectValue("identifiers", msg);
						}
					}
					catch (Exception e) {
						log.error("exception thrown with: " + pit.getName() + " " + identifier);
						log.error("Error while adding patient identifiers to savedIdentifier list", e);
						String msg = getMessageSourceAccessor().getMessage("error.identifier.formatInvalid", args);
						errors.rejectValue("identifiers", msg);
					}
					
					if (errors.hasErrors()) {
						return showForm(request, response, errors);
					}
				}
				
			} // end "if we're saving the patient"
		}
		
		return super.processFormSubmission(request, response, patient, errors);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 * @should void patient when void reason is not empty
	 * @should not void patient when void reason is empty
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		Patient patient = (Patient) obj;
		
		if (Context.isAuthenticated()) {
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			PatientService ps = Context.getPatientService();
			
			if (action.equals(msa.getMessage("Patient.delete"))) {
				try {
					ps.purgePatient(patient);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.deleted");
					return new ModelAndView(new RedirectView("index.htm"));
				}
				catch (DataIntegrityViolationException e) {
					log.error("Unable to delete patient because of database FK errors: " + patient, e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Patient.cannot.delete");
					return new ModelAndView(new RedirectView(getSuccessView() + "?patientId="
					        + patient.getPatientId().toString()));
				}
			} else if (action.equals(msa.getMessage("Patient.void"))) {
				String voidReason = request.getParameter("voidReason");
				if (StringUtils.isBlank(voidReason)) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Patient.error.void.reasonEmpty");
				} else {
					ps.voidPatient(patient, voidReason);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.voided");
				}
				return new ModelAndView(new RedirectView(getSuccessView() + "?patientId=" + patient.getPatientId()));
			} else if (action.equals(msa.getMessage("Patient.unvoid"))) {
				ps.unvoidPatient(patient);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.unvoided");
				return new ModelAndView(new RedirectView(getSuccessView() + "?patientId=" + patient.getPatientId()));
			} else {
				//boolean isNew = (patient.getPatientId() == null);
				boolean isError = false;
				
				try {
					Context.getPatientService().savePatient(patient);
				}
				catch (ValidationException ve) {
					log.error(ve);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, ve.getMessage());
					isError = true;
				}
				catch (InvalidIdentifierFormatException iife) {
					log.error(iife);
					patient.removeIdentifier(iife.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
					isError = true;
				}
				catch (IdentifierNotUniqueException inue) {
					log.error(inue);
					patient.removeIdentifier(inue.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
					isError = true;
				}
				catch (DuplicateIdentifierException die) {
					log.error(die);
					patient.removeIdentifier(die.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
					isError = true;
				}
				catch (InsufficientIdentifiersException iie) {
					log.error(iie);
					patient.removeIdentifier(iie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					    "PatientIdentifier.error.insufficientIdentifiers");
					isError = true;
				}
				catch (PatientIdentifierException pie) {
					log.error(pie);
					patient.removeIdentifier(pie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.general");
					isError = true;
				}
				
				// If patient is dead
				if (patient.getDead() && !isError) {
					log.debug("Patient is dead, so let's make sure there's an Obs for it");
					// need to make sure there is an Obs that represents the patient's cause of death, if applicable
					
					String causeOfDeathConceptId = Context.getAdministrationService().getGlobalProperty(
					    "concept.causeOfDeath");
					Concept causeOfDeath = Context.getConceptService().getConcept(causeOfDeathConceptId);
					
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
									Location location = Context.getLocationService().getDefaultLocation();
									// TODO person healthcenter //if ( loc == null ) loc = patient.getHealthCenter();
									if (location != null) {
										obsDeath.setLocation(location);
									} else {
										log.error("Could not find a suitable location for which to create this new Obs");
									}
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
										String otherInfo = ServletRequestUtils.getStringParameter(request,
										    "causeOfDeath_other", "");
										if (conceptOther.equals(currCause)) {
											// seems like this is an other concept - let's try to get the "other" field info
											deathReasonChanged = !otherInfo.equals(obsDeath.getValueText());
											log.debug("Setting value_text as " + otherInfo);
											obsDeath.setValueText(otherInfo);
										} else {
											// non empty text value implies concept changed from OTHER NON CODED to NONE
											deathReasonChanged = !"".equals(otherInfo);
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
						log
						        .debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
					}
					
				}
				
				if (!isError) {
					String view = getSuccessView();
					
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
					
					view = view + "?patientId=" + patient.getPatientId();
					return new ModelAndView(new RedirectView(view));
				} else {
					return showForm(request, response, errors);
				}
			}
		}
		return new ModelAndView(new RedirectView(getFormView()));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Patient patient = null;
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			String patientId = request.getParameter("patientId");
			Integer id;
			if (patientId != null) {
				try {
					id = Integer.valueOf(patientId);
					patient = ps.getPatientOrPromotePerson(id);
					if (patient == null) {
						HttpSession session = request.getSession();
						session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "patientDashboard.noPatientWithId");
						session.setAttribute(WebConstants.OPENMRS_ERROR_ARGS, patientId);
						return new Patient();
					}
				}
				catch (NumberFormatException numberError) {
					log.warn("Invalid patientId supplied: '" + patientId + "'", numberError);
				}
			}
		}
		
		if (patient == null) {
			patient = new Patient();
			
			String name = request.getParameter("addName");
			if (name != null) {
				String gender = request.getParameter("addGender");
				String date = request.getParameter("addBirthdate");
				String age = request.getParameter("addAge");
				
				getMiniPerson(patient, name, gender, date, age);
			}
		}
		
		if (patient.getIdentifiers().size() < 1) {
			patient.addIdentifier(new PatientIdentifier());
		} else {
			// we need to check if current patient has preferred id
			// if no we look for suitable one to set it as preferred 
			if (patient.getPatientIdentifier() != null && !patient.getPatientIdentifier().isPreferred()) {
				
				List<PatientIdentifier> pi = patient.getActiveIdentifiers();
				for (PatientIdentifier patientIdentifier : pi) {
					if (!patientIdentifier.isVoided() && !patientIdentifier.getIdentifierType().isRetired()) {
						patientIdentifier.setPreferred(true);
						break;
					}
				}
			}
		}
		
		super.setupFormBackingObject(patient);
		
		return patient;
	}
	
	/**
	 * Called prior to form display. Allows for data to be put in the request to be used in the view
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		
		Patient patient = (Patient) obj;
		List<Form> forms = new Vector<Form>();
		Map<String, Object> map = new HashMap<String, Object>();
		List<Encounter> encounters = new Vector<Encounter>();
		
		if (Context.isAuthenticated() && patient.getPatientId() != null) {
			boolean onlyPublishedForms = true;
			if (Context.hasPrivilege(PrivilegeConstants.VIEW_UNPUBLISHED_FORMS)) {
				onlyPublishedForms = false;
			}
			forms.addAll(Context.getFormService().getForms(null, onlyPublishedForms, null, false, null, null, null));
			
			List<Encounter> encs = Context.getEncounterService().getEncountersByPatient(patient);
			if (encs != null && encs.size() > 0) {
				encounters.addAll(encs);
			}
		}
		
		String patientVariation = "";
		if (patient.isDead()) {
			patientVariation = "Dead";
		}
		
		Concept reasonForExitConcept = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));
		
		if (reasonForExitConcept != null && patient.getPatientId() != null) {
			List<Obs> patientExitObs = Context.getObsService().getObservationsByPersonAndConcept(patient,
			    reasonForExitConcept);
			if (patientExitObs != null && patientExitObs.size() > 0) {
				log.debug("Exit obs is size " + patientExitObs.size());
				if (patientExitObs.size() == 1) {
					Obs exitObs = patientExitObs.iterator().next();
					Concept exitReason = exitObs.getValueCoded();
					Date exitDate = exitObs.getObsDatetime();
					if (exitReason != null && exitDate != null) {
						patientVariation = "Exited";
					}
				} else {
					log.error("Too many reasons for exit - not putting data into model");
				}
			}
		}
		List<PatientIdentifierType> pits = Context.getPatientService().getAllPatientIdentifierTypes();
		boolean identifierLocationUsed = false;
		for (PatientIdentifierType pit : pits) {
			if (pit.getLocationBehavior() == null || pit.getLocationBehavior() == LocationBehavior.REQUIRED) {
				identifierLocationUsed = true;
			}
		}
		map.put("identifierTypes", pits);
		map.put("identifierLocationUsed", identifierLocationUsed);
		map.put("identifiers", patient.getIdentifiers());
		map.put("patientVariation", patientVariation);
		
		map.put("forms", forms);
		
		// empty objects used to create blank template in the view
		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PersonName());
		map.put("emptyAddress", new PersonAddress());
		map.put("encounters", encounters);
		
		super.setupReferenceData(map, patient);
		
		return map;
	}
	
}
