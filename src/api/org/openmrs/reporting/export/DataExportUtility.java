package org.openmrs.reporting.export;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;

public class DataExportUtility {
	
	public final Log log = LogFactory.getLog(this.getClass());
	
	private Patient patient;
	
	private String separator = ",";
	private DateFormat dateFormatLong = null; 
	private DateFormat dateFormatShort = null; 
	
	private Encounter lastEncounter = null;
	
	private Set<Encounter> encounters = new TreeSet<Encounter>(new CompareEncounterDatetime());
	
	private Set<Obs> obs = new TreeSet<Obs>(new CompareObsDatetime());
	
	private Map<String, Concept> conceptNameMap = new HashMap<String, Concept>();

	private Context context;
	private EncounterService encounterService;
	private ObsService obsService;
	private PatientService patientService;
	
	// Constructors
	
	public DataExportUtility(Context c, Patient p) {
		this(c, p.getPatientId());
	}

	public DataExportUtility(Context c, Integer patientId) {
		this(c);
		setPatient(patientId);
	}
	
	public DataExportUtility(Context c) {
		this.context = c;
		this.obsService = context.getObsService();
		this.encounterService = context.getEncounterService();
		this.patientService = context.getPatientService();
		
		Locale locale = context.getLocale();
		dateFormatLong = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		dateFormatShort = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		
	}

	
	// getters and setters

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
		this.lastEncounter = null;
		this.encounters.clear();
		this.obs.clear();
	}

	public void setPatient(Integer patientId) {
		log.debug("setting patient: " + patientId);
		setPatient(patientService.getPatient(patientId));
	}
	
	/**
	 * @return Returns the separator.
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @param separator The separator to set.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * @return Returns the context.
	 */
	public Context getContext() {
		return context;
	}
	
	/**
	 * @return Encounter last encounter
	 */
	public Encounter getLastEncounter() {
		if (lastEncounter != null)
			return lastEncounter;
		
		for(Encounter e : getEncounters()) {
			lastEncounter = e;
			return lastEncounter;
		}
		
		return null;
	}
	
	
	
	
	// methods
	
	public Concept getConcept(String conceptName) throws Exception {
		if (conceptName == null)
			throw new Exception("conceptName cannot be null");
		
		if (conceptNameMap.containsKey(conceptName))
			return conceptNameMap.get(conceptName);
		
		Concept c = context.getConceptService().getConceptByName(conceptName);
		if (c == null)
			throw new Exception("A Concept with name '" + conceptName + "' was not found");
		
		conceptNameMap.put(conceptName, c);
		return c;
	}
	
	/**
	 * Get the patient's encounters.  Sorts the list on encounterDatetime
	 * @return
	 */
	public Set<Encounter> getEncounters() {
		if (encounters.size() == 0) {
			encounters.addAll(encounterService.getEncounters(patient));
		}
		return encounters;
	}
	
	public Set<Obs> getObs() {
		if (obs.size() == 0) {
			obs.addAll(obsService.getObservations(patient));
		}
		return obs;
	}
	
	/**
	 * Gets an Observation from the last encounter
	 * @param n max number of obs to return
	 * @param conceptName 
	 * @return
	 */
	public Collection<Obs> getLastNObs(Integer n, String conceptName) throws Exception {
		return getLastNObs(n, getConcept(conceptName));
	}
	
	/**
	 * Gets an Observation from the last encounter
	 * 
	 * @param n max number of obs to return
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public Collection<Obs> getLastNObs(Integer n, Concept concept) throws Exception {
		Collection<Obs> returnList = obsService.getLastNObservations(n, patient, concept);
		
		if (returnList == null)
			returnList = new Vector<Obs>();
		
		// bring the list size up to 'n'
		while (returnList.size() < n)
			returnList.add(new Obs());
		
		return returnList;
	}
	
	/**
	 * Gets the most recent Observation matching this concept
	 * @param conceptName
	 * @return
	 */
	public Obs getLastObs(String conceptName) throws Exception {
		return getLastObs(getConcept(conceptName));
	}
	
	/**
	 * Get the most recent obs matching <code>concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public Obs getLastObs(Concept concept) throws Exception {
		for (Obs o : getObs()) {
			if (concept.getConceptId().equals(o.getConcept().getConceptId())) {
				return o;
			}
		}
		
		log.info("Could not find an Obs with concept #" + concept + " for patient " + patient);
		
		return null;
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param conceptName 
	 * @return
	 */
	public Obs getFirstObs(String conceptName) throws Exception {
		return getFirstObs(getConcept(conceptName));
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public Obs getFirstObs(Concept concept) throws Exception {
		List<Obs> reverseObs = new Vector<Obs>();
		reverseObs.addAll(getObs());
		Collections.reverse(reverseObs);
		
		for (Obs o : reverseObs) {
			if (concept.getConceptId().equals(o.getConcept().getConceptId()))
				return o;
		}
		
		log.info("Could not find an Obs with concept " + concept + " for patient " + patient);
		
		return null;
	}
	
	/**
	 * Get all obs for the current patient that match this <code>obs.concept</code>=<code>concept>
	 * and <code>obs.valueCoded</code>=<code>valueCoded</code> 
	 * 
	 * @param conceptName
	 * @param valueCoded
	 * @return
	 * @throws Exception
	 */
	public Collection<Obs> getMatchingObs(String conceptName, String valueCoded) throws Exception {
		return getMatchingObs(getConcept(conceptName), getConcept(valueCoded));
	}
	
	/**
	 * Get all obs for the current patient that match this <code>obs.concept</code>=<code>concept>
	 * and <code>obs.valueCoded</code>=<code>valueCoded</code> 
	 * 
	 * @param concept
	 * @param valueCoded
	 * @return
	 * @throws Exception
	 */
	public Collection<Obs> getMatchingObs(Concept concept, Concept valueCoded) throws Exception {
		Collection<Obs> returnList = new Vector<Obs>();
		for (Obs o : getObs()) {
			if (concept.getConceptId().equals(o.getConcept().getConceptId()) && valueCoded.equals(o.getValueCoded()))
				returnList.add(o);
		}
		
		return returnList;
	}
	
	/**
	 * Format the given date according to the type ('short', 'long')
	 * @param type
	 * @param d
	 * @return
	 */
	public String formatDate(String type, Date d) {
		if (d == null)
			return "";
		
		if ("long".equals(type)) {
			return dateFormatLong.format(d);
		}
		else
			return dateFormatShort.format(d);
	}
	
	
	
	
	
	private class CompareEncounterDatetime implements Comparator<Encounter> {
		
		public int compare(Encounter enc1, Encounter enc2) throws ClassCastException {
			
			int value = 1;
			if (enc2.getEncounterDatetime().after(enc1.getEncounterDatetime()))
				value = -1;
			
			return value;
		}
	}
	
	private class CompareObsDatetime implements Comparator<Obs> {
		
		public int compare(Obs o1, Obs o2) throws ClassCastException {
			
			int value = 1;
			if (o2.getObsDatetime().after(o1.getObsDatetime()))
				value = -1;
			
			return value;
		}
	}
}