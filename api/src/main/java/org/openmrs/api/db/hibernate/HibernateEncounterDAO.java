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
package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;

/**
 * Hibernate specific dao for the {@link EncounterService} All calls should be
 * made on the Context.getEncounterService() object
 * 
 * @see EncounterDAO
 * @see EncounterService
 */
public class HibernateEncounterDAO implements EncounterDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounter(org.openmrs.Encounter)
	 */
	public Encounter saveEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounter);
		return encounter;
	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounter);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return (Encounter) sessionFactory.getCurrentSession().get(
				Encounter.class, encounterId);
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByPatientId(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncountersByPatientId(Integer patientId)
			throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(
				Encounter.class).createAlias("patient", "p").add(
				Expression.eq("p.patientId", patientId)).add(
				Expression.eq("voided", false)).addOrder(
				Order.desc("encounterDatetime"));

		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date,
	 *      java.util.Collection, java.util.Collection, java.util.Collection,
	 *      boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(Patient patient, Location location,
			Date fromDate, Date toDate, Collection<Form> enteredViaForms,
			Collection<EncounterType> encounterTypes,
			Collection<User> providers, boolean includeVoided) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(
				Encounter.class);
		if (patient != null && patient.getPatientId() != null) {
			crit.add(Expression.eq("patient", patient));
		}
		if (location != null && location.getLocationId() != null) {
			crit.add(Expression.eq("location", location));
		}
		if (fromDate != null) {
			crit.add(Expression.ge("encounterDatetime", fromDate));
		}
		if (toDate != null) {
			crit.add(Expression.le("encounterDatetime", toDate));
		}
		if (enteredViaForms != null && enteredViaForms.size() > 0) {
			crit.add(Expression.in("form", enteredViaForms));
		}
		if (encounterTypes != null && encounterTypes.size() > 0) {
			crit.add(Expression.in("encounterType", encounterTypes));
		}
		if (providers != null && providers.size() > 0) {
			crit.add(Expression.in("provider", providers));
		}
		if (!includeVoided) {
			crit.add(Expression.eq("voided", false));
		}
		crit.addOrder(Order.asc("encounterDatetime"));
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType saveEncounterType(EncounterType encounterType) {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterType);
		return encounterType;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType)
			throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterType);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId)
			throws DAOException {
		return (EncounterType) sessionFactory.getCurrentSession().get(
				EncounterType.class, encounterTypeId);
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.String)
	 */
	public EncounterType getEncounterType(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(
				EncounterType.class);
		crit.add(Expression.eq("retired", false));
		crit.add(Expression.eq("name", name));
		EncounterType encounterType = (EncounterType) crit.uniqueResult();

		return encounterType;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterTypes(java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<EncounterType> getAllEncounterTypes(Boolean includeRetired)
			throws DAOException {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(
				EncounterType.class);

		criteria.addOrder(Order.asc("name"));

		if (includeRetired == false)
			criteria.add(Expression.eq("retired", false));

		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#findEncounterTypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<EncounterType> findEncounterTypes(String name)
			throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(
				EncounterType.class)
				// 'ilike' case insensitive search
				.add(Expression.ilike("name", name, MatchMode.START)).addOrder(
						Order.asc("name")).addOrder(Order.asc("retired"))
				.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterDatetime(org.openmrs.Encounter)
	 */
	public Date getSavedEncounterDatetime(Encounter encounter) {
		SQLQuery sql = sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"select encounter_datetime from encounter where encounter_id = :encounterId");
		sql.setInteger("encounterId", encounter.getEncounterId());
		return (Date) sql.uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterByUuid(java.lang.String)
	 */
	public Encounter getEncounterByUuid(String uuid) {
		return (Encounter) sessionFactory.getCurrentSession().createQuery(
				"from Encounter e where e.uuid = :uuid")
				.setString("uuid", uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterTypeByUuid(java.lang.String)
	 */
	public EncounterType getEncounterTypeByUuid(String uuid) {
		return (EncounterType) sessionFactory.getCurrentSession().createQuery(
				"from EncounterType et where et.uuid = :uuid").setString(
				"uuid", uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByPatient(String,
	 *      boolean)
	 */
	public List<Encounter> getEncountersByPatient(String query,
			boolean includedVoided) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(
				Encounter.class);
		if (!includedVoided)
			criteria.add(Restrictions.eq("voided", false));

		criteria = criteria.createCriteria("patient", "pat");
		String name = null;
		String identifier = null;
		if (query.matches(".*\\d+.*")) {
			identifier = query;
		} else {
			// there is no number in the string, search on name
			name = query;
		}
		criteria = new PatientSearchCriteria(sessionFactory, criteria)
				.prepareCriteria(name, identifier,
						new ArrayList<PatientIdentifierType>(), false);
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterLocation(org.openmrs.Encounter)
	 */
	public Location getSavedEncounterLocation(Encounter encounter) {
		SQLQuery sql = sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"select location_id from encounter where encounter_id = :encounterId");
		sql.setInteger("encounterId", encounter.getEncounterId());
		return Context.getLocationService().getLocation(
				(Integer) sql.uniqueResult());
	}
}
