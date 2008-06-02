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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;

/**
 * Hibernate specific ProgramWorkflow related functions
 * 
 * This class should not be used directly.  All calls should go through the
 * {@link org.openmrs.api.ProgramWorkflowService} methods.
 * 
 * @see org.openmrs.api.db.ProgramWorkflowDAO
 * @see org.openmrs.api.ProgramWorkflowService
 */
public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
	protected final Log log = LogFactory.getLog(getClass());

	private SessionFactory sessionFactory;
	
	public HibernateProgramWorkflowDAO() { }
	
	/**
	 * Hibernate Session Factory
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
		if (includeRetired == false) {
			criteria.add(Expression.eq("retired", false));
		}
		return criteria.list();
	}

	/**
     * @see org.openmrs.api.db.ProgramWorkflowDAO#findPrograms(java.lang.String)
     */
	@SuppressWarnings("unchecked")
    public List<Program> findPrograms(String nameFragment) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Program.class, "program");
		criteria.add(Expression.ilike("name", nameFragment, MatchMode.ANYWHERE));
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
		}
		else {
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
     * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date, java.util.Date, java.util.Date)
     */
	@SuppressWarnings("unchecked")
    public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate, Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided) throws DAOException {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class);
        if (patient != null) {
        	crit.add(Expression.eq("patient", patient));
        }
        if (program != null) {
        	crit.add(Expression.eq("program", program));
        }
        if (minEnrollmentDate != null) {
        	crit.add(Expression.ge("dateEnrolled", minEnrollmentDate));
    }
        if (maxEnrollmentDate != null) {
        	crit.add(Expression.le("dateEnrolled", maxEnrollmentDate));
	}
        if (minCompletionDate != null) {
        	crit.add(Expression.or(Expression.isNull("dateCompleted"), Expression.ge("dateCompleted", minCompletionDate)));
	}
        if (maxCompletionDate != null) {
        	crit.add(Expression.le("dateCompleted", maxCompletionDate));
	}
        if (!includeVoided) {
			crit.add(Expression.eq("voided", false));
        }
		return crit.list();
	}

    /**
     * TODO: refactor this
     * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort, java.util.Collection)
     */
    public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs) {
		String hql = "from PatientProgram ";
		if (cohort != null || programs != null)
			hql += "where ";
		if (cohort != null)
			hql += "patient.patientId in (:patientIds) ";
		if (programs != null) {
			if (cohort != null)
				hql += "and ";
			hql += " program in (:programs)";
		}
		hql += " order by patient.patientId, dateEnrolled";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		if (cohort != null)
			query.setParameterList("patientIds", cohort.getMemberIds());
		if (programs != null)
			query.setParameterList("programs", programs);
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
		}
		else {
			sessionFactory.getCurrentSession().merge(csc);
		}
		return csc;
	}
	
	/**
     * @see org.openmrs.api.db.ProgramWorkflowDAO#getAllConceptStateConversions(boolean)
     */
    @SuppressWarnings("unchecked")
    public List<ConceptStateConversion> getAllConceptStateConversions() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class).list();
	}

    /**
     * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
     */
	public ConceptStateConversion getConceptStateConversion(Integer conceptStateConversionId) {
		return (ConceptStateConversion) sessionFactory.getCurrentSession().get(ConceptStateConversion.class, conceptStateConversionId);
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deleteConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().delete(csc);	
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(org.openmrs.ProgramWorkflow, org.openmrs.Concept)
	 */
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		ConceptStateConversion csc = null;

		if ( workflow != null && trigger != null ) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class, "csc");
			criteria.add(Expression.eq("csc.programWorkflow", workflow));
			criteria.add(Expression.eq("csc.concept", trigger));
			csc = (ConceptStateConversion) criteria.uniqueResult();
		}
		
		return csc;
	}	
}
