package org.openmrs.reporting.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsUtil;

public class DataExportUtility {
	
	public final Log log = LogFactory.getLog(this.getClass());
	
	private Integer patientId;
	private Patient patient;
	private PatientSet patientSet;
	
	private String separator = "	";
	private DateFormat dateFormatLong = null; 
	private DateFormat dateFormatShort = null;
	private DateFormat dateFormatYmd = null;
	
	public Date currentDate = new Date();
	
	// Map<EncounterType, Map<patientId, Encounter>>
	private Map<String, Map<Integer, Encounter>> patientEncounterMap = new HashMap<String, Map<Integer, Encounter>>();
	
	// Map<EncounterType, Map<patientId, Encounter>>
	private Map<String, Map<Integer, Encounter>> patientFirstEncounterMap = new HashMap<String, Map<Integer, Encounter>>();
	
	private Map<String, Concept> conceptNameMap = new HashMap<String, Concept>();
	
	private Map<Concept, Map<Integer, List<Obs>>> conceptObsMap = new HashMap<Concept, Map<Integer, List<Obs>>>();
	
	// Map<RelationshipType, Map<patientId, List<Relationship>>>
	private Map<String, Map<Integer, List<Relationship>>> relationshipMap = new HashMap<String, Map<Integer, List<Relationship>>>();
	
	// Map<Program.name, Map<patientId, PatientProgram>>
	private Map<String, Map<Integer, PatientProgram>> programMap = new HashMap<String, Map<Integer, PatientProgram>>();
	
	// Map<name of drug set, Map<patientId, List<DrugOrder>>>
	private Map<String, Map<Integer, List<DrugOrder>>> drugOrderMap = new HashMap<String, Map<Integer, List<DrugOrder>>>();

	// Map<name of drug set, Map<patientId, List<DrugOrder>>>
	private Map<String, Map<Integer, List<DrugOrder>>> currentDrugOrderMap = new HashMap<String, Map<Integer, List<DrugOrder>>>();
	
	// Map<tablename+columnname, Map<patientId, columnvalue>>
	private Map<String, Map<Integer, Object>> attributeMap = new HashMap<String, Map<Integer, Object>>();
	
	private PatientSetService patientSetService;
	private PatientService patientService;
	
	// Constructors
	
	public DataExportUtility(Patient p) {
		this(p.getPatientId());
	}

	public DataExportUtility(Integer patientId) {
		this();
		setPatientId(patientId);
	}
	
