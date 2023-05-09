/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.Arrays;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Condition;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ConditionDAO;
import org.openmrs.api.db.DAOException;

import static org.openmrs.ConditionClinicalStatus.ACTIVE;
import static org.openmrs.ConditionClinicalStatus.RECURRENCE;
import static org.openmrs.ConditionClinicalStatus.RELAPSE;

/**
 * Hibernate implementation of the ConditionDAO
 *
 * @see ConditionDAO
 */
public class HibernateConditionDAO implements ConditionDAO {
	
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
	 * Gets the condition with the specified id.
	 *
	 * @param conditionId the id to search for in the database.
	 * @return the condition associated with the id.
	 */
	@Override
	public Condition getCondition(Integer conditionId) {
		return sessionFactory.getCurrentSession().get(Condition.class, conditionId);
	}
	
	/**
	 * Gets the condition by its UUID.
	 *
	 * @param uuid the UUID to search for in the database.
	 * @return the condition associated with the UUID.
	 */
	@Override
	public Condition getConditionByUuid(String uuid) {
		return sessionFactory.getCurrentSession().createQuery("from Condition c where c.uuid = :uuid", Condition.class)
				.setParameter("uuid", uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.ConditionService#getConditionsByEncounter(Encounter)
	 */
	@Override
	public List<Condition> getConditionsByEncounter(Encounter encounter) throws APIException {
		Query<Condition> query = sessionFactory.getCurrentSession().createQuery(
			"from Condition c where c.encounter.encounterId = :encounterId and c.voided = false order "
				+ "by c.dateCreated desc", Condition.class);
		query.setParameter("encounterId", encounter.getId());
		return query.list();
	}
	
	/**
	 * Gets all active conditions related to the specified patient.
	 *
	 * @param patient the patient whose active conditions are being queried.
	 * @return all active conditions associated with the specified patient.
	 */
	@Override
	public List<Condition> getActiveConditions(Patient patient) {
		Query<Condition> query = sessionFactory.getCurrentSession().createQuery(
				 "from Condition c " +
					 "where c.patient.patientId = :patientId " +
					"and c.clinicalStatus in :activeStatuses " +
					"and c.voided = false " +
					"order by c.dateCreated desc", Condition.class);
		query.setParameter("patientId", patient.getId());
		query.setParameterList("activeStatuses", Arrays.asList(ACTIVE, RECURRENCE, RELAPSE));
		return query.list();
	}

	/**
	 * @see org.openmrs.api.ConditionService#getAllConditions(Patient)
	 */
	@Override
	public List<Condition> getAllConditions(Patient patient) {
		Query<Condition> query = sessionFactory.getCurrentSession().createQuery(
				"from Condition c " +
					"where c.patient.patientId = :patientId " +
					"and c.voided = false " +
					"order by c.dateCreated desc", Condition.class);
		query.setParameter("patientId", patient.getId());
		return query.list();
	}
	
	/**
	 * Removes a condition from the database
	 * 
	 * @param condition the condition to be deleted
	 */
	@Override
	public void deleteCondition(Condition condition) throws DAOException {
		sessionFactory.getCurrentSession().delete(condition);
	}

	/**
	 * Saves the condition.
	 *
	 * @param condition the condition to save.
	 * @return the saved condition.
	 */
	@Override
	public Condition saveCondition(Condition condition) {
		sessionFactory.getCurrentSession().saveOrUpdate(condition);
		return condition;
	}
}
