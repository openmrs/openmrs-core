package org.openmrs.reporting.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class DataExportFunctions {
	
	public final Log log = LogFactory.getLog(this.getClass());
	
	protected Integer patientId;
	protected Patient patient;
	protected PatientSet patientSet;
	private Integer patientCounter = 0; // used for garbage collection (Clean up every x patients)
	
	protected String separator = "	";
	protected DateFormat dateFormatLong = null; 
	protected DateFormat dateFormatShort = null;
	protected DateFormat dateFormatYmd = null;
	protected Map<String, DateFormat> formats = new HashMap<String, DateFormat>();
	
	public Date currentDate = new Date();
	
	// Map<EncounterType, Map<patientId, Encounter>>
	protected Map<String, Map<Integer, ?>> patientEncounterMap = new HashMap<String, Map<Integer, ?>>();
	
	// Map<EncounterType, Map<patientId, Encounter>>
	protected Map<String, Map<Integer, ?>> patientFirstEncounterMap = new HashMap<String, Map<Integer, ?>>();
	
	protected Map<String, Concept> conceptNameMap = new HashMap<String, Concept>();
	
	// Map<Concept, Map<obsId, List<Obs values>>>
	//protected Map<Concept, Map<Integer, List<Object>>> conceptObsMap = new HashMap<Concept, Map<Integer, List<Object>>>();
	
	// Map<conceptId + attr, Map<patientId, List<List<Obs values>>>>
	protected Map<String, Map<Integer, List<List<Object>>>> conceptAttrObsMap = new HashMap<String, Map<Integer, List<List<Object>>>>();
	
	// Map<RelationshipType, Map<patientId, List<Relationship>>>
	protected Map<String, Map<Integer, List<Relationship>>> relationshipMap = new HashMap<String, Map<Integer, List<Relationship>>>();
	
	// Map<Program.name, Map<patientId, PatientProgram>>
	protected Map<String, Map<Integer, PatientProgram>> programMap = new HashMap<String, Map<Integer, PatientProgram>>();
	
	// Map<name of drug set, Map<patientId, List<DrugOrder>>>
	protected Map<String, Map<Integer, List<DrugOrder>>> drugOrderMap = new HashMap<String, Map<Integer, List<DrugOrder>>>();

	// Map<name of drug set, Map<patientId, List<DrugOrder>>>
	protected Map<String, Map<Integer, List<DrugOrder>>> currentDrugOrderMap = new HashMap<String, Map<Integer, List<DrugOrder>>>();
	
	// Map<tablename+columnname, Map<patientId, columnvalue>>
	protected Map<String, Map<Integer, Object>> patientAttributeMap = new HashMap<String, Map<Integer, Object>>();
	
	// Map<tablename+columnname, Map<personId, columnvalue>>
	protected Map<String, Map<Integer, Object>> personAttributeMap = new HashMap<String, Map<Integer, Object>>();
	
	
	protected PatientSetService patientSetService;
	protected PatientService patientService;
	protected ConceptService conceptService;
	protected EncounterService encounterService;
	
	protected Locale locale = null;
	// Constructors
	
	public DataExportFunctions(Patient p) {
		this(p.getPatientId());
	}

	public DataExportFunctions(Integer patientId) {
		this();
		setPatientId(patientId);
	}
	
	public DataExportFunctions() {
		this.patientSetService = Context.getPatientSetService();
		this.patientService = Context.getPatientService();
		this.conceptService = Context.getConceptService();
		this.encounterService = Context.getEncounterService();
		
		locale = Context.getLocale();
		dateFormatLong = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		String format = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(locale.toString().toLowerCase());
		if (format == null)
			format = "dd-MM-yyyy";
		dateFormatShort = new SimpleDateFormat(format, locale);
		dateFormatYmd = new SimpleDateFormat("yyyy-MM-dd", locale);
	}
	
	public void clear() {
		for (Map map : patientEncounterMap.values())
			map.clear();
		patientEncounterMap.clear();
		patientEncounterMap = null;
		for (Map map : patientFirstEncounterMap.values())
			map.clear();
		patientFirstEncounterMap.clear();
		patientFirstEncounterMap = null;
		conceptNameMap.clear();
		for (Map map : conceptAttrObsMap.values())
			map.clear();
		conceptAttrObsMap.clear();
		for (Map map : relationshipMap.values())
			map.clear();
		relationshipMap.clear();
		for (Map map : programMap.values())
			map.clear();
		programMap.clear();
		for (Map map : drugOrderMap.values())
			map.clear();
		drugOrderMap.clear();
		for (Map map : currentDrugOrderMap.values())
			map.clear();
		currentDrugOrderMap.clear();
		currentDrugOrderMap = null;
		for (Map map : patientAttributeMap.values())
			map.clear();
		patientAttributeMap.clear();
		patientAttributeMap = null;
		
		patientSetService = null;
		patientService = null;
		conceptService = null;
		encounterService = null;
	}
	
	/**
	 * Called when garbage collecting this class
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		log.error("GC is collecting the data export functions..." + this);
		super.finalize();
	}

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
			for (Map<Integer, ?> map : patientEncounterMap.values())
				map.remove(this.patientId);
			for (Map<Integer, ?> map : patientFirstEncounterMap.values())
				map.remove(this.patientId);
			for (Map<Integer, Object> map : patientAttributeMap.values())
				map.remove(this.patientId);
		}
		
		// reclaim some memory
		garbageCollect();
		
		setPatient(null);
		this.patientId = patientId;
	}
	
	/**
	 * Call the system garbage collecter.  This method only calls 
	 * every 500 patients
	 */
	protected void garbageCollect() {
		if (patientCounter++ % 500 == 0) {
			System.gc();
			System.gc();
		}
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
			return (Encounter)patientEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = encounterService.getEncounterType(encounterType);
		
		Map<Integer, ?> encounterMap = patientSetService.getEncountersByType(getPatientSet(), type);
		
		patientEncounterMap.put(encounterType, encounterMap);
		
		return (Encounter)encounterMap.get(getPatientId());
	}
	
	/**
	 * 
	 * @param typeArray
	 * @param attr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getLastEncounterAttr(Object typeArray, String attr) {
		
		List<String> types = (List<String>)typeArray;
		String key = OpenmrsUtil.join(types, ",") + "|" + attr;
		
		if (patientEncounterMap.containsKey(key))
			return patientEncounterMap.get(key).get(getPatientId());
		
		log.debug("getting first encounters for type: " + key);
		
		List<EncounterType> encounterTypes = new Vector<EncounterType>();
		
		// find the EncounterType objects for each type passed in
		for (String typeName : types) {
			EncounterType type = null;
			try {
				type = encounterService.getEncounterType(Integer.valueOf(typeName));
			}
			catch (Exception e) { /* pass */ };
			
			if (type == null)
				type = encounterService.getEncounterType(typeName);
			
			if (type != null)
				encounterTypes.add(type);
		}
		
		Map<Integer, Object> encounterMap = patientSetService.getEncounterAttrsByType(getPatientSet(), encounterTypes, attr);
		
		patientEncounterMap.put(key, encounterMap);
		
		return encounterMap.get(getPatientId());
		
	}

	/**
	 * @return Encounter first encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getFirstEncounter(String encounterType) {
		if (patientFirstEncounterMap.containsKey(encounterType))
			return (Encounter)patientFirstEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = encounterService.getEncounterType(encounterType);
		
		Map<Integer, Encounter> encounterMap = patientSetService.getFirstEncountersByType(getPatientSet(), type);
		
		patientFirstEncounterMap.put(encounterType, encounterMap);
		
		return encounterMap.get(getPatientId());
	}
	
	/**
	 * 
	 * @param typeArray
	 * @param attr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getFirstEncounterAttr(Object typeArray, String attr) {
		
		List<String> types = (List<String>)typeArray;
		String key = OpenmrsUtil.join(types, ",") + "|" + attr;
		
		if (patientFirstEncounterMap.containsKey(key))
			return patientFirstEncounterMap.get(key).get(getPatientId());
		
		log.debug("getting first encounters for type: " + key);
		
		List<EncounterType> encounterTypes = new Vector<EncounterType>();
		
		// find the EncounterType objects for each type passed in
		for (String typeName : types) {
			EncounterType type = null;
			try {
				type = encounterService.getEncounterType(Integer.valueOf(typeName));
			}
			catch (Exception e) { /* pass */ };
			
			if (type == null)
				type = encounterService.getEncounterType(typeName);
			
			if (type != null)
				encounterTypes.add(type);
		}
		
		Map<Integer, Object> encounterMap = patientSetService.getFirstEncounterAttrsByType(getPatientSet(), encounterTypes, attr);
		
		patientFirstEncounterMap.put(key, encounterMap);
		
		return encounterMap.get(getPatientId());
		
	}
	
	
	
	// methods
	
	public Concept getConcept(String conceptName) throws Exception {
		if (conceptName == null)
			throw new Exception("conceptName cannot be null");
		
		if (conceptNameMap.containsKey(conceptName))
			return conceptNameMap.get(conceptName);
		
		//log.debug("getting concept object for name: " + conceptName);
		
		Concept c;
		try {
			Integer conceptId = Integer.valueOf(conceptName);
			c = conceptService.getConcept(conceptId);
		}
		catch (NumberFormatException e) {
			c = conceptService.getConceptByName(conceptName);
		}
		
		if (c == null)
			throw new APIException("A Concept with name or id '" + conceptName + "' was not found");
		
		conceptNameMap.put(conceptName, c);
		return c;
	}
	
	/*
	public List<Object> getObs(Concept c) {
		Map<Integer, List<Object>> patientIdObsMap;
		if (conceptObsMap.containsKey(c)) {
			patientIdObsMap = conceptObsMap.get(c);
		}
		else {
			log.debug("getting obs list for concept: " + c);
			patientIdObsMap = Context.getPatientSetService().getObservationsValues(getPatientSet(), c);
			conceptObsMap.put(c, patientIdObsMap);
		}
		return patientIdObsMap.get(patientId);
	}
	*/
	
	public List<List<Object>> getObsWithValues(Concept c, List<String> attrs) {
		if (attrs == null)
			attrs = new Vector<String>();
		
		String key = c.getConceptId() + "";
		Map<Integer, List<List<Object>>> patientIdObsMap = conceptAttrObsMap.get(key);
		if (patientIdObsMap == null) {
			//log.debug("getting obs list for concept: " + c + " and attr: " + attr);
			patientIdObsMap = patientSetService.getObservationsValues(getPatientSet(), c, attrs);
			conceptAttrObsMap.put(key, patientIdObsMap);
		}
		return patientIdObsMap.get(patientId);
	}
	
	public PatientProgram getProgram(String programName) {
		Map<Integer, PatientProgram> patientIdProgramMap;
		if (programMap.containsKey(programName)) {
			patientIdProgramMap = programMap.get(programName);
		} else {
			Program program = Context.getProgramWorkflowService().getProgram(programName);
			patientIdProgramMap = patientSetService.getPatientPrograms(getPatientSet(), program);
			programMap.put(programName, patientIdProgramMap);
		}
		return patientIdProgramMap.get(patientId);		
	}
	
	public List<DrugOrder> getCurrentDrugOrders(String drugSetName) {
		Map<Integer, List<DrugOrder>> patientIdDrugOrderMap;
		if (currentDrugOrderMap.containsKey(drugSetName)) {
			patientIdDrugOrderMap = currentDrugOrderMap.get(drugSetName);
		} else {
			Concept drugSet = conceptService.getConceptByName(drugSetName);
			patientIdDrugOrderMap = patientSetService.getCurrentDrugOrders(getPatientSet(), drugSet);
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
			Concept drugSet = conceptService.getConceptByName(drugSetName);
			patientIdDrugOrderMap = patientSetService.getCurrentDrugOrders(getPatientSet(), drugSet);
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
			//log.debug("getting relationship list for type: " + relationshipTypeName);
			RelationshipType relType = Context.getPersonService().findRelationshipType(relationshipTypeName);
			patientIdRelationshipMap = patientSetService.getRelationships(getPatientSet(), relType);
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
				sb.append(r.getPersonA().toString());
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
				r.getPersonA().toString();
				if (i.hasNext())
					sb.append(" ");
			}
			return sb.toString();
		}
	}
	
	public String getRelationshipIdentifiers(String relationshipTypeName) {
		List<Relationship> rels = getRelationships(relationshipTypeName);
		if (rels == null || rels.size() == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext(); ) {
				Relationship r = i.next();
				Patient p = patientService.getPatient(r.getPersonA().getPersonId());
				if (p != null)
					sb.append("Patient " + p.getPatientIdentifier());
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
		if (patientAttributeMap.containsKey(key)) {
			patientIdAttrMap = patientAttributeMap.get(key);
		}
		else {
			//log.debug("getting patient attrs: " + key);
			patientIdAttrMap = patientSetService.getPatientAttributes(patientSet, className, property, returnAll);
			patientAttributeMap.put(key, patientIdAttrMap);
		}
		return patientIdAttrMap.get(patientId);
	}
	
	public Object getPersonAttribute(String attributeName, String joinClass, String joinProperty, String outputColumn, boolean returnAll) {
		String key = attributeName + "." + joinClass + "." + joinProperty;
		
		if (returnAll)
			key += "--all";
		
		Map<Integer, Object> personIdAttrMap;
		if (personAttributeMap.containsKey(key)) {
			personIdAttrMap = personAttributeMap.get(key);
		}
		else {
			//log.debug("getting patient attrs: " + key);
			personIdAttrMap = patientSetService.getPersonAttributes(patientSet, attributeName, joinClass, joinProperty, outputColumn, returnAll);
			personAttributeMap.put(key, personIdAttrMap);
		}
		return personIdAttrMap.get(patientId);
	}
	
	public Object getPersonAttribute(String attributeName) {
		return getPersonAttribute(attributeName, null, null, null, false);
	}
	
	/**
	 * Gets the last <code>n</code> obs for <code>conceptName</code>
	 * @param n max number of obs to return
	 * @param conceptName 
	 * @return
	 */
	public List<Object> getLastNObs(Integer n, String conceptName) throws Exception {
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
	public List<Object> getLastNObs(Integer n, Concept concept) throws Exception {
		List<Object> returnList = new Vector<Object>();
		for (List<Object> row : getLastNObsWithValues(n, concept, null)) {
			returnList.add(row.get(0));
		}
		return returnList;
	}
	
	public List<List<Object>> getLastNObsWithValues(Integer n, String conceptId, Object attrs) throws Exception {
		return getLastNObsWithValues(n, getConcept(conceptId), (List<String>)attrs);
	}
	
	/**
	 * Gets the most recent observation value 
	 * 
	 * @param n max number of obs to return
	 * @param concept
	 * @param attrs
	 * @return
	 * @throws Exception
	 */
	public List<List<Object>> getLastNObsWithValues(Integer n, Concept concept, List<String> attrs) throws Exception {
		//log.debug("Looking for n concepts: " + n + " " + concept);
		
		if (attrs == null)
			attrs = new Vector<String>();
		
		attrs.add(0, null);
		
		List<List<Object>> returnList = getObsWithValues(concept, attrs);
		
		if (returnList == null)
			returnList = new Vector<List<Object>>();
		
		//log.debug("Got obs list for: " + concept.getConceptId() + ". size: " + returnList.size());
		
		if (n.equals(-1))
			return returnList;
		
		// bring the list size up to 'n'
		List<Object> blankRow = new Vector<Object>();
		for (String attr : attrs)
			blankRow.add("");
		while (returnList.size() < n)
			returnList.add(blankRow);
		
		List<List<Object>> rList = returnList.subList(0, n);
		
		//for (Object o : rList)
		//	log.debug("rList object: " + o);
		
		return rList;
	}
	
	/**
	 * Gets the most recent Observation matching this concept
	 * @param conceptName
	 * @return
	 */
	public Object getLastObs(String conceptName) throws Exception {
		return getLastObs(getConcept(conceptName));
	}
	
	/**
	 * Gets the most recent Observation value matching this concept
	 * @param conceptName
	 * @param attrs string array
	 * @return
	 */
	public List<Object> getLastObsWithValues(String conceptName, Object attrs) throws Exception {
		//List<String> attrs = new Vector<String>();
		//Collections.addAll(attrs, attrArray);
		return getLastObsWithValues(getConcept(conceptName), (List<String>)attrs);
	}
	
	/**
	 * Get the most recent obs matching <code>concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public Object getLastObs(Concept concept) throws Exception {
		List<Object> obs = getLastNObs(1, concept);
		
		return obs.get(0);
	}
	
	/**
	 * Get the most recent obs matching <code>concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @param attr list of Strings
	 * @return
	 * @throws Exception
	 */
	public List<Object> getLastObsWithValues(Concept concept, List<String> attrs) throws Exception {
		List<List<Object>> obs = getLastNObsWithValues(1, concept, attrs);
		
		return obs.get(0);
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param conceptName 
	 * @return
	 */
	public Object getFirstObs(String conceptName) throws Exception {
		return getFirstObs(getConcept(conceptName));
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public Object getFirstObs(Concept concept) throws Exception {
		List<List<Object>> obs = getObsWithValues(concept, null);
		
		for (int x = obs.size() - 1; x >= 0; x--) {
			List<Object> o = obs.get(x);
			return o.get(0);
		}
		
		log.info("Could not find an Obs with concept " + concept + " for patient " + patientId);
		
		return null;
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param conceptName 
	 * @return
	 */
	public List<Object> getFirstObsWithValues(String conceptName, Object attrs) throws Exception {
		//List<String> attrs = new Vector<String>();
		//Collections.addAll(attrs, attrArray);
		return getFirstObsWithValues(getConcept(conceptName), (List<String>)attrs);
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * @param e
	 * @param concept
	 * @return
	 * @throws Exception
	 */
	public List<Object> getFirstObsWithValues(Concept concept, List<String> attrs) throws Exception {
		List<List<Object>> obs = getObsWithValues(concept, attrs);
		
		if (obs == null) {
			List<Object> blankRow = new Vector<Object>();
			for (String attr : attrs)
				blankRow.add("");
			return blankRow;
		}
		
		for (int x = obs.size() - 1; x >= 0; x--) {
			List<Object> o = obs.get(x);
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
//	public Collection<Obs> getMatchingObs(String conceptName, String valueCoded) throws Exception {
//		return getMatchingObs(getConcept(conceptName), getConcept(valueCoded));
//	}
	
	/**
	 * Get all obs for the current patient that match this <code>obs.concept</code>=<code>concept</code>
	 * and <code>obs.valueCoded</code>=<code>valueCoded</code> 
	 * 
	 * @param concept
	 * @param valueCoded
	 * @return
	 * @throws Exception
	 */
//	public Collection<Obs> getMatchingObs(Concept concept, Concept valueCoded) throws Exception {
//		Collection<Obs> returnList = new Vector<Obs>();
//		for (Obs o : getObs(concept)) {
//			if (concept.getConceptId().equals(o.getConcept().getConceptId()) && valueCoded.equals(o.getValueCoded()))
//				returnList.add(o);
//		}
//		
//		return returnList;
//	}
	
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
		else if ("short".equals(type) || type == null)
			return dateFormatShort.format(d);
		else {
			if (formats.containsKey(type))
				return formats.get(type).format(d);
			else {
				DateFormat df = new SimpleDateFormat(type, locale);
				formats.put(type, df);
				return df.format(d);
			}
		}
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
	
	/**
	 * @return results of o.hashCode()
	 */
	public Integer hashCode(Object o) {
		if (o == null)
			return null;
		return o.hashCode();
	}
	
	public String getValueAsString(Object o) {
		if (o == null)
			return "";
		
		else if (o instanceof Concept)
			return ((Concept)o).getName().toString();
		else if (o instanceof Drug)
			return ((Drug)o).getName();
		else if (o instanceof Location)
			return ((Location)o).getName();
		else if (o instanceof User)
			return ((User)o).toString();
		else if (o instanceof EncounterType)
			return ((EncounterType)o).getName();
		else if (o instanceof Date)
			return formatDate(null, (Date)o);
		else
			return o.toString();
	}
	
}