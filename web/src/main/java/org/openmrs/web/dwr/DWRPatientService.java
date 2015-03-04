/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergySeverity;
import org.openmrs.activelist.AllergyType;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;
import org.openmrs.util.OpenmrsConstants;

/**
 * DWR patient methods. The methods in here are used in the webapp to get data from the database via
 * javascript calls.
 *
 * @see PatientService
 */
public class DWRPatientService implements GlobalPropertyListener {
	
	private static final Log log = LogFactory.getLog(DWRPatientService.class);
	
	private static Integer maximumResults;
	
	/**
	 * Search on the <code>searchValue</code>. If a number is in the search string, do an identifier
	 * search. Else, do a name search
	 *
	 * @param searchValue string to be looked for
	 * @param includeVoided true/false whether or not to included voided patients
	 * @return Collection<Object> of PatientListItem or String
	 * @should return only patient list items with nonnumeric search
	 * @should return string warning if invalid patient identifier
	 * @should not return string warning if searching with valid identifier
	 * @should include string in results if doing extra decapitated search
	 * @should not return duplicate patient list items if doing decapitated search
	 * @should not do decapitated search if numbers are in the search string
	 * @should get results for patients that have edited themselves
	 * @should logged in user should load their own patient object
	 */
	public Collection<Object> findPatients(String searchValue, boolean includeVoided) {
		return findBatchOfPatients(searchValue, includeVoided, null, null);
	}
	
	public static void setMaximumResults(Integer maximumResults) {
		DWRPatientService.maximumResults = maximumResults;
	}
	
	/**
	 * Search on the <code>searchValue</code>. If a number is in the search string, do an identifier
	 * search. Else, do a name search
	 *
	 * @see PatientService#getPatients(String, String, List, boolean, int, Integer)
	 * @param searchValue string to be looked for
	 * @param includeVoided true/false whether or not to included voided patients
	 * @param start The starting index for the results to return
	 * @param length The number of results of return
	 * @return Collection<Object> of PatientListItem or String
	 * @since 1.8
	 */
	public Collection<Object> findBatchOfPatients(String searchValue, boolean includeVoided, Integer start, Integer length) {
		if (maximumResults == null) {
			setMaximumResults(getMaximumSearchResults());
		}
		if (length != null && length > maximumResults) {
			length = maximumResults;
		}
		
		// the list to return
		List<Object> patientList = new Vector<Object>();
		
		PatientService ps = Context.getPatientService();
		Collection<Patient> patients;
		
		try {
			patients = ps.getPatients(searchValue, includeVoided, start, length);
		}
		catch (APIAuthenticationException e) {
			patientList.add(Context.getMessageSourceService().getMessage("Patient.search.error") + " - " + e.getMessage());
			return patientList;
		}
		
		patientList = new Vector<Object>(patients.size());
		for (Patient p : patients) {
			patientList.add(new PatientListItem(p, searchValue));
		}
		//no results found and a number was in the search --
		//should check whether the check digit is correct.
		if (patients.size() == 0 && searchValue.matches(".*\\d+.*")) {
			
			//Looks through all the patient identifier validators to see if this type of identifier
			//is supported for any of them.  If it isn't, then no need to warn about a bad check
			//digit.  If it does match, then if any of the validators validates the check digit
			//successfully, then the user is notified that the identifier has been entered correctly.
			//Otherwise, the user is notified that the identifier was entered incorrectly.
			
			Collection<IdentifierValidator> pivs = ps.getAllIdentifierValidators();
			boolean shouldWarnUser = true;
			boolean validCheckDigit = false;
			boolean identifierMatchesValidationScheme = false;
			
			for (IdentifierValidator piv : pivs) {
				try {
					if (piv.isValid(searchValue)) {
						shouldWarnUser = false;
						validCheckDigit = true;
					}
					identifierMatchesValidationScheme = true;
				}
				catch (UnallowedIdentifierException e) {
					log.error("Error while validating identifier", e);
				}
			}
			
			if (identifierMatchesValidationScheme) {
				if (shouldWarnUser) {
					patientList
					        .add("<p style=\"color:red; font-size:big;\"><b>WARNING: Identifier has been typed incorrectly!  Please double check the identifier.</b></p>");
				} else if (validCheckDigit) {
					patientList
					        .add("<p style=\"color:green; font-size:big;\"><b>This identifier has been entered correctly, but still no patients have been found.</b></p>");
				}
			}
		}
		
		return patientList;
	}
	
