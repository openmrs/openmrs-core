package org.openmrs.api.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.Constraint;

/**
 * Observation-related database functions
 * 
 * @version 1.0
 */
public interface ObsDAO {

	/**
	 * @see org.openmrs.api.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person)
	 */
	public Set<Obs> getObservations(Person who) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept,org.openmrs.Location,java.lang.String,java.lang.Integer)
	 */
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer patientType)
			throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person,org.openmrs.Concept)
	 */
	public Set<Obs> getObservations(Person who, Concept question)
			throws DAOException;

	/**
	 * e.g. get last 'n' number of observations for a patient for given concept
	 * 
	 * @param n
	 *            number of concepts to retrieve
	 * @param who
	 * @param question
	 * @return
	 */
	public List<Obs> getLastNObservations(Integer n, Person who,
			Concept question);

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept,java.lang.String,java.lang.Integer)
	 */
	public List<Obs> getObservations(Concept question, String sort, Integer personType)
			throws DAOException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsAnsweredByConcept(org.openmrs.Concept,java.lang.Integer)
	 */
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType);
	
	/**
	 *  @see org.openmrs.api.ObsService#getNumericAnswersForConcept(org.openmrs.Concept,java.lang.Boolean,java.lang.Integer)
	 */
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType);
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Encounter)
	 */
	public Set<Obs> getObservations(Encounter whichEncounter)
			throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getVoidedObservations()
	 */
	public List<Obs> getVoidedObservations() throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#findObservations(java.lang.Integer,boolean,java.lang.Integer)
	 */
	public List<Obs> findObservations(Integer id, boolean includeVoided, Integer personType)
			throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#findObsByGroupId(java.lang.Integer)
	 */
	public List<Obs> findObsByGroupId(Integer obsGroupId) throws DAOException;

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person,org.openmrs.logic.Aggregation)
	 */
	public List<Obs> getObservations(Person who, Aggregation aggregation,
			Concept question, Constraint constraint);

}
