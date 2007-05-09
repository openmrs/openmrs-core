package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.Constraint;
import org.openmrs.util.OpenmrsConstants;

/**
 * Observation-related services
 * 
 * @version 1.0
 */
public class ObsServiceImpl implements ObsService {

	private Log log = LogFactory.getLog(this.getClass());

	private ObsDAO dao;

	public ObsServiceImpl() {
	}

	/**
	 * Returns the injected dao object for this class
	 * 
	 * @return
	 */
	private ObsDAO getObsDAO() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_OBS);

		return dao;
	}

	/**
	 * @see org.openmrs.api.ObsService#setObsDAO(org.openmrs.api.db.ObsDAO)
	 */
	public void setObsDAO(ObsDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see org.openmrs.api.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_ADD_OBS);
		getObsDAO().createObs(obs);
	}

	/**
	 * @see org.openmrs.api.ObsService#createObsGroup(org.openmrs.Obs[])
	 */
	public void createObsGroup(Obs[] obs) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_ADD_OBS);
		if (obs == null || obs.length < 1)
			return; // silently tolerate calls with missing/empty parameter

		// TODO - consider creating a DAO-level method for creating obs groups
		// more efficiently
		getObsDAO().createObs(obs[0]);
		Integer obsGroupId = obs[0].getObsId();
		obs[0].setObsGroupId(obsGroupId);
		getObsDAO().updateObs(obs[0]);
		for (int i = 1; i < obs.length; i++) {
			obs[i].setObsGroupId(obsGroupId);
			getObsDAO().createObs(obs[i]);
		}
	}

	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws APIException {
		return getObsDAO().getObs(obsId);
	}

	/**
	 * @see org.openmrs.api.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_OBS);

		if (obs.isVoided() && obs.getVoidedBy() == null)
			voidObs(obs, obs.getVoidReason());
		else if (obs.isVoided() == false && obs.getVoidedBy() != null)
			unvoidObs(obs);
		else {
			log.debug("Date voided: " + obs.getDateVoided());
			getObsDAO().updateObs(obs);
		}
	}

	/**
	 * @see org.openmrs.api.ObsService#voidObs(org.openmrs.Obs, java.lang.String)
	 */
	public void voidObs(Obs obs, String reason) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_OBS);
		obs.setVoided(true);
		obs.setVoidReason(reason);
		obs.setVoidedBy(Context.getAuthenticatedUser());
		obs.setDateVoided(new Date());
		getObsDAO().updateObs(obs);
	}

	/**
	 * @see org.openmrs.api.ObsService#unvoidObs(org.openmrs.Obs)
	 */
	public void unvoidObs(Obs obs) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_OBS);
		obs.setVoided(false);
		obs.setVoidReason(null);
		obs.setVoidedBy(null);
		obs.setDateVoided(null);
		getObsDAO().updateObs(obs);
	}

	/**
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_OBS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_DELETE_OBS);
		getObsDAO().deleteObs(obs);
	}

	/**
	 * @see org.openmrs.api.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws APIException {
		return getObsDAO().getMimeTypes();
	}

	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {
		return getObsDAO().getMimeType(mimeTypeId);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person)
	 */
	public Set<Obs> getObservations(Person who) {
		return getObsDAO().getObservations(who);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, org.openmrs.Location, java.lang.String, java.lang.Integer)
	 */
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer personType) {
		return getObsDAO().getObservations(c, loc, sort, personType);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, org.openmrs.Concept)
	 */
	public Set<Obs> getObservations(Person who, Concept question) {
		return getObsDAO().getObservations(who, question);
	}

	/**
	 * @see org.openmrs.api.ObsService#getLastNObservations(java.lang.Integer, org.openmrs.Person, org.openmrs.Concept)
	 */
	public List<Obs> getLastNObservations(Integer n, Person who,
			Concept question) {
		return getObsDAO().getLastNObservations(n, who, question);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Concept, java.lang.String, java.lang.Integer)
	 */
	public List<Obs> getObservations(Concept question, String sort, Integer personType) {
		return getObsDAO().getObservations(question, sort, personType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationsAnsweredByConcept(org.openmrs.Concept, java.lang.Integer)
	 */
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType) {
		return getObsDAO().getObservationsAnsweredByConcept(answer, personType);
	}
	
	/**
	 * @see org.openmrs.api.ObsService#getNumericAnswersForConcept(org.openmrs.Concept, java.lang.Boolean, java.lang.Integer)
	 */
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType) {
		return getObsDAO().getNumericAnswersForConcept(answer, sortByValue, personType);
	}
	

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Encounter)
	 */
	public Set<Obs> getObservations(Encounter whichEncounter) {
		return getObsDAO().getObservations(whichEncounter);
	}

	/**
	 * @see org.openmrs.api.ObsService#getVoidedObservations()
	 */
	public List<Obs> getVoidedObservations() {
		return getObsDAO().getVoidedObservations();
	}

	/**
	 * @see org.openmrs.api.ObsService#findObservations(java.lang.String, boolean, java.lang.Integer)
	 */
	public List<Obs> findObservations(String search, boolean includeVoided, Integer personType) {
		List<Obs> obs = new Vector<Obs>();
		for (Person p : Context.getPatientService().getPatientsByIdentifier(
				search, includeVoided)) {
			obs.addAll(getObsDAO().findObservations(p.getPersonId(),
					includeVoided, personType));
		}
		try {
			Integer i = Integer.valueOf(search);
			if (i != null)
				obs.addAll(getObsDAO().findObservations(i, includeVoided, personType));
		} catch (Exception e) {
		}

		return obs;
	}

	/**
	 * @see org.openmrs.api.ObsService#getDistinctObservationValues(org.openmrs.Concept, java.lang.Integer)
	 */
	public List<String> getDistinctObservationValues(Concept question, Integer personType) {
		// todo: make this efficient, and add a sort option

		Locale l = Context.getLocale();
		List<Obs> obs = getObservations(question, null, personType);
		SortedSet<String> set = new TreeSet<String>();
		for (Obs o : obs) {
			set.add(o.getValueAsString(l));
		}
		return new ArrayList<String>(set);
	}

	/**
	 * @see org.openmrs.api.ObsService#findObsByGroupId(java.lang.Integer)
	 */
	public List<Obs> findObsByGroupId(Integer obsGroupId) {
		return getObsDAO().findObsByGroupId(obsGroupId);
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(org.openmrs.Person, org.openmrs.logic.Aggregation, org.openmrs.Concept, org.openmrs.logic.Constraint)
	 */
	public List<Obs> getObservations(Person who, Aggregation aggregation,
			Concept question, Constraint constraint) {
		return getObsDAO().getObservations(who, aggregation, question, constraint);
	}

}
