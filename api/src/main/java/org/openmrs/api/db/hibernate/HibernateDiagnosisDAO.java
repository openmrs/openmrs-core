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
import org.hibernate.criterion.Restrictions;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.DiagnosisAttribute;
import org.openmrs.DiagnosisAttributeType;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.DiagnosisDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;


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
	 * @see org.openmrs.api.db.DiagnosisDAO#getDiagnosesByEncounter(Encounter, boolean, boolean)
	 */
	@Override
	public List<Diagnosis> getDiagnosesByEncounter(Encounter encounter, boolean primaryOnly, boolean confirmedOnly) {
		String queryString = "from Diagnosis d where d.encounter.encounterId = :encounterId";
		if (primaryOnly) {
			queryString += " and d.rank = :rankId";
		}
		if (confirmedOnly) {
			queryString += " and d.certainty = :certainty";
		}
		queryString += " order by d.dateCreated desc";

		TypedQuery<Diagnosis> query = sessionFactory.getCurrentSession().createQuery(queryString, Diagnosis.class).setParameter("encounterId", encounter.getId());
		if (primaryOnly) {
			query.setParameter("rankId", PRIMARY_RANK);
		}
		if (confirmedOnly) {
			query.setParameter("certainty", ConditionVerificationStatus.CONFIRMED);
		}

		return query.getResultList();
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#getDiagnosesByVisit(Visit, boolean, boolean)
	 */
	@Override
	public List<Diagnosis> getDiagnosesByVisit(Visit visit, boolean primaryOnly, boolean confirmedOnly) {
		String queryString = "from Diagnosis d where d.encounter.visit.visitId = :visitId";
		if (primaryOnly) {
			queryString += " and d.rank = :rankId";
		}
		if (confirmedOnly) {
			queryString += " and d.certainty = :certainty";
		}
		queryString += " order by d.dateCreated desc";

		TypedQuery<Diagnosis> query = sessionFactory.getCurrentSession().createQuery(queryString, Diagnosis.class).setParameter("visitId", visit.getId());
		if (primaryOnly) {
			query.setParameter("rankId", PRIMARY_RANK);
		}
		if (confirmedOnly) {
			query.setParameter("certainty", ConditionVerificationStatus.CONFIRMED);
		}

		return query.getResultList();
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

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#getAllDiagnosisAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<DiagnosisAttributeType> getAllDiagnosisAttributeTypes() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(DiagnosisAttributeType.class).list();
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#getDiagnosisAttributeTypeById(Integer) 
	 */
	@Override
	@Transactional(readOnly = true)
	public DiagnosisAttributeType getDiagnosisAttributeTypeById(Integer id) throws DAOException {
		return sessionFactory.getCurrentSession().get(DiagnosisAttributeType.class, id);
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#getDiagnosisAttributeTypeByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public DiagnosisAttributeType getDiagnosisAttributeTypeByUuid(String uuid) throws DAOException {
		return (DiagnosisAttributeType) sessionFactory.getCurrentSession().createCriteria(DiagnosisAttributeType.class).add(
				Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#saveDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Override
	@Transactional
	public DiagnosisAttributeType saveDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(diagnosisAttributeType);
		return diagnosisAttributeType;
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#deleteDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Override
	@Transactional
	public void deleteDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType) throws DAOException {
		sessionFactory.getCurrentSession().delete(diagnosisAttributeType);
	}

	/**
	 * @see org.openmrs.api.db.DiagnosisDAO#getDiagnosisAttributeByUuid(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public DiagnosisAttribute getDiagnosisAttributeByUuid(String uuid) throws DAOException {
		return (DiagnosisAttribute) sessionFactory.getCurrentSession().createCriteria(DiagnosisAttribute.class).add(Restrictions.eq("uuid", uuid))
				.uniqueResult();
	}
}