	/**
	 * Returns a map of results with the values as count of matches and a partial list of the
	 * matching patients (depending on values of start and length parameters) while the keys are are
	 * 'count' and 'objectList' respectively, if the length parameter is not specified, then all
	 * matches will be returned from the start index if specified.
	 *
	 * @param searchValue patient name or identifier
	 * @param includeVoided true/false whether or not to included voided patients
	 * @param start the beginning index
	 * @param length the number of matching patients to return
	 * @param getMatchCount Specifies if the count of matches should be included in the returned map
	 * @return a map of results
	 * @throws APIException
	 */
	public Map<String, Object> findCountAndPatientsWithVoided(String searchValue, Integer start, Integer length,
	        boolean getMatchCount, Boolean includeVoided) throws APIException {
		
		//Map to return
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Collection<Object> objectList = new Vector<Object>();
		
		if (includeVoided == null) {
			includeVoided = false;
		}
		
		try {
			PatientService ps = Context.getPatientService();
			int patientCount = 0;
			//if this is the first call
			if (getMatchCount) {
				patientCount += ps.getCountOfPatients(searchValue, includeVoided);
				
				// if there are no results found and a number was not in the
				// search and this is the first call, then do a decapitated search: 
				//trim each word down to the first three characters and search again				
				if (patientCount == 0 && start == 0 && !searchValue.matches(".*\\d+.*")) {
					String[] names = searchValue.split(" ");
					StringBuilder newSearch = new StringBuilder("");
					for (String name : names) {
						if (name.length() > 3) {
							name = name.substring(0, 3);
						}
						newSearch.append(" ").append(name);
					}
					
					String newSearchStr = newSearch.toString().trim();
					if (!newSearchStr.equals(searchValue)) {
						int newPatientCount = ps.getCountOfPatients(newSearchStr);
						if (newPatientCount > 0) {
							// Send a signal to the core search widget to search again against newSearch
							resultsMap.put("searchAgain", newSearchStr);
							resultsMap.put("notification", Context.getMessageSourceService().getMessage(
							    "searchWidget.noResultsFoundFor", new Object[] { searchValue, newSearchStr },
							    Context.getLocale()));
						}
					}
				}

				//no results found and a number was in the search --
				//should check whether the check digit is correct.
				else if (patientCount == 0 && searchValue.matches(".*\\d+.*")) {
					
					//Looks through all the patient identifier validators to see if this type of identifier
					//is supported for any of them.  If it isn't, then no need to warn about a bad check
					//digit.  If it does match, then if any of the validators validates the check digit
					//successfully, then the user is notified that the identifier has been entered correctly.
					//Otherwise, the user is notified that the identifier was entered incorrectly.
					
					Collection<IdentifierValidator> pivs = ps.getAllIdentifierValidators();
					boolean shouldWarnUser = true;
					boolean validCheckDigit = false;
					boolean identifierMatchesValidationScheme = false;
					
					for (IdentifierValidator piv : pivs) {
						try {
							if (piv.isValid(searchValue)) {
								shouldWarnUser = false;
								validCheckDigit = true;
							}
							identifierMatchesValidationScheme = true;
						}
						catch (UnallowedIdentifierException e) {}
					}
					
					if (identifierMatchesValidationScheme) {
						if (shouldWarnUser) {
							resultsMap.put("notification", "<b>"
							        + Context.getMessageSourceService().getMessage("Patient.warning.inValidIdentifier")
							        + "<b/>");
						} else if (validCheckDigit) {
							resultsMap.put("notification", "<b style=\"color:green;\">"
							        + Context.getMessageSourceService().getMessage("Patient.message.validIdentifier")
							        + "<b/>");
						}
					}
				} else {
					//ensure that count never exceeds this value because the API's service layer would never
					//return more than it since it is limited in the DAO layer
					if (maximumResults == null) {
						setMaximumResults(getMaximumSearchResults());
					}
					if (length != null && length > maximumResults) {
						length = maximumResults;
					}
					
					if (patientCount > maximumResults) {
						patientCount = maximumResults;
						if (log.isDebugEnabled()) {
							log.debug("Limitng the size of matching patients to " + maximumResults);
						}
					}
				}
				
			}
			
			//if we have any matches or this isn't the first ajax call when the caller
			//requests for the count
			if (patientCount > 0 || !getMatchCount) {
				objectList = findBatchOfPatients(searchValue, includeVoided, start, length);
			}
			
			resultsMap.put("count", patientCount);
			resultsMap.put("objectList", objectList);
		}
		catch (Exception e) {
			log.error("Error while searching for patients", e);
			objectList.clear();
			objectList.add(Context.getMessageSourceService().getMessage("Patient.search.error") + " - " + e.getMessage());
			resultsMap.put("count", 0);
			resultsMap.put("objectList", objectList);
		}
		return resultsMap;
	}
	