	public DataExportUtility() {
		this.patientSetService = Context.getPatientSetService();
		this.patientService = Context.getPatientService();
		
		Locale locale = Context.getLocale();
		dateFormatLong = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		dateFormatShort = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		dateFormatYmd = new SimpleDateFormat("yyyy-MM-dd", locale);
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
		// remove last patient from maps to allow for garbage collection
		if (this.patientId != null) {
			for (Map<Integer, Encounter> map : patientEncounterMap.values())
				map.remove(this.patientId);
			for (Map<Integer, Encounter> map : patientFirstEncounterMap.values())
				map.remove(this.patientId);
			for (Map<Integer, List<Obs>> map : conceptObsMap.values())
				map.remove(this.patientId);
			for (Map<Integer, Object> map : attributeMap.values())
				map.remove(this.patientId);
		}
		
		setPatient(null);
		this.patientId = patientId;
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
	 * @return Encounter last encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getLastEncounter(String encounterType) {
		if (patientEncounterMap.containsKey(encounterType))
			return patientEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = Context.getEncounterService().getEncounterType(encounterType);
		
		Map<Integer, Encounter> encounterMap = patientSetService.getEncountersByType(getPatientSet(), type);
		
		patientEncounterMap.put(encounterType, encounterMap);
		
		return encounterMap.get(getPatientId());
	}

	/**
	 * @return Encounter first encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getFirstEncounter(String encounterType) {
		if (patientFirstEncounterMap.containsKey(encounterType))
			return patientFirstEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = Context.getEncounterService().getEncounterType(encounterType);
		
		Map<Integer, Encounter> encounterMap = patientSetService.getFirstEncountersByType(getPatientSet(), type);
		
		patientFirstEncounterMap.put(encounterType, encounterMap);
		
		return encounterMap.get(getPatientId());
	}
	
	
	
	// methods
	
	public Concept getConcept(String conceptName) throws Exception {
		if (conceptName == null)
			throw new Exception("conceptName cannot be null");
		
		if (conceptNameMap.containsKey(conceptName))
			return conceptNameMap.get(conceptName);
		
		log.debug("getting concept object for name: " + conceptName);
		
		Concept c = Context.getConceptService().getConceptByName(conceptName);
		if (c == null)
			throw new APIException("A Concept with name '" + conceptName + "' was not found");
		
		conceptNameMap.put(conceptName, c);
		return c;
	}
	
	public List<Obs> getObs(Concept c) {
		Map<Integer, List<Obs>> patientIdObsMap;
		if (conceptObsMap.containsKey(c)) {
			patientIdObsMap = conceptObsMap.get(c);
		}
		else {
			log.debug("getting obs list for concept: " + c);
			patientIdObsMap = Context.getPatientSetService().getObservations(getPatientSet(), c);
			conceptObsMap.put(c, patientIdObsMap);
		}
		return patientIdObsMap.get(patientId);
	}
	
	public PatientProgram getProgram(String programName) {
		Map<Integer, PatientProgram> patientIdProgramMap;
		if (programMap.containsKey(programName)) {
			patientIdProgramMap = programMap.get(programName);
		} else {
			Program program = Context.getProgramWorkflowService().getProgram(programName);
			patientIdProgramMap = Context.getPatientSetService().getPatientPrograms(getPatientSet(), program);
			programMap.put(programName, patientIdProgramMap);
		}
		return patientIdProgramMap.get(patientId);		
	}
	
	public List<DrugOrder> getCurrentDrugOrders(String drugSetName) {
		Map<Integer, List<DrugOrder>> patientIdDrugOrderMap;
		if (currentDrugOrderMap.containsKey(drugSetName)) {
			patientIdDrugOrderMap = currentDrugOrderMap.get(drugSetName);
		} else {
			Concept drugSet = Context.getConceptService().getConceptByName(drugSetName);
			patientIdDrugOrderMap = Context.getPatientSetService().getCurrentDrugOrders(getPatientSet(), drugSet);
			currentDrugOrderMap.put(drugSetName, patientIdDrugOrderMap);
		}
		return patientIdDrugOrderMap.get(patientId);
	}
	
	public String getCurrentDrugNames(String drugSetName) {
		List<DrugOrder> patientOrders = getCurrentDrugOrders(drugSetName);
		if (patientOrders == null)
			return "";
		StringBuilder ret = new StringBuilder();
		for (Iterator<DrugOrder> i = patientOrders.iterator(); i.hasNext(); ) {
			DrugOrder o = i.next();
			ret.append(o.getDrug().getName());
			if (i.hasNext())
				ret.append(" ");
		}
		return ret.toString();
	}
	
	public List<DrugOrder> getDrugOrders(String drugSetName) {
		Map<Integer, List<DrugOrder>> patientIdDrugOrderMap;
		if (drugOrderMap.containsKey(drugSetName)) {
			patientIdDrugOrderMap = drugOrderMap.get(drugSetName);
		} else {
			Concept drugSet = Context.getConceptService().getConceptByName(drugSetName);
			patientIdDrugOrderMap = Context.getPatientSetService().getCurrentDrugOrders(getPatientSet(), drugSet);
			drugOrderMap.put(drugSetName, patientIdDrugOrderMap);
		}
		return patientIdDrugOrderMap.get(patientId);
	}
	
	public Date getEarliestDrugStart(String drugSetName) {
		List<DrugOrder> patientOrders = getDrugOrders(drugSetName);
		if (patientOrders == null)
			return null;
		Date earliest = null;
		for (DrugOrder o : patientOrders) {
			if (earliest == null || OpenmrsUtil.compareWithNullAsLatest(o.getStartDate(), earliest) < 0)
				earliest = o.getStartDate();
		}
		return earliest;
	}
	
	public List<Relationship> getRelationships(String relationshipTypeName) {
		Map<Integer, List<Relationship>> patientIdRelationshipMap;
		if (relationshipMap.containsKey(relationshipTypeName)) {
			patientIdRelationshipMap = relationshipMap.get(relationshipTypeName);
		} else {
			log.debug("getting relationship list for type: " + relationshipTypeName);
			RelationshipType relType = Context.getPatientService().findRelationshipType(relationshipTypeName);
			patientIdRelationshipMap = Context.getPatientSetService().getRelationships(getPatientSet(), relType);
			relationshipMap.put(relationshipTypeName, patientIdRelationshipMap);
		}
		return patientIdRelationshipMap.get(patientId);
	}
	
	// TODO: revisit this if we change our terminology for relationships
	public String getRelationshipNames(String relationshipTypeName) {
		List<Relationship> rels = getRelationships(relationshipTypeName);
		if (rels == null || rels.size() == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext(); ) {
				Relationship r = i.next();
				if (r.getPerson().getUser() != null) {
					sb.append(r.getPerson().getUser().toString());
				} else {
					sb.append(r.getPerson().getPatient().getPatientName());
				}
				if (i.hasNext())
					sb.append(" ");
			}
			return sb.toString();
		}
	}
	
	// TODO: revisit this if we change our terminology for relationships
	public String getRelationshipIds(String relationshipTypeName) {
		List<Relationship> rels = getRelationships(relationshipTypeName);
		if (rels == null || rels.size() == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext(); ) {
				Relationship r = i.next();
				if (r.getPerson().getUser() != null) {
					sb.append("User " + r.getPerson().getUser().getUserId());
				} else {
					sb.append("Patient " + r.getPerson().getPatient().getPatientId());
				}
				if (i.hasNext())
					sb.append(" ");
			}
			return sb.toString();
		}
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
			log.debug("getting patient attrs: " + key);
			patientIdAttrMap = Context.getPatientSetService().getPatientAttributes(patientSet, className, property, returnAll);
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
		List<Obs> returnList = getObs(concept);
		
		//log.debug("Got obs list. size: " + returnList.size());
		if (returnList == null)
			returnList = new Vector<Obs>();
		
		// bring the list size up to 'n'
		while (returnList.size() < n)
			returnList.add(new Obs());
		
		List<Obs> rList = returnList.subList(0, n);

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
	 * Get all obs for the current patient that match this <code>obs.concept</code>=<code>concept</code>
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
	 * Get all obs for the current patient that match this <code>obs.concept</code>=<code>concept</code>
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
	 * Format the given date according to the type ('short', 'long', 'ymd')
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
		else if ("ymd".equals(type))
			return dateFormatYmd.format(d);
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
			return OpenmrsUtil.isValidCheckDigit(id);
		}
		catch (Exception e) {
			log.error("Error evaluating identifier during report", e);
			return false;
		}
	}

}