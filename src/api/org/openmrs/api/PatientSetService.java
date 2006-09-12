package org.openmrs.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;

public class PatientSetService {
	
	private Context context;
	private DAOContext daoContext;
	
	public PatientSetService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}

	private PatientSetDAO getPatientSetDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_SETS)) {
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENT_SETS);
		}
		return daoContext.getPatientSetDAO();
	}
	
	/**
	 * @param ps The set you want to export as XML
	 * @return an XML representation of this patient-set, including patient characteristics, and observations
	 */
	public String exportXml(PatientSet ps) {
		return getPatientSetDAO().exportXml(ps);
	}

	public String exportXml(Integer patientId) {
		return getPatientSetDAO().exportXml(patientId);
	}
	
	public PatientSet getAllPatients() throws DAOException {
		return getPatientSetDAO().getAllPatients();
	}
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException {
		return getPatientsByCharacteristics(gender, minBirthdate, maxBirthdate, null, null, null, null);
	}
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate,
			Integer minAge, Integer maxAge, Boolean aliveOnly, Boolean deadOnly) throws DAOException {
		return getPatientSetDAO().getPatientsByCharacteristics(gender, minBirthdate, maxBirthdate, minAge, maxAge, aliveOnly, deadOnly);
	}

	/*
	public PatientSet getPatientsHavingNumericObs(Concept concept, PatientSetService.Modifier modifier, Number value) {
		return getPatientsHavingNumericObs(concept.getConceptId(), null, modifier, value, null, null);
	}
	
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, PatientSetService.Modifier modifier, Number value) {
		return getPatientsHavingNumericObs(conceptId, null, modifier, value, null, null);
	}
	*/
	
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, TimeModifier timeModifier, PatientSetService.Modifier modifier, Number value, Date fromDate, Date toDate) {
		return getPatientSetDAO().getPatientsHavingNumericObs(conceptId, timeModifier, modifier, value, fromDate, toDate);
	}
	
	public PatientSet getPatientsHavingTextObs(Concept concept, String value) {
		return getPatientsHavingTextObs(concept.getConceptId(), value);
	}
	
	public PatientSet getPatientsHavingTextObs(Integer conceptId, String value) {
		return getPatientSetDAO().getPatientsHavingTextObs(conceptId, value);
	}
	
	public PatientSet getPatientsHavingLocation(Location loc) {
		return getPatientsHavingLocation(loc.getLocationId());
	}
	
	public PatientSet getPatientsHavingLocation(Integer locationId) {
		return getPatientSetDAO().getPatientsHavingLocation(locationId);
	}

	/**
	 * Returns a PatientSet of patient who had drug orders for a set of drugs active on a certain date.
	 * Can also be used to find patient with no drug orders on that date.
	 * @param patientIds Collection of patientIds you're interested in. NULL means all patients.
	 * @param takingAny Collection of drugIds the patient is taking. (Or the empty set to mean "any drug" or NULL to mean "no drugs")
	 * @param onDate Which date to look at the patients' drug orders. (NULL defaults to now().)
	 */
	public PatientSet getPatientsHavingDrugOrder(Collection<Integer> patientIds, Collection<Integer> takingIds, Date onDate) {
		Map<Integer, Collection<Integer>> activeDrugs = getPatientSetDAO().getActiveDrugIds(patientIds, onDate);
		List<Integer> ret = new ArrayList<Integer>();
		boolean takingAny = takingIds != null && takingIds.size() == 0;
		boolean takingNone = takingIds == null;
		if (takingAny) {
			ret.addAll(activeDrugs.keySet());
		} else if (takingNone) {
			if (patientIds == null) {
				patientIds = getAllPatients().getPatientIds();
			}
			patientIds.removeAll(activeDrugs.keySet());
			ret.addAll(patientIds);
		} else { // taking any of the drugs in takingIds
			for (Map.Entry<Integer, Collection<Integer>> e : activeDrugs.entrySet()) {
				for (Integer drugId : takingIds) {
					if (e.getValue().contains(drugId)) {
						ret.add(e.getKey());
						break;
					}
				}
			}
		}
		PatientSet ps = new PatientSet();
		ps.setPatientIds(ret);
		return ps;
	}
	
	public Map<Integer, String> getShortPatientDescriptions(Collection<Integer> patientIds) {
		return getPatientSetDAO().getShortPatientDescriptions(patientIds);
	}
	
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept) {
		return getPatientSetDAO().getObservations(patients, concept, null, null);
	}
	
	/**
	 * Date range is inclusive of both endpoints 
	 */
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept, Date fromDate, Date toDate) {
		return getPatientSetDAO().getObservations(patients, concept, fromDate, toDate);
	}

	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, EncounterType encType) {
		return getPatientSetDAO().getEncountersByType(patients, encType);
	}
	
	public Map<Integer, Encounter> getFirstEncountersByType(PatientSet patients, EncounterType encType) {
		return getPatientSetDAO().getEncountersByType(patients, encType);
	}
	
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll) {
		return getPatientSetDAO().getPatientAttributes(patients, className, property, returnAll);
	}
	
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String classNameDotProperty, boolean returnAll) {
		String[] temp = classNameDotProperty.split("\\.");
		if (temp.length != 2) {
			throw new IllegalArgumentException(classNameDotProperty + " must be ClassName.property");
		}
		return getPatientAttributes(patients, temp[0], temp[1], returnAll);
	}
	
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) {
		return getPatientSetDAO().getCharacteristics(patients);
	}
	
	public enum Modifier {
		LESS_THAN ("<"),
		LESS_EQUAL ("<="),
		EQUAL ("="),
		GREATER_EQUAL (">="),
		GREATER_THAN (">");

		public final String sqlRep;
		Modifier(String sqlRep) {
			this.sqlRep = sqlRep;
		}
		public String getSqlRepresentation() {
			return sqlRep;
		}
	}
	
	public enum TimeModifier {
		ANY,
		NO,
		FIRST,
		LAST,
		MIN,
		MAX,
		AVG;
	}
	
	public List<Patient> getPatients(Collection<Integer> patientIds) {
		return getPatientSetDAO().getPatients(patientIds);		
	}
	
	// these should go elsewhere
	
	private static Map<User, PatientSet> userPatientSets;
	
	public void setMyPatientSet(PatientSet ps) {
		if (context != null) {
			if (userPatientSets == null) {
				userPatientSets = new HashMap<User, PatientSet>();
			}
			User u = context.getAuthenticatedUser();
			userPatientSets.put(u, ps);
		}
	}
	
	public PatientSet getMyPatientSet() {
		if (context == null) {
			return null;
		}
		if (userPatientSets == null) {
			userPatientSets = new HashMap<User, PatientSet>();
		}
		PatientSet mine = userPatientSets.get(context.getAuthenticatedUser()); 
		if (mine == null) {
			mine = new PatientSet();
			userPatientSets.put(context.getAuthenticatedUser(), mine);
		}
		return mine;
	}
	
	public void addToMyPatientSet(Integer ptId) {
		getMyPatientSet().add(ptId);
	}
	
	public void removeFromMyPatientSet(Integer ptId) {
		getMyPatientSet().remove(ptId);
	}
	
	public void clearMyPatientSet() {
		setMyPatientSet(null);
	}

	public Map<Integer, PatientState> getCurrentStates(PatientSet ps, ProgramWorkflow wf) {
		return getPatientSetDAO().getCurrentStates(ps, wf);
	}

	public Map<Integer, PatientProgram> getCurrentPatientPrograms(PatientSet ps, Program program) {
		return getPatientSetDAO().getPatientPrograms(ps, program, false, false);
	}
	
	public Map<Integer, PatientProgram> getPatientPrograms(PatientSet ps, Program program) {
		return getPatientSetDAO().getPatientPrograms(ps, program, false, true);
	}
	
	/**
	 * @return all active drug orders whose drug concept is in the given set (or all drugs if that's null) 
	 */
	public Map<Integer, List<DrugOrder>> getCurrentDrugOrders(PatientSet ps, Concept drugSet) {
		List<Concept> drugConcepts = null;
		if (drugSet != null) {
			List<ConceptSet> concepts = context.getConceptService().getConceptSets(drugSet);
			drugConcepts = new ArrayList<Concept>();
			for (ConceptSet cs : concepts) {
				drugConcepts.add(cs.getConcept());
			}
		}
		return getPatientSetDAO().getCurrentDrugOrders(ps, drugConcepts);		
	}

	static Map<User, PatientAnalysis> userAnalyses = new HashMap<User, PatientAnalysis>();
	
	public void setMyPatientAnalysis(PatientAnalysis pa) {
		userAnalyses.put(context.getAuthenticatedUser(), pa);
	}
	
	public PatientAnalysis getMyPatientAnalysis() {
		PatientAnalysis analysis = userAnalyses.get(context.getAuthenticatedUser());
		if (analysis == null) {
			analysis = new PatientAnalysis();
			setMyPatientAnalysis(analysis);
		}
		return analysis;
	}
	
}
