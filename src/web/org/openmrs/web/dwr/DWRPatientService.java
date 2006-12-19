package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.util.OpenmrsConstants;

public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findPatients(String searchValue, boolean includeVoided) {
		
		Vector<Object> patientList = new Vector<Object>();

		Integer userId = -1;
		if (Context.isAuthenticated())
			userId = Context.getAuthenticatedUser().getUserId();
		log.info(userId + "|" + searchValue);
		
		FormEntryService ps = Context.getFormEntryService();
		List<Patient> patients;
		
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
			List<Patient> newPatients = ps.findPatients(newSearch, includeVoided);
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
		FormEntryService ps = Context.getFormEntryService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		if (p.getAddresses() != null && p.getAddresses().size() > 0) {
			PatientAddress pa = (PatientAddress)p.getAddresses().toArray()[0];
			pli.setAddress1(pa.getAddress1());
			pli.setAddress2(pa.getAddress2());
		}
		return pli;
	}
	
	public Vector getSimilarPatients(String name, String birthyear, String age, String gender) {
		Vector<Object> patientList = new Vector<Object>();

		Integer userId = Context.getAuthenticatedUser().getUserId();
		log.info(userId + "|" + name + "|" + birthyear + "|" + age + "|" + gender);
		
		FormEntryService ps = Context.getFormEntryService();
		List<Patient> patients = new Vector<Patient>();
		
		Integer d = null;
		birthyear = birthyear.trim();
		age = age.trim();
		if (birthyear.length() > 3)
			d = Integer.valueOf(birthyear);
		else if (age.length() > 0) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			d = c.get(Calendar.YEAR);
			d = d - Integer.parseInt(age);
		}
		
		if (gender.length() < 1)
			gender = null;
		
		patients.addAll(ps.getSimilarPatients(name, d, gender));
		
		patientList = new Vector<Object>(patients.size());
		for (Patient p : patients) {
			patientList.add(new PatientListItem(p));
		}
		
		return patientList;

	}
	
	public Vector findTribes(String search) {
		Vector<Object> tribeList = new Vector<Object>();
		
		try {
			tribeList.addAll(Context.getFormEntryService().findTribes(search));
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
			tribeList.addAll(Context.getFormEntryService().getTribes());
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

	
	public void addIdentifier(Integer patientId, String identifierType, String identifier, Integer identifierLocationId) {
		if (identifier == null || identifier.length() == 0)
			return;
		PatientService ps = Context.getPatientService();
		EncounterService es = Context.getEncounterService();
		PatientIdentifierType idType = ps.getPatientIdentifierType(identifierType);
		Location location = es.getLocation(identifierLocationId);
		log.debug("idType=" + identifierType + "->" + idType + " , location=" + identifierLocationId + "->" + location + " identifier=" + identifier);
		Patient p = ps.getPatient(patientId);
		PatientIdentifier id = new PatientIdentifier();
		id.setIdentifierType(idType);
		id.setIdentifier(identifier);
		id.setLocation(location);
		p.addIdentifier(id);
		ps.updatePatient(p);
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
			String datePattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase());
			SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
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
	
}
