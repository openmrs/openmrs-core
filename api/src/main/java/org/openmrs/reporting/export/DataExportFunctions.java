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
package org.openmrs.reporting.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
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
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class DataExportFunctions {
	
	public final Log log = LogFactory.getLog(this.getClass());
	
	protected Integer patientId;
	
	protected Patient patient;
	
	//protected PatientSet patientSet;
	protected Cohort patientSet;
	
	protected boolean isAllPatients = false;
	
	private Integer patientCounter = 0; // used for garbage collection (Clean up every x patients)
	
	protected String separator = "	";
	
	protected DateFormat dateFormatLong = null;
	
	protected DateFormat dateFormatShort = null;
	
	protected DateFormat dateFormatYmd = null;
	
	protected Map<String, DateFormat> formats = new HashMap<String, DateFormat>();
	
	public Date currentDate = new Date();
	
	protected Calendar calendar = null;
	
	// Map<EncounterType, Map<patientId, Encounter>>
	protected Map<String, Map<Integer, ?>> patientEncounterMap = new HashMap<String, Map<Integer, ?>>();
	
	// Map<PatientIdentifierType, Map<patientId, PatientIdentifier>>
	protected Map<String, Map<Integer, PatientIdentifier>> patientIdentifierMap = new HashMap<String, Map<Integer, PatientIdentifier>>();
	
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
	
	// Map<key, Collection<personId>>, where key is like "Cohort.1" or "Filter.3"
	protected Map<String, Collection<Integer>> cohortMap = new HashMap<String, Collection<Integer>>();
	
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
		dateFormatShort = Context.getDateFormat();
		dateFormatYmd = new SimpleDateFormat("yyyy-MM-dd", locale);
	}
	
	@SuppressWarnings("unchecked")
	public void clear() {
		for (Map map : patientEncounterMap.values())
			map.clear();
		patientEncounterMap.clear();
		patientEncounterMap = null;
		for (Map map : patientIdentifierMap.values())
			map.clear();
		patientIdentifierMap.clear();
		patientIdentifierMap = null;
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
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		log.debug("GC is collecting the data export functions..." + this);
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
			for (Map<Integer, ?> map : patientIdentifierMap.values())
				map.remove(this.patientId);
		}
		
		// reclaim some memory
		garbageCollect();
		
		setPatient(null);
		this.patientId = patientId;
	}
	
	/**
	 * Call the system garbage collecter. This method only calls every 500 patients
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
	public Cohort getPatientSet() {
		return patientSet;
	}
	
	/**
	 * @param patientSet The patientSet to set.
	 */
	public void setPatientSet(Cohort patientSet) {
		this.patientSet = patientSet;
	}
	
	/**
	 * @return the isAllPatients
	 */
	public boolean isAllPatients() {
		return isAllPatients;
	}
	
	/**
	 * @param isAllPatients the isAllPatients to set
	 */
	public void setAllPatients(boolean isAllPatients) {
		this.isAllPatients = isAllPatients;
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
	
	public String getCohortMembership(Integer cohortId, String valueIfTrue, String valueIfFalse) {
		return getCohortHelper("C." + cohortId) ? valueIfTrue : valueIfFalse;
	}
	
	public String getCohortDefinitionMembership(Integer filterId, String valueIfTrue, String valueIfFalse) {
		return getCohortHelper("F." + filterId) ? valueIfTrue : valueIfFalse;
	}
	
	public String getPatientSearchMembership(Integer searchId, String valueIfTrue, String valueIfFalse) {
		return getCohortHelper("S." + searchId) ? valueIfTrue : valueIfFalse;
	}
	
	protected Boolean getCohortHelper(String key) {
		if (cohortMap.containsKey(key))
			return cohortMap.get(key).contains(getPatientId());
		
		//TODO try to cache the evaluation context
		EvaluationContext context = new EvaluationContext();
		
		log.debug("getting cohort/definition for key: " + key);
		//PatientSet ps = null;
		Cohort ps = null;
		if (key.startsWith("C.")) {
			ps = Context.getCohortService().getCohort(Integer.valueOf(key.substring(2)));
		} else if (key.startsWith("F.")) {
			PatientFilter pf = Context.getReportObjectService().getPatientFilterById(Integer.valueOf(key.substring(2)));
			ps = pf.filter(getPatientSet(), context);
		} else if (key.startsWith("S.")) {
			PatientSearchReportObject ro = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(
			    Integer.valueOf(key.substring(2)));
			PatientFilter pf = OpenmrsUtil.toPatientFilter(ro.getPatientSearch(), null);
			ps = pf.filter(getPatientSet(), context);
		} else {
			log.error("key = " + key);
		}
		Set<Integer> set = new HashSet<Integer>(ps.getMemberIds());
		cohortMap.put(key, set);
		
		return set.contains(getPatientId());
	}
	
	/**
	 * @return Encounter last encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getLastEncounter(String encounterType) {
		if (patientEncounterMap.containsKey(encounterType))
			return (Encounter) patientEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = encounterService.getEncounterType(encounterType);
		
		Map<Integer, ?> encounterMap = patientSetService.getEncountersByType(getPatientSetIfNotAllPatients(), type);
		
		patientEncounterMap.put(encounterType, encounterMap);
		
		return (Encounter) encounterMap.get(getPatientId());
	}
	
	/**
	 * Get the attribute (encounterDatetime, provider, encounterType, etc) from the most recent
	 * encounter.
	 * 
	 * @param typeArray
	 * @param attr
	 * @return the encounter attribute
	 */
	@SuppressWarnings("unchecked")
	public Object getLastEncounterAttr(Object typeArray, String attr) {
		
		List<String> types = (List<String>) typeArray;
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
			catch (Exception e) { /* pass */}
			;
			
			if (type == null)
				type = encounterService.getEncounterType(typeName);
			
			if (type != null)
				encounterTypes.add(type);
		}
		
		Map<Integer, Object> encounterMap = patientSetService.getEncounterAttrsByType(getPatientSetIfNotAllPatients(),
		    encounterTypes, attr);
		
		patientEncounterMap.put(key, encounterMap);
		
		return encounterMap.get(getPatientId());
		
	}
	
	/**
	 * @return Encounter first encounter of type <code>encounterType</code>
	 * @param encounterType
	 */
	public Encounter getFirstEncounter(String encounterType) {
		if (patientFirstEncounterMap.containsKey(encounterType))
			return (Encounter) patientFirstEncounterMap.get(encounterType).get(getPatientId());
		
		log.debug("getting first encounters for type: " + encounterType);
		
		EncounterType type = null;
		if (!encounterType.equals(""))
			type = encounterService.getEncounterType(encounterType);
		
		Map<Integer, Encounter> encounterMap = patientSetService.getFirstEncountersByType(getPatientSetIfNotAllPatients(),
		    type);
		
		patientFirstEncounterMap.put(encounterType, encounterMap);
		
		return encounterMap.get(getPatientId());
	}
	
	/**
	 * Get an attribute (encounterDatetime, provider, etc) from the oldest encounter.
	 * 
	 * @param typeArray
	 * @param attr
	 * @return Object from the oldest encounter
	 */
	@SuppressWarnings("unchecked")
	public Object getFirstEncounterAttr(Object typeArray, String attr) {
		
		List<String> types = (List<String>) typeArray;
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
			catch (Exception e) { /* pass */}
			;
			
			if (type == null)
				type = encounterService.getEncounterType(typeName);
			
			if (type != null)
				encounterTypes.add(type);
		}
		
		Map<Integer, Object> encounterMap = patientSetService.getFirstEncounterAttrsByType(getPatientSetIfNotAllPatients(),
		    encounterTypes, attr);
		
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
			patientIdObsMap = patientSetService.getObservationsValues(getPatientSetIfNotAllPatients(), c, attrs);
			conceptAttrObsMap.put(key, patientIdObsMap);
		}
		return patientIdObsMap.get(patientId);
	}
	
	/**
	 * Gets a patient program given a program ID or program name.
	 * 
	 * @param programIdOrName the identifier or name of the program
	 * @return PatientProgram by the program ID or name
	 */
	public PatientProgram getProgram(String programIdOrName) {
		
		Map<Integer, PatientProgram> patientIdProgramMap;
		if (programMap.containsKey(programIdOrName)) {
			patientIdProgramMap = programMap.get(programIdOrName);
		} else {
			
			Program program = null;
			
			//
			// Ticket #912 - Fixed by adding some code to lookup the program by ID
			//
			try {
				Integer programId = Integer.parseInt(programIdOrName);
				program = Context.getProgramWorkflowService().getProgram(programId);
			}
			catch (NumberFormatException e) { /* ignore error because we're going to look the program up by name */}
			
			if (program == null) {
				program = Context.getProgramWorkflowService().getProgramByName(programIdOrName);
			}
			patientIdProgramMap = patientSetService.getPatientPrograms(getPatientSetIfNotAllPatients(), program);
			programMap.put(programIdOrName, patientIdProgramMap);
		}
		return patientIdProgramMap.get(patientId);
	}
	
	public List<DrugOrder> getCurrentDrugOrders(String drugSetName) {
		Map<Integer, List<DrugOrder>> patientIdDrugOrderMap;
		if (currentDrugOrderMap.containsKey(drugSetName)) {
			patientIdDrugOrderMap = currentDrugOrderMap.get(drugSetName);
		} else {
			Concept drugSet = conceptService.getConceptByName(drugSetName);
			patientIdDrugOrderMap = patientSetService.getCurrentDrugOrders(getPatientSetIfNotAllPatients(), drugSet);
			currentDrugOrderMap.put(drugSetName, patientIdDrugOrderMap);
		}
		return patientIdDrugOrderMap.get(patientId);
	}
	
	public String getCurrentDrugNames(String drugSetName) {
		List<DrugOrder> patientOrders = getCurrentDrugOrders(drugSetName);
		if (patientOrders == null)
			return "";
		StringBuilder ret = new StringBuilder();
		for (Iterator<DrugOrder> i = patientOrders.iterator(); i.hasNext();) {
			DrugOrder o = i.next();
			if (o.getDrug() != null)
				ret.append(o.getDrug().getName());
			else
				ret.append(o.getConcept().getBestName(Context.getLocale()).getName());
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
			patientIdDrugOrderMap = patientSetService.getDrugOrders(getPatientSetIfNotAllPatients(), drugSet);
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
			RelationshipType relType = Context.getPersonService().getRelationshipTypeByName(relationshipTypeName);
			patientIdRelationshipMap = patientSetService.getRelationships(getPatientSetIfNotAllPatients(), relType);
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
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext();) {
				Relationship r = i.next();
				sb.append(r.getPersonA().getPersonName().getFullName());
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
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext();) {
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
			for (Iterator<Relationship> i = rels.iterator(); i.hasNext();) {
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
	 * @return Object the specified property of the patient
	 */
	public Object getPatientAttr(String className, String property) {
		return getPatientAttr(className, property, false);
	}
	
	/**
	 * Retrieves properties on the patient like patient.patientName.familyName
	 * <p>
	 * If returnAll is set, returns an array of every matching property for the patient instead of
	 * just the preferred one
	 * 
	 * @param className
	 * @param property
	 * @param returnAll
	 * @return Object the specified property of the patient, or an array of all properties if
	 *         returnAll is set to true
	 */
	public Object getPatientAttr(String className, String property, boolean returnAll) {
		String key = className + "." + property;
		
		if (returnAll)
			key += "--all";
		
		Map<Integer, Object> patientIdAttrMap;
		if (patientAttributeMap.containsKey(key)) {
			patientIdAttrMap = patientAttributeMap.get(key);
		} else {
			//log.debug("getting patient attrs: " + key);
			patientIdAttrMap = patientSetService.getPatientAttributes(getPatientSetIfNotAllPatients(), className, property,
			    returnAll);
			patientAttributeMap.put(key, patientIdAttrMap);
		}
		
		return patientIdAttrMap.get(patientId);
	}
	
	public Object getPersonAttribute(String attributeName, String joinClass, String joinProperty, String outputColumn,
	        boolean returnAll) {
		String key = attributeName + "." + joinClass + "." + joinProperty;
		
		if (returnAll)
			key += "--all";
		
		Map<Integer, Object> personIdAttrMap;
		if (personAttributeMap.containsKey(key)) {
			personIdAttrMap = personAttributeMap.get(key);
		} else {
			//log.debug("getting patient attrs: " + key);
			personIdAttrMap = patientSetService.getPersonAttributes(getPatientSetIfNotAllPatients(), attributeName,
			    joinClass, joinProperty, outputColumn, returnAll);
			personAttributeMap.put(key, personIdAttrMap);
		}
		return personIdAttrMap.get(patientId);
	}
	
	public Object getPersonAttribute(String attributeName) {
		return getPersonAttribute(attributeName, null, null, null, false);
	}
	
	/**
	 * Gets the last <code>n</code> obs for <code>conceptName</code>
	 * 
	 * @param n max number of obs to return
	 * @param conceptName
	 * @return List<Object> of observations
	 */
	public List<Object> getLastNObs(Integer n, String conceptName) throws Exception {
		return getLastNObs(n, getConcept(conceptName));
	}
	
	/**
	 * Gets Observations from the last encounter
	 * 
	 * @param n max number of obs to return
	 * @param concept
	 * @return List<Object> of observations
	 * @throws Exception
	 */
	public List<Object> getLastNObs(Integer n, Concept concept) throws Exception {
		List<Object> returnList = new Vector<Object>();
		for (List<Object> row : getLastNObsWithValues(n, concept, null)) {
			returnList.add(row.get(0));
		}
		return returnList;
	}
	
	@SuppressWarnings("unchecked")
	public List<List<Object>> getLastNObsWithValues(Integer n, String conceptId, Object attrs) throws Exception {
		return getLastNObsWithValues(n, getConcept(conceptId), (List<String>) attrs);
	}
	
	/**
	 * Gets the most recent observation value
	 * 
	 * @param n max number of obs to return
	 * @param concept
	 * @param attrs
	 * @return List<List<Object>> of the n most recent observations (with their values)
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
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
	 * 
	 * @param conceptName
	 * @return Object the most recent observation that matches the concept name specified
	 */
	public Object getLastObs(String conceptName) throws Exception {
		return getLastObs(getConcept(conceptName));
	}
	
	/**
	 * Gets the most recent Observation value matching this concept
	 * 
	 * @param conceptName
	 * @param attrs string array
	 * @return List<Object> of the most recent observation values
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getLastObsWithValues(String conceptName, Object attrs) throws Exception {
		//List<String> attrs = new Vector<String>();
		//Collections.addAll(attrs, attrArray);
		return getLastObsWithValues(getConcept(conceptName), (List<String>) attrs);
	}
	
	/**
	 * Get the most recent obs matching <code>concept</code> out of the patient's encounters
	 * 
	 * @param concept
	 * @return the most recent Obs matching concept from the last encounter
	 * @throws Exception
	 */
	public Object getLastObs(Concept concept) throws Exception {
		List<Object> obs = getLastNObs(1, concept);
		
		return obs.get(0);
	}
	
	/**
	 * Get the most recent obs matching <code>concept</code> out of the patient's encounters
	 * 
	 * @param concept
	 * @param attrs list of Strings like provider, encounterDatetime, etc
	 * @return List<Object> of the most recent observation values that matches the concept of the
	 *         patient encounters
	 * @throws Exception
	 */
	public List<Object> getLastObsWithValues(Concept concept, List<String> attrs) throws Exception {
		List<List<Object>> obs = getLastNObsWithValues(1, concept, attrs);
		
		return obs.get(0);
	}
	
	/**
	 * Get the first occurrence of matching <code>obs.concept</code> out of the patient's encounters
	 * 
	 * @param conceptName
	 * @return Object the first occurrence of the observation concept name of the patient's
	 *         encounters
	 */
	public Object getFirstObs(String conceptName) throws Exception {
		return getFirstObs(getConcept(conceptName));
	}
	
	/**
	 * Get the first occurrence of matching <code>obs.concept</code> out of the patient's encounters
	 * 
	 * @param concept
	 * @return Object
	 * @throws Exception
	 */
	public Object getFirstObs(Concept concept) throws Exception {
		List<List<Object>> obs = getObsWithValues(concept, null);
		
		if (obs != null && obs.size() > 0) {
			List<Object> o = obs.get(obs.size() - 1);
			return o.get(0);
		}
		
		log.info("Could not find an Obs with concept " + concept + " for patient " + patientId);
		
		return null;
	}
	
	/**
	 * Get the first occurrence of matching <code>obs.concept</code> out of the patient's encounters
	 * 
	 * @param conceptName
	 * @return List<Object>
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getFirstObsWithValues(String conceptName, Object attrs) throws Exception {
		//List<String> attrs = new Vector<String>();
		//Collections.addAll(attrs, attrArray);
		return getFirstObsWithValues(getConcept(conceptName), (List<String>) attrs);
	}
	
	/**
	 * Get the first occurence of matching <code>obs.concept</code> out of the patient's encounters
	 * 
	 * @param concept
	 * @param attrs the List of attributes to fetch
	 * @return List<Object>
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public List<Object> getFirstObsWithValues(Concept concept, List<String> attrs) throws Exception {
		// add a null first column for the actual obs value
		attrs.add(0, null);
		
		List<List<Object>> obs = getObsWithValues(concept, attrs);
		
		if (obs == null) {
			List<Object> blankRow = new Vector<Object>();
			for (String attr : attrs)
				blankRow.add("");
			return blankRow;
		}
		
		if (obs.size() > 0) {
			return obs.get(obs.size() - 1);
		}
		
		log.info("Could not find an Obs with concept " + concept + " for patient " + patientId);
		
		return null;
	}
	
	/**
	 * Get the first occurrences of matching <code>obs.concept</code> out of the patient's
	 * encounters
	 * 
	 * @param concept the Concept of the obs to fetch
	 * @param n number of obs to get
	 * @param attrs the Extra obs attributes to get along with this obs value
	 * @return List<List<Object>>
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public List<List<Object>> getFirstNObsWithValues(Integer n, Concept concept, List<String> attrs) throws Exception {
		// add a null first column for the actual obs value
		attrs.add(0, null);
		
		List<List<Object>> obs = getObsWithValues(concept, attrs);
		
		if (obs == null)
			obs = new Vector<List<Object>>();
		
		if (n.equals(-1))
			return obs;
		
		List<Object> blankRow = new Vector<Object>();
		for (String attr : attrs)
			blankRow.add("");
		while (obs.size() < n)
			obs.add(0, blankRow);
		
		int size = obs.size();
		List<List<Object>> rList = obs.subList(size - n, size);
		
		Collections.reverse(rList);
		
		return rList;
	}
	
	/**
	 * Convenience method for other getFirstNObsWithValues method
	 * 
	 * @see #getFirstNObsWithValues(Integer, Concept, List)
	 */
	@SuppressWarnings("unchecked")
	public List<List<Object>> getFirstNObsWithValues(Integer n, String conceptId, Object attrs) throws Exception {
		return getFirstNObsWithValues(n, getConcept(conceptId), (List<String>) attrs);
	}
	
	/**
	 * Retrieves a patient identifier based on the given identifier type.
	 * 
	 * @param typeName
	 * @return the PatientIdentifier for the given typename
	 */
	public Object getPatientIdentifier(String typeName) {
		
		log.debug("Identifier Type: " + typeName);
		Map<Integer, PatientIdentifier> patientIdentifiers;
		if (patientIdentifierMap.containsKey(typeName)) {
			patientIdentifiers = patientIdentifierMap.get(typeName);
		} else {
			PatientIdentifierType type = null;
			// First try by Integer id
			try {
				Integer id = Integer.valueOf(typeName);
				type = patientService.getPatientIdentifierType(id);
			}
			catch (NumberFormatException ex) {}
			// otherwise get identifier type by the given name
			if (type == null) {
				type = patientService.getPatientIdentifierTypeByName(typeName);
			}
			// Get identifiers by type 
			patientIdentifiers = patientSetService.getPatientIdentifiersByType(getPatientSetIfNotAllPatients(), type);
			
			log.debug("Found identifiers for patient identifier " + type + " = " + patientIdentifiers);
			
			patientIdentifierMap.put(typeName, patientIdentifiers);
		}
		
		return patientIdentifiers.get(patientId);
		
	}
	
	// /**
	// * Get all obs for the current patient that match this
	// * <code>obs.concept</code>=<code>concept</code> and
	// * <code>obs.valueCoded</code>=<code>valueCoded</code>
	// * 
	// * @param conceptName
	// * @param valueCoded
	// * @return 
	// * @throws Exception
	// */
	//	public Collection<Obs> getMatchingObs(String conceptName, String valueCoded) throws Exception {
	//		return getMatchingObs(getConcept(conceptName), getConcept(valueCoded));
	//	}
	// /**
	// * Get all obs for the current patient that match this
	// * <code>obs.concept</code>=<code>concept</code> and
	// * <code>obs.valueCoded</code>=<code>valueCoded</code>
	// * 
	// * @param concept
	// * @param valueCoded
	// * @return
	// * @throws Exception
	// */
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
	 * Calculate the years between two dates (age).
	 * 
	 * @param fromDate
	 * @param toDate
	 */
	public int calculateYearsBetween(Date fromDate, Date toDate) {
		
		if (fromDate == null || toDate == null) {
			return 0;
		}
		
		Calendar from = Calendar.getInstance();
		from.setTime(fromDate);
		
		Calendar to = Calendar.getInstance();
		to.setTime(toDate);
		
		int yearsBetween = to.get(Calendar.YEAR) - from.get(Calendar.YEAR);
		if (from.get(Calendar.DAY_OF_YEAR) > to.get(Calendar.DAY_OF_YEAR)) {
			yearsBetween -= 1;
		}
		return yearsBetween;
	}
	
	/**
	 * Get the person's birthdate as of today
	 * 
	 * @param birthdate the person's date of birth
	 * @return The calculated age
	 */
	public int calculateAge(Date birthdate) {
		log.info("Calculating age " + birthdate);
		return calculateYearsBetween(birthdate, new Date());
	}
	
	/**
	 * Format the given date according to the type ('short', 'long', 'ymd')
	 * 
	 * @param type format to use
	 * @param d Date to format
	 * @return a String with the formatted date
	 */
	public String formatDate(String type, Date d) {
		if (d == null)
			return "";
		
		if ("long".equals(type)) {
			return dateFormatLong.format(d);
		} else if ("ymd".equals(type))
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
	 * Get a calendar instance for use in velocity scripts.
	 * 
	 * @return calendar
	 */
	public Calendar getCalendarInstance() {
		if (calendar == null)
			calendar = Calendar.getInstance(locale);
		return calendar;
	}
	
	/**
	 * Check the given string against the check digit algorithm
	 * 
	 * @param id
	 * @return true/false whether the string has a valid check digit
	 */
	public boolean isValidCheckDigit(String id) {
		try {
			return Context.getPatientService().getDefaultIdentifierValidator().isValid(id);
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
			return ((Concept) o).getName().toString();
		else if (o instanceof Drug)
			return ((Drug) o).getName();
		else if (o instanceof Location)
			return ((Location) o).getName();
		else if (o instanceof User)
			return ((User) o).toString();
		else if (o instanceof EncounterType)
			return ((EncounterType) o).getName();
		else if (o instanceof Date)
			return formatDate(null, (Date) o);
		else
			return o.toString();
	}
	
	/**
	 * Returns the patient set only if it is a subset of all patients. Returns null other wise
	 * 
	 * @return PatientSet object with patients or null if it isn't needed
	 */
	public Cohort getPatientSetIfNotAllPatients() {
		if (isAllPatients)
			return null;
		return getPatientSet();
	}
	
}
