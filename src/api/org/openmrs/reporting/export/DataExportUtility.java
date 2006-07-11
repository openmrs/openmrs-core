package org.openmrs.reporting.export;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.Helper;

public class DataExportUtility {
	
	public final Log log = LogFactory.getLog(this.getClass());
	
	private Integer patientId;
	private Patient patient;
	private PatientSet patientSet;
	
	private String separator = "	";
	private DateFormat dateFormatLong = null; 
	private DateFormat dateFormatShort = null; 
	
	public Date currentDate = new Date();
	
	// Map<EncounterType, Map<patientId, Encounter>>
	private Map<String, Map<Integer, Encounter>> patientEncounterMap = new HashMap<String, Map<Integer, Encounter>>();
	
	private Map<String, Concept> conceptNameMap = new HashMap<String, Concept>();
	
	private Map<Concept, Map<Integer, List<Obs>>> conceptObsMap = new HashMap<Concept, Map<Integer, List<Obs>>>();
	
	// Map<tablename+columnname, Map<patientId, columnvalue>>
	private Map<String, Map<Integer, Object>> attributeMap = new HashMap<String, Map<Integer, Object>>();
	
	private Context context;
	private PatientSetService patientSetService;
	private PatientService patientService;
	
	// Constructors
	
	public DataExportUtility(Context c, Patient p) {
		this(c, p.getPatientId());
	}

	public DataExportUtility(Context c, Integer patientId) {
		this(c);
		setPatientId(patientId);
	}
	
	public DataExportUtility(Context c) {
		this.context = c;
		this.patientSetService = context.getPatientSetService();
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
		if (patient == null)
			patient = patientService.getPatient(patientId);
		
		return patient;
	}
	
	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		long t = new Date().getTime();
		setPatient(null);
		this.patientId = patientId;
		log.debug("execution time: " + (new Date().getTime() - t));
	}
	
	/**
	 * @return Returns the patientSet.
	 */
	public PatientSet getPatientSet() {
		return patientSet;
	}
	
	/**
	 * @param patientSet The patientSet to set.
	 */
	public void setPatientSet(PatientSet patientSet) {
		this.patientSet = patientSet;
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
	 * @return Encounter last encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getLastEncounter(String encounterType) {
		if (patientEncounterMap.containsKey(encounterType))
			return patientEncounterMap.get(encounterType).get(getPatientId());
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = context.getEncounterService().getEncounterType(encounterType);
		
		Map<Integer, Encounter> encounterMap = patientSetService.getEncountersByType(getPatientSet(), type);
		
		patientEncounterMap.put(encounterType, encounterMap);
		
		return encounterMap.get(getPatientId());
	}
	
	
	
	
	// methods
	
	public Concept getConcept(String conceptName) throws Exception {
		long t = new Date().getTime();
		if (conceptName == null)
			throw new Exception("conceptName cannot be null");
		
		if (conceptNameMap.containsKey(conceptName)) {
			Concept c = conceptNameMap.get(conceptName);
			log.debug("execution time: " + (new Date().getTime() - t));
			return c;
		}
		
		Concept c = context.getConceptService().getConceptByName(conceptName);
		if (c == null)
			throw new Exception("A Concept with name '" + conceptName + "' was not found");
		
		conceptNameMap.put(conceptName, c);
		log.debug("execution time: " + (new Date().getTime() - t));
		return c;
	}
	
	public List<Obs> getObs(Concept c) {
		Map<Integer, List<Obs>> patientIdObsMap;
		if (conceptObsMap.containsKey(c)) {
			patientIdObsMap = conceptObsMap.get(c);
		}
		else {
			patientIdObsMap = context.getPatientSetService().getObservations(patientSet, c);
			conceptObsMap.put(c, patientIdObsMap);
		}
		return patientIdObsMap.get(patientId);
	}
	
	/**
	 * Retrieves properties on the patient like patient.patientName.familyName
	 * 
	 * @param className
	 * @param property
	 * @return
	 */
	public Object getPatientAttr(String className, String property) {
		return getPatientAttr(className, property, false);
	}
	
	/**
	 * Retrieves properties on the patient like patient.patientName.familyName
	 * If returnAll is set, returns an array of every matching property for 
	 * the patient instead of just the preferred one
	 * 
	 * @param className
	 * @param property
	 * @param returnAll
	 * @return
	 */
	public Object getPatientAttr(String className, String property, boolean returnAll) {
		String key = className + "." + property;
		
		if (returnAll)
			key += "--all";
		
		Map<Integer, Object> patientIdAttrMap;
		if (attributeMap.containsKey(key)) {
			patientIdAttrMap = attributeMap.get(key);
		}
		else {
			patientIdAttrMap = context.getPatientSetService().getPatientAttributes(patientSet, className, property, returnAll);
			attributeMap.put(key, patientIdAttrMap);
		}
		return patientIdAttrMap.get(patientId);
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
	public List<Obs> getLastNObs(Integer n, Concept concept) throws Exception {
		long t = new Date().getTime();
		
		List<Obs> returnList = getObs(concept);
		
		if (returnList == null)
			returnList = new Vector<Obs>();
		
		// bring the list size up to 'n'
		while (returnList.size() < n)
			returnList.add(new Obs());
		
		List<Obs> rList = returnList.subList(0, n);
		log.debug("execution time: " + (new Date().getTime() - t));
		return rList;
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
		List<Obs> obs = getLastNObs(1, concept);
		
		return obs.get(0);
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
		reverseObs.addAll(getObs(concept));
		Collections.reverse(reverseObs);
		
		for (Obs o : reverseObs) {
			if (concept.getConceptId().equals(o.getConcept().getConceptId()))
				return o;
		}
		
		log.info("Could not find an Obs with concept " + concept + " for patient " + patientId);
		
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
		for (Obs o : getObs(concept)) {
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
	
	/**
	 * Check the given string against the check digit algorithm
	 * @param id
	 * @return true/false whether the string has a valid check digit
	 */
	public boolean isValidCheckDigit(String id) {
		try {
			return Helper.isValidCheckDigit(id);
		}
		catch (Exception e) {
			log.error("Error evaluating identifier during report", e);
			return false;
		}
	}

}