	/**
	 * Returns a map of results with the values as count of matches and a partial list of the
	 * matching patients (depending on values of start and length parameters) while the keys are are
	 * 'count' and 'objectList' respectively, if the length parameter is not specified, then all
	 * matches will be returned from the start index if specified.
	 *
	 * @param searchValue patient name or identifier
	 * @param start the beginning index
	 * @param length the number of matching patients to return
	 * @param getMatchCount Specifies if the count of matches should be included in the returned map
	 * @return a map of results
	 * @throws APIException
	 * @since 1.8
	 * @should signal for a new search if the new search value has matches and is a first call
	 * @should not signal for a new search if it is not the first ajax call
	 * @should not signal for a new search if the new search value has no matches
	 * @should match patient with identifiers that contain no digit
	 */
	public Map<String, Object> findCountAndPatients(String searchValue, Integer start, Integer length, boolean getMatchCount)
	        throws APIException {
		return findCountAndPatientsWithVoided(searchValue, start, length, getMatchCount, false);
	}
	
	/**
	 * Convenience method for dwr/javascript to convert a patient id into a Patient object (or at
	 * least into data about the patient)
	 *
	 * @param patientId the {@link Patient#getPatientId()} to match on
	 * @return a truncated Patient object in the form of a PatientListItem
	 */
	public PatientListItem getPatient(Integer patientId) {
		PatientService ps = Context.getPatientService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		if (p != null && p.getAddresses() != null && p.getAddresses().size() > 0) {
			PersonAddress pa = (PersonAddress) p.getAddresses().toArray()[0];
			pli.setAddress1(pa.getAddress1());
			pli.setAddress2(pa.getAddress2());
		}
		return pli;
	}
	
