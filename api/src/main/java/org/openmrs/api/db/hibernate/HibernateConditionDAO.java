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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Condition;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.db.ConditionDAO;
import org.openmrs.api.db.DAOException;

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
	
	/**
	 * Gets the condition with the specified id.
	 *
	 * @param conditionId the id to search for in the database.
	 * @return the condition associated with the id.
	 */
	@Override
	public Condition getCondition(Integer conditionId) {
		return (Condition) sessionFactory.getCurrentSession().get(Condition.class, conditionId);
	}
	
	/**
	 * Gets the condition by its UUID.
	 *
	 * @param uuid the UUID to search for in the database.
	 * @return the condition associated with the UUID.
	 */
	@Override
	public Condition getConditionByUuid(String uuid) {
		return (Condition) sessionFactory.getCurrentSession().createQuery("from Condition c where c.uuid = :uuid")
				.setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * Gets all conditions related to the specified patient.
	 *
	 * @param patient the patient whose condition history is being queried.
	 * @return all active and non active conditions related to the specified patient.
	 */
	@Override
	public List<Condition> getConditionHistory(Patient patient) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Condition con where con.patient.patientId = :patientId and con.voided = false " +
						"order by con.onsetDate desc");
		query.setInteger("patientId", patient.getId());
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
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Condition c where c.patient.patientId = :patientId and c.voided = false and c.endDate is null order "
						+ "by c.onsetDate desc");
		query.setInteger("patientId", patient.getId());
		return query.list();
	}

	/**
	 * @see ConditionService#getAllConditions(Patient)
	 */
	@Override
	public List<Condition> getAllConditions(Patient patient) {
		return this.getConditionHistory(patient);
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
	 * @see ConditionService#getConditionsByEncounter(Encounter)
	 */
	@Override
	public List<Condition> getConditionsByEncounter(Encounter encounter) throws APIException {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Condition c where c.encounter.encounterId = :encounterId and c.voided = false order "
						+ "by c.onsetDate desc");
		query.setInteger("encounterId", encounter.getId());
		return query.list();
	}
}
