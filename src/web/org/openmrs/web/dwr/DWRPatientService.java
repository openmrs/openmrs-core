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
package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.Tribe;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * DWR patient methods
 */
public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Search on the <code>searchValue</code>.  If a number is in the search
	 * string, do an identifier search.  Else, do a name search
	 * 
	 * @param searchValue string to be looked for 
	 * @param includeVoided true/false whether or not to included voided patients
	 * @return Collection<Object> of PatientListItem or String
	 */
	public Collection findPatients(String searchValue, boolean includeVoided) {
		
		Collection<Object> patientList = new Vector<Object>();

		Integer userId = -1;
		if (Context.isAuthenticated())
			userId = Context.getAuthenticatedUser().getUserId();
		log.info(userId + "|" + searchValue);
		
		PatientService ps = Context.getPatientService();
		Collection<Patient> patients;
		
		patients = ps.findPatients(searchValue, includeVoided);
		patientList = new Vector<Object>(patients.size());
		for (Patient p : patients)
			patientList.add(new PatientListItem(p));
		
		// only 2 results found and a number was not in the search
		// decapitated search
		if (patientList.size() < 3 && !searchValue.matches(".*\\d+.*")) {
			String[] names = searchValue.split(" ");
			String newSearch = "";
			for (String name : names) {
				if (name.length() > 3)
					name = name.substring(0, 4);
				newSearch += " " + name;
			}
			
			newSearch = newSearch.trim();
			Collection<Patient> newPatients = ps.findPatients(newSearch, includeVoided);
			newPatients.removeAll(patients);
			if (newPatients.size() > 0) {
				patientList.add("Minimal patients returned. Results for <b>" + newSearch + "</b>");
				for (Patient p : newPatients) {
					PatientListItem pi = new PatientListItem(p);
					patientList.add(pi);
				}
			}
		}
				
		return patientList;
	}
	
	public PatientListItem getPatient(Integer patientId) {
		PatientService ps = Context.getPatientService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		if (p.getAddresses() != null && p.getAddresses().size() > 0) {
			PersonAddress pa = (PersonAddress)p.getAddresses().toArray()[0];
			pli.setAddress1(pa.getAddress1());
			pli.setAddress2(pa.getAddress2());
		}
		return pli;
	}
	
	public Vector findTribes(String search) {
		Vector<Object> tribeList = new Vector<Object>();
		
		try {
			tribeList.addAll(Context.getPatientService().findTribes(search));
		} catch (Exception e) {
			log.error(e);
			tribeList.add("Error while attempting to find tribe - " + e.getMessage());
		}
		
		return tribeList;
			
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Tribe> getTribes() {
		Vector tribeList = new Vector();
		
		try {
			tribeList.addAll(Context.getPatientService().getTribes());
		} catch (Exception e) {
			log.error(e);
			tribeList.add("Error while attempting to find tribe - " + e.getMessage());
		}
		
		return tribeList;
	}
	
	/**
	 * find all patients with duplicate attributes (searchOn)
	 *   
	 * @param searchOn
	 * @return list of patientListItems
	 */
	public Vector findDuplicatePatients(String[] searchOn) {
		Vector<Object> patientList = new Vector<Object>();
		
		try {
			Set<String> options = new HashSet<String>(searchOn.length);
			for (String s : searchOn)
				options.add(s);
			
			List<Patient> patients = Context.getPatientService().findDuplicatePatients(options);
			
			if (patients.size() > 200)
				patients.subList(0, 200);
			
			for (Patient p : patients)
				patientList.add(new PatientListItem(p));
		} catch (Exception e) {
			log.error(e);
			patientList.add("Error while attempting to find duplicate patients - " + e.getMessage());
		}
		
		return patientList;
	}

	
	public String addIdentifier(Integer patientId, String identifierType, String identifier, Integer identifierLocationId) {

		String ret = "";
		
		if (identifier == null || identifier.length() == 0)
			return "PatientIdentifier.error.general";
		PatientService ps = Context.getPatientService();
		EncounterService es = Context.getEncounterService();
		Patient p = ps.getPatient(patientId);
		PatientIdentifierType idType = ps.getPatientIdentifierType(identifierType);
		//ps.updatePatientIdentifier(pi);
		Location location = es.getLocation(identifierLocationId);
		log.debug("idType=" + identifierType + "->" + idType + " , location=" + identifierLocationId + "->" + location + " identifier=" + identifier);
		PatientIdentifier id = new PatientIdentifier();
		id.setIdentifierType(idType);
		id.setIdentifier(identifier);
		id.setLocation(location);

		// in case we are editing, check to see if there is already an ID of this type and location
		for ( PatientIdentifier previousId : p.getActiveIdentifiers() ) {
			if ( previousId.getIdentifierType().equals(idType) && previousId.getLocation().equals(location) ) {
				log.debug("Found equivalent ID: [" + idType + "][" + location + "][" + previousId.getIdentifier() + "], about to remove");
				p.removeIdentifier(previousId);
			} else {
				if ( !previousId.getIdentifierType().equals(idType) ) log.debug("Previous ID id type does not match: [" + previousId.getIdentifierType().getName() + "][" + previousId.getIdentifier() + "]");
				if ( !previousId.getLocation().equals(location) ) {
					log.debug("Previous ID location is: " + previousId.getLocation());
					log.debug("New location is: " + location);
				}
			}
		}
		
		p.addIdentifier(id);

		try {
			ps.updatePatient(p);
		} catch ( InvalidIdentifierFormatException iife ) {
			log.error(iife);
			ret = "PatientIdentifier.error.formatInvalid";
		} catch ( InvalidCheckDigitException icde ) {
			log.error(icde);
			ret = "PatientIdentifier.error.checkDigit";
		} catch ( IdentifierNotUniqueException inue ) {
			log.error(inue);
			ret = "PatientIdentifier.error.notUnique";
		} catch ( DuplicateIdentifierException die ) {
			log.error(die);
			ret = "PatientIdentifier.error.duplicate";
		} catch ( InsufficientIdentifiersException iie ) {
			log.error(iie);
			ret = "PatientIdentifier.error.insufficientIdentifiers";
		} catch ( PatientIdentifierException pie ) {
			log.error(pie);
			ret = "PatientIdentifier.error.general";
		}

		return ret;
	}
	
	public String exitPatientFromCare(Integer patientId, Integer reasonForExitId, String dateOfExit, Integer causeOfDeath, String otherReason ) {
		log.debug("Entering exitfromcare with [" + patientId + "] [" + reasonForExitId + "] [" + dateOfExit + "]");
		String ret = "";
		
		PatientService ps = Context.getPatientService();
		ConceptService cs = Context.getConceptService();
		
		Patient p = null;
		try {
			p = ps.getPatient(patientId);
		} catch ( Exception e ) {
			p = null;
		}
		
		if ( p == null ) {
			ret = "Unable to find valid patient with the supplied identification information - cannot exit patient from care";
		}
		
		Concept c = null;
		try {
			c = cs.getConcept(reasonForExitId);
		} catch ( Exception e ) {
			c = null;
		}
		
		if ( c == null ) {
			ret = "Unable to locate reason for exit in dictionary - cannot exit patient from care";
		}
		
		Date exitDate = null;
		if ( dateOfExit != null ) {
			SimpleDateFormat sdf = OpenmrsUtil.getDateFormat();
			try {
				exitDate = sdf.parse(dateOfExit);
			} catch (ParseException e) {
				exitDate = null;
			}
		}

		if ( exitDate == null ) {
			ret = "Invalid date supplied - cannot exit patient from care without a valid date.";
		}
		
		if ( p != null && c != null && exitDate != null ) {
			// need to check first if this is death or not
			String deathProp = Context.getAdministrationService().getGlobalProperty("concept.patientDied");
			Concept deathConcept = null;
			if ( deathProp != null ) {
				deathConcept = cs.getConceptByIdOrName(deathProp);
			}
			
			if ( deathConcept != null ) {
				if ( c.equals(deathConcept) ) {
					Concept causeConcept = null;
					try {
						causeConcept = cs.getConcept(causeOfDeath);
					} catch ( Exception e ) {
						causeConcept = null;
					}
					
					if ( causeConcept == null ) {
						ret = "Unable to locate cause of death in dictionary - cannot proceed";
					} else {
						try {
							ps.processDeath(p, exitDate, causeConcept, otherReason);
						} catch ( Exception e ) {
							log.debug("Caught error", e);
							ret = "Internal error while trying to process patient death - unable to proceed.";
						}
					}
				} else {
					try {
						ps.exitFromCare(p, exitDate, c);
					} catch ( Exception e ) {
						log.debug("Caught error", e);
						ret = "Internal error while trying to exit patient from care - unable to exit patient from care at this time.";
					}
				}
			} else {
				try {
					ps.exitFromCare(p, exitDate, c);
				} catch ( Exception e ) {
					log.debug("Caught error", e);
					ret = "Internal error while trying to exit patient from care - unable to exit patient from care at this time.";
				}
			}
			log.debug("Exited from care, it seems");
		}
		
		return ret;
	}
	
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
}