	/**
	 * find all patients with duplicate attributes (searchOn)
	 *
	 * @param searchOn
	 * @return list of patientListItems
	 */
	public Vector<Object> findDuplicatePatients(String[] searchOn) {
		Vector<Object> patientList = new Vector<Object>();
		
		try {
			List<String> options = new Vector<String>(searchOn.length);
			for (String s : searchOn) {
				options.add(s);
			}
			
			List<Patient> patients = Context.getPatientService().getDuplicatePatientsByAttributes(options);
			
			if (patients.size() > 200) {
				patients.subList(0, 200);
			}
			
			for (Patient p : patients) {
				patientList.add(new PatientListItem(p));
			}
		}
		catch (Exception e) {
			log.error(e);
			patientList.add("Error while attempting to find duplicate patients - " + e.getMessage());
		}
		
		return patientList;
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @param patientId
	 * @param identifierType
	 * @param identifier
	 * @param identifierLocationId
	 * @return
	 */
	public String addIdentifier(Integer patientId, String identifierType, String identifier, Integer identifierLocationId) {
		
		String ret = "";
		
		if (identifier == null || identifier.length() == 0) {
			return "PatientIdentifier.error.general";
		}
		PatientService ps = Context.getPatientService();
		LocationService ls = Context.getLocationService();
		Patient p = ps.getPatient(patientId);
		PatientIdentifierType idType = ps.getPatientIdentifierTypeByName(identifierType);
		//ps.updatePatientIdentifier(pi);
		Location location = ls.getLocation(identifierLocationId);
		log.debug("idType=" + identifierType + "->" + idType + " , location=" + identifierLocationId + "->" + location
		        + " identifier=" + identifier);
		PatientIdentifier id = new PatientIdentifier();
		id.setIdentifierType(idType);
		id.setIdentifier(identifier);
		id.setLocation(location);
		
		// in case we are editing, check to see if there is already an ID of this type and location
		for (PatientIdentifier previousId : p.getActiveIdentifiers()) {
			if (previousId.getIdentifierType().equals(idType) && previousId.getLocation().equals(location)) {
				log.debug("Found equivalent ID: [" + idType + "][" + location + "][" + previousId.getIdentifier()
				        + "], about to remove");
				p.removeIdentifier(previousId);
			} else {
				if (!previousId.getIdentifierType().equals(idType)) {
					log.debug("Previous ID id type does not match: [" + previousId.getIdentifierType().getName() + "]["
					        + previousId.getIdentifier() + "]");
				}
				if (!previousId.getLocation().equals(location)) {
					log.debug("Previous ID location is: " + previousId.getLocation());
					log.debug("New location is: " + location);
				}
			}
		}
		
		p.addIdentifier(id);
		
		try {
			ps.savePatient(p);
		}
		catch (InvalidIdentifierFormatException iife) {
			log.error(iife);
			ret = "PatientIdentifier.error.formatInvalid";
		}
		catch (InvalidCheckDigitException icde) {
			log.error(icde);
			ret = "PatientIdentifier.error.checkDigit";
		}
		catch (IdentifierNotUniqueException inue) {
			log.error(inue);
			ret = "PatientIdentifier.error.notUnique";
		}
		catch (DuplicateIdentifierException die) {
			log.error(die);
			ret = "PatientIdentifier.error.duplicate";
		}
		catch (InsufficientIdentifiersException iie) {
			log.error(iie);
			ret = "PatientIdentifier.error.insufficientIdentifiers";
		}
		catch (PatientIdentifierException pie) {
			log.error(pie);
			ret = "PatientIdentifier.error.general";
		}
		
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @param patientId
	 * @param reasonForExitId
	 * @param dateOfExit
	 * @param causeOfDeath
	 * @param otherReason
	 * @return
	 */
	public String exitPatientFromCare(Integer patientId, Integer exitReasonId, String exitDateStr,
	        Integer causeOfDeathConceptId, String otherReason) {
		log.debug("Entering exitfromcare with [" + patientId + "] [" + exitReasonId + "] [" + exitDateStr + "]");
		String ret = "";
		
		PatientService ps = Context.getPatientService();
		ConceptService cs = Context.getConceptService();
		
		Patient patient = null;
		try {
			patient = ps.getPatient(patientId);
		}
		catch (Exception e) {
			patient = null;
		}
		
		if (patient == null) {
			ret = "Unable to find valid patient with the supplied identification information - cannot exit patient from care";
		}
		
		// Get the exit reason concept (if possible)
		Concept exitReasonConcept = null;
		try {
			exitReasonConcept = cs.getConcept(exitReasonId);
		}
		catch (Exception e) {
			exitReasonConcept = null;
		}
		
		// Exit reason error handling
		if (exitReasonConcept == null) {
			ret = "Unable to locate reason for exit in dictionary - cannot exit patient from care";
		}
		
		// Parse the exit date
		Date exitDate = null;
		if (exitDateStr != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				exitDate = sdf.parse(exitDateStr);
			}
			catch (ParseException e) {
				exitDate = null;
			}
		}
		
		// Exit date error handling
		if (exitDate == null) {
			ret = "Invalid date supplied - cannot exit patient from care without a valid date.";
		}
		
		// If all data is provided as expected
		if (patient != null && exitReasonConcept != null && exitDate != null) {
			
			// need to check first if this is death or not
			String patientDiedConceptId = Context.getAdministrationService().getGlobalProperty("concept.patientDied");
			
			Concept patientDiedConcept = null;
			if (patientDiedConceptId != null) {
				patientDiedConcept = cs.getConcept(patientDiedConceptId);
			}
			
			// If there is a concept for death in the dictionary
			if (patientDiedConcept != null) {
				
				// If the exist reason == patient died
				if (exitReasonConcept.equals(patientDiedConcept)) {
					
					Concept causeOfDeathConcept = null;
					try {
						causeOfDeathConcept = cs.getConcept(causeOfDeathConceptId);
					}
					catch (Exception e) {
						causeOfDeathConcept = null;
					}
					
					// Cause of death concept exists
					if (causeOfDeathConcept != null) {
						try {
							ps.processDeath(patient, exitDate, causeOfDeathConcept, otherReason);
						}
						catch (Exception e) {
							log.warn("Caught error", e);
							ret = "Internal error while trying to process patient death - unable to proceed. Cause: "
							        + e.getMessage();
						}
					}
					// cause of death concept does not exist
					else {
						ret = "Unable to locate cause of death in dictionary - cannot proceed";
					}
				}

				// Otherwise, we process this as an exit
				else {
					try {
						ps.exitFromCare(patient, exitDate, exitReasonConcept);
					}
					catch (Exception e) {
						log.warn("Caught error", e);
						ret = "Internal error while trying to exit patient from care - unable to exit patient from care at this time. Cause: "
						        + e.getMessage();
					}
				}
			}

			// If the system does not recognize death as a concept, then we exit from care
			else {
				try {
					ps.exitFromCare(patient, exitDate, exitReasonConcept);
				}
				catch (Exception e) {
					log.warn("Caught error", e);
					ret = "Internal error while trying to exit patient from care - unable to exit patient from care at this time. Cause: "
					        + e.getMessage();
				}
			}
			log.debug("Exited from care, it seems");
		}
		
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @param patientId
	 * @param locationId
	 * @return
	 */
	public String changeHealthCenter(Integer patientId, Integer locationId) {
		log.warn("Deprecated method in 'DWRPatientService.changeHealthCenter'");
		
		String ret = "";
		
		/*
		
		if ( patientId != null && locationId != null ) {
			Patient patient = Context.getPatientService().getPatient(patientId);
			Location location = Context.getEncounterService().getLocation(locationId);
			
			if ( patient != null && location != null ) {
				patient.setHealthCenter(location);
				Context.getPatientService().updatePatient(patient);
			}
		}
		*/

		return ret;
	}
	
	/**
	 * Creates an Allergy Item
	 *
	 * @param patientId
	 * @param allergenId
	 * @param type
	 * @param pStartDate
	 * @param severity
	 * @param reactionId
	 */
	public void createAllergy(Integer patientId, Integer allergenId, String type, String pStartDate, String severity,
	        Integer reactionId) {
		Date startDate = parseDate(pStartDate);
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		Concept allergyConcept = Context.getConceptService().getConcept(allergenId);
		Concept reactionConcept = (reactionId == null) ? null : Context.getConceptService().getConcept(reactionId);
		AllergySeverity allergySeverity = StringUtils.isBlank(severity) ? null : AllergySeverity.valueOf(severity);
		AllergyType allergyType = StringUtils.isBlank(type) ? null : AllergyType.valueOf(type);
		
		Allergy allergy = new Allergy(patient, allergyConcept, startDate, allergyType, reactionConcept, allergySeverity);
		Context.getPatientService().saveAllergy(allergy);
	}
	
	/**
	 * Save an Allergy
	 *
	 * @param activeListItemId
	 * @param allergenId Concept ID
	 * @param type
	 * @param pStartDate
	 * @param severity
	 * @param reactionId
	 */
	public void saveAllergy(Integer activeListItemId, Integer allergenId, String type, String pStartDate, String severity,
	        Integer reactionId) {
		//get the allergy
		Allergy allergy = Context.getPatientService().getAllergy(activeListItemId);
		allergy.setAllergen(Context.getConceptService().getConcept(allergenId));
		allergy.setAllergyType(type);
		allergy.setStartDate(parseDate(pStartDate));
		allergy.setSeverity(severity);
		allergy.setReaction((reactionId == null) ? null : Context.getConceptService().getConcept(reactionId));
		Context.getPatientService().saveAllergy(allergy);
	}
	
	/**
	 * Resolve an allergy
	 *
	 * @param activeListId
	 * @param resolved
	 * @param reason
	 * @param pEndDate
	 */
	public void removeAllergy(Integer activeListId, String reason) {
		Allergy allergy = Context.getPatientService().getAllergy(activeListId);
		Context.getPatientService().removeAllergy(allergy, reason);
	}
	
	/**
	 * Voids the Allergy
	 *
	 * @param activeListId
	 * @param reason
	 */
	public void voidAllergy(Integer activeListId, String reason) {
		Allergy allergy = Context.getPatientService().getAllergy(activeListId);
		if (reason == null) {
			reason = "Error - user entered incorrect data from UI";
		}
		Context.getPatientService().voidAllergy(allergy, reason);
	}
	
	/**
	 * Creates a Problem Item
	 *
	 * @param patientId
	 * @param problemId
	 * @param status
	 * @param pStartDate
	 * @param comments
	 */
	public void createProblem(Integer patientId, Integer problemId, String status, String pStartDate, String comments) {
		Patient patient = Context.getPatientService().getPatient(patientId);
		Concept problemConcept = Context.getConceptService().getConcept(problemId);
		ProblemModifier modifier = StringUtils.isBlank(status) ? null : ProblemModifier.valueOf(status);
		
		Problem problem = new Problem(patient, problemConcept, parseDate(pStartDate), modifier, comments, null);
		Context.getPatientService().saveProblem(problem);
	}
	
	/**
	 * Saves the Problem
	 *
	 * @param activeListId
	 * @param problemId
	 * @param status
	 * @param pStartDate
	 * @param comments
	 */
	public void saveProblem(Integer activeListId, Integer problemId, String status, String pStartDate, String comments) {
		//get the allergy
		Problem problem = Context.getPatientService().getProblem(activeListId);
		problem.setProblem(Context.getConceptService().getConcept(problemId));
		problem.setModifier(status);
		problem.setStartDate(parseDate(pStartDate));
		problem.setComments(comments);
		Context.getPatientService().saveProblem(problem);
	}
	
	/**
	 * Remove a problem, sets the end date
	 *
	 * @param activeListId
	 * @param resolved
	 * @param reason
	 * @param pEndDate
	 */
	public void removeProblem(Integer activeListId, String reason, String pEndDate) {
		Problem problem = Context.getPatientService().getProblem(activeListId);
		problem.setEndDate(parseDate(pEndDate));
		
		Context.getPatientService().removeProblem(problem, reason);
	}
	
	/**
	 * Voids the Problem
	 *
	 * @param activeListId
	 * @param reason
	 */
	public void voidProblem(Integer activeListId, String reason) {
		Problem problem = Context.getPatientService().getProblem(activeListId);
		if (reason == null) {
			reason = "Error - user entered incorrect data from UI";
		}
		Context.getPatientService().voidProblem(problem, reason);
	}
	
	/**
	 * Simple utility method to parse the date object into the correct, local format
	 *
	 * @param date
	 * @return
	 */
	private Date parseDate(String date) {
		if (date != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				return sdf.parse(date);
			}
			catch (ParseException e) {}
		}
		return null;
	}
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		try {
			setMaximumResults(Integer.valueOf(newValue.getPropertyValue()));
		}
		catch (NumberFormatException e) {
			setMaximumResults(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE);
		}
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		setMaximumResults(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE);
	}
	
	/**
	 * Fetch the max results value from the global properties table
	 *
	 * @return Integer value for the person search max results global property
	 */
	private static Integer getMaximumSearchResults() {
		try {
			return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS,
			    String.valueOf(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE)));
		}
		catch (Exception e) {
			log.warn("Unable to convert the global property " + OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS
			        + "to a valid integer. Returning the default "
			        + OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE);
		}
		
		return OpenmrsConstants.GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE;
	}
}
