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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;

/**
 * Hibernate specific ProgramWorkflow related functions.<br/>
 * <br/>
 * This class should not be used directly. All calls should go through the
 * {@link org.openmrs.api.ProgramWorkflowService} methods.
 *
 * @see org.openmrs.api.db.ProgramWorkflowDAO
 * @see org.openmrs.api.ProgramWorkflowService
 */
public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	public HibernateProgramWorkflowDAO() {
	}
	
	/**
	 * Hibernate Session Factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	// **************************
	// PROGRAM
	// **************************
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#saveProgram(org.openmrs.Program)
	 */
	public Program saveProgram(Program program) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(program);
		return program;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgram(java.lang.Integer)
	 */
	public Program getProgram(Integer programId) throws DAOException {
		return (Program) sessionFactory.getCurrentSession().get(Program.class, programId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getAllPrograms(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Program> getAllPrograms(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Program.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByName(java.lang.String)
	 */
	public List<Program> getProgramsByName(String programName, boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Program.class);
		criteria.add(Restrictions.eq("name", programName));
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		
		@SuppressWarnings("unchecked")
		List<Program> list = criteria.list();
		
		return list;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#findPrograms(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Program> findPrograms(String nameFragment) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Program.class, "program");
		criteria.add(Restrictions.ilike("name", nameFragment, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deleteProgram(org.openmrs.Program)
	 */
	public void deleteProgram(Program program) throws DAOException {
		sessionFactory.getCurrentSession().delete(program);
	}
	
	// **************************
	// PATIENT PROGRAM
	// **************************
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#savePatientProgram(org.openmrs.PatientProgram)
	 */
	public PatientProgram savePatientProgram(PatientProgram patientProgram) throws DAOException {
		if (patientProgram.getPatientProgramId() == null) {
			sessionFactory.getCurrentSession().save(patientProgram);
		} else {
			sessionFactory.getCurrentSession().merge(patientProgram);
		}
		return patientProgram;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientProgram(java.lang.Integer)
	 */
	public PatientProgram getPatientProgram(Integer patientProgramId) throws DAOException {
		return (PatientProgram) sessionFactory.getCurrentSession().get(PatientProgram.class, patientProgramId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate,
	        Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
	        throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class);
		if (patient != null) {
			crit.add(Restrictions.eq("patient", patient));
		}
		if (program != null) {
			crit.add(Restrictions.eq("program", program));
		}
		if (minEnrollmentDate != null) {
			crit.add(Restrictions.ge("dateEnrolled", minEnrollmentDate));
		}
		if (maxEnrollmentDate != null) {
			crit.add(Restrictions.le("dateEnrolled", maxEnrollmentDate));
		}
		if (minCompletionDate != null) {
			crit.add(Restrictions.or(Restrictions.isNull("dateCompleted"), Restrictions.ge("dateCompleted",
			    minCompletionDate)));
		}
		if (maxCompletionDate != null) {
			crit.add(Restrictions.le("dateCompleted", maxCompletionDate));
		}
		if (!includeVoided) {
			crit.add(Restrictions.eq("voided", false));
		}
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort,
	 *      java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs) {
		String hql = "from PatientProgram ";
		if (cohort != null || programs != null) {
			hql += "where ";
		}
		if (cohort != null) {
			hql += "patient.patientId in (:patientIds) ";
		}
		if (programs != null) {
			if (cohort != null) {
				hql += "and ";
			}
			hql += " program in (:programs)";
		}
		hql += " order by patient.patientId, dateEnrolled";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		if (cohort != null) {
			query.setParameterList("patientIds", cohort.getMemberIds());
		}
		if (programs != null) {
			query.setParameterList("programs", programs);
		}
		return query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deletePatientProgram(org.openmrs.PatientProgram)
	 */
	public void deletePatientProgram(PatientProgram patientProgram) throws DAOException {
		sessionFactory.getCurrentSession().delete(patientProgram);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#saveConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	public ConceptStateConversion saveConceptStateConversion(ConceptStateConversion csc) throws DAOException {
		if (csc.getConceptStateConversionId() == null) {
			sessionFactory.getCurrentSession().save(csc);
		} else {
			sessionFactory.getCurrentSession().merge(csc);
		}
		return csc;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getAllConceptStateConversions()
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptStateConversion> getAllConceptStateConversions() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
	 */
	public ConceptStateConversion getConceptStateConversion(Integer conceptStateConversionId) {
		return (ConceptStateConversion) sessionFactory.getCurrentSession().get(ConceptStateConversion.class,
		    conceptStateConversionId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deleteConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().delete(csc);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(org.openmrs.ProgramWorkflow,
	 *      org.openmrs.Concept)
	 */
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		ConceptStateConversion csc = null;
		
		if (workflow != null && trigger != null) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class, "csc");
			criteria.add(Restrictions.eq("csc.programWorkflow", workflow));
			criteria.add(Restrictions.eq("csc.concept", trigger));
			csc = (ConceptStateConversion) criteria.uniqueResult();
		}
		
		return csc;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversionByUuid(java.lang.String)
	 */
	public ConceptStateConversion getConceptStateConversionByUuid(String uuid) {
		return (ConceptStateConversion) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptStateConversion csc where csc.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientProgramByUuid(java.lang.String)
	 */
	public PatientProgram getPatientProgramByUuid(String uuid) {
		return (PatientProgram) sessionFactory.getCurrentSession().createQuery(
		    "from PatientProgram pp where pp.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramByUuid(java.lang.String)
	 */
	public Program getProgramByUuid(String uuid) {
		return (Program) sessionFactory.getCurrentSession().createQuery("from Program p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getStateByUuid(java.lang.String)
	 */
	public ProgramWorkflowState getStateByUuid(String uuid) {
		return (ProgramWorkflowState) sessionFactory.getCurrentSession().createQuery(
		    "from ProgramWorkflowState pws where pws.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	public PatientState getPatientStateByUuid(String uuid) {
		return (PatientState) sessionFactory.getCurrentSession().createQuery("from PatientState pws where pws.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getWorkflowByUuid(java.lang.String)
	 */
	public ProgramWorkflow getWorkflowByUuid(String uuid) {
		return (ProgramWorkflow) sessionFactory.getCurrentSession().createQuery(
		    "from ProgramWorkflow pw where pw.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<Program> getProgramsByConcept(Concept concept) {
		String pq = "select distinct p from Program p where p.concept = :concept";
		Query pquery = sessionFactory.getCurrentSession().createQuery(pq);
		pquery.setEntity("concept", concept);
		return pquery.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept) {
		String wq = "select distinct w from ProgramWorkflow w where w.concept = :concept";
		Query wquery = sessionFactory.getCurrentSession().createQuery(wq);
		wquery.setEntity("concept", concept);
		return wquery.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowStatesByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept) {
		String sq = "select distinct s from ProgramWorkflowState s where s.concept = :concept";
		Query squery = sessionFactory.getCurrentSession().createQuery(sq);
		squery.setEntity("concept", concept);
		return squery.list();
	}
}
