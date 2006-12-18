package org.openmrs.web.dwr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;

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
	
}
