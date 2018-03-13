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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.DiagnosisDAO;


/**
 * Hibernate implementation of the DiagnosisDAO
 *
 * @see DiagnosisDAO
 * @see org.openmrs.api.DiagnosisService
 *
 */
public class HibernateDiagnosisDAO implements DiagnosisDAO {
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	/**
	 * The rank for a primary diagnosis
	 */
	private static final Integer PRIMARY_RANK = 1;

	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Saves the diagnosis.
	 *
	 * @param diagnosis the diagnosis to save.
	 * @return the saved diagnosis.
	 */
	@Override
	public Diagnosis saveDiagnosis(Diagnosis diagnosis) {
		sessionFactory.getCurrentSession().saveOrUpdate(diagnosis);
		return diagnosis;
	}

	/**
	 * Gets all active diagnoses related to the specified patient.
	 *
	 * @param patient the patient whose active diagnoses are being queried.
	 * @return all active diagnoses associated with the specified patient.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Diagnosis> getActiveDiagnoses(Patient patient, Date fromDate) {
		String fromDateCriteria = "";
		if(fromDate != null){
			fromDateCriteria = " and d.dateCreated >= :fromDate ";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(
			"from Diagnosis d where d.patient.patientId = :patientId and d.voided = false " 
				+ fromDateCriteria  
				+ " order by d.dateCreated desc");
		query.setInteger("patientId", patient.getId());
		if(fromDate != null){
			query.setDate("fromDate", fromDate);
		}
		return query.list();
	}

	/**
	 * Gets all diagnoses for a given encounter
	 *
	 * @param encounter the specific encounter to get the diagnoses for.
	 * @return list of diagnoses for an encounter
	 */
	@Override
	public List<Diagnosis> getDiagnoses(Encounter encounter){
		Query query = sessionFactory.getCurrentSession().createQuery(
			"from Diagnosis d where d.encounter.encounterId = :encounterId order by dateCreated desc");
		query.setInteger("encounterId", encounter.getId());
		return query.list();	
	}

	/**
	 * Gets primary diagnoses for a given encounter
	 *
	 * @param encounter the specific encounter to get the primary diagnoses for.
	 * @return list of primary diagnoses for an encounter
	 */
	@Override
	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter) {
		Query query = sessionFactory.getCurrentSession().createQuery(
			"from Diagnosis d where d.encounter.encounterId = :encounterId and d.rank = :rankId order by dateCreated desc");
		query.setInteger("encounterId", encounter.getId());
		query.setInteger("rankId", PRIMARY_RANK);
		return query.list();
	}

	/**
	 * Gets a diagnosis from database using the diagnosis id
	 * 
	 * @param diagnosisId the id of the diagnosis to look for
	 * @return the diagnosis with the given diagnosis id
	 */
	@Override
	public Diagnosis getDiagnosisById(Integer diagnosisId) {
		return (Diagnosis) sessionFactory.getCurrentSession().get(Diagnosis.class, diagnosisId);
	}
	
	/**
	 * Gets the diagnosis attached to the specified UUID.
	 *
	 * @param uuid the uuid to search for in the database.
	 * @return the diagnosis associated with the UUID.
	 */
	@Override
	public Diagnosis getDiagnosisByUuid(String uuid){
		return (Diagnosis) sessionFactory.getCurrentSession().createQuery("from Diagnosis d where d.uuid = :uuid")
			.setString("uuid", uuid).uniqueResult();
	}

	/**
	 * Completely remove a diagnosis from the database. 
	 * @param diagnosis diagnosis to remove from the database
	 */
	@Override
	public void deleteDiagnosis(Diagnosis diagnosis) throws DAOException{
		sessionFactory.getCurrentSession().delete(diagnosis);
	}
}
