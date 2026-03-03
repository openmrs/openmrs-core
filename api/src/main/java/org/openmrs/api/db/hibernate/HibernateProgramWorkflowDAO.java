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

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.FlushMode;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientProgramAttribute;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramAttributeType;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;

/**
 * Hibernate specific ProgramWorkflow related functions.<br>
 * <br>
 * This class should not be used directly. All calls should go through the
 * {@link org.openmrs.api.ProgramWorkflowService} methods.
 *
 * @see org.openmrs.api.db.ProgramWorkflowDAO
 * @see org.openmrs.api.ProgramWorkflowService
 */
public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
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
	@Override
	public Program saveProgram(Program program) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(program);
		return program;
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgram(java.lang.Integer)
	 */
	@Override
	public Program getProgram(Integer programId) throws DAOException {
		return sessionFactory.getCurrentSession().get(Program.class, programId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getAllPrograms(boolean)
	 */
	@Override
	public List<Program> getAllPrograms(boolean includeRetired) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Program> cq = cb.createQuery(Program.class);
		Root<Program> root = cq.from(Program.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByName(String, boolean)
	 */
	@Override
	public List<Program> getProgramsByName(String programName, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Program> cq = cb.createQuery(Program.class);
		Root<Program> root = cq.from(Program.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("name"), programName));

		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}

		cq.where(cb.and(predicates.toArray(new Predicate[]{})));
		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#findPrograms(java.lang.String)
	 */
	@Override
	public List<Program> findPrograms(String nameFragment) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Program> cq = cb.createQuery(Program.class);
		Root<Program> root = cq.from(Program.class);

		Predicate nameLike = cb.like(cb.lower(root.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(nameFragment));

		cq.where(nameLike).orderBy(cb.asc(root.get("name")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deleteProgram(org.openmrs.Program)
	 */
	@Override
	public void deleteProgram(Program program) throws DAOException {
		sessionFactory.getCurrentSession().delete(program);
	}
	
	// **************************
	// PATIENT PROGRAM
	// **************************
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#savePatientProgram(org.openmrs.PatientProgram)
	 */
	@Override
	public PatientProgram savePatientProgram(PatientProgram patientProgram) throws DAOException {
                CustomDatatypeUtil.saveAttributesIfNecessary(patientProgram);

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
	@Override
	public PatientProgram getPatientProgram(Integer patientProgramId) throws DAOException {
		return sessionFactory.getCurrentSession().get(PatientProgram.class, patientProgramId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@Override
	public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate,
												   Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
		throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<PatientProgram> cq = cb.createQuery(PatientProgram.class);
		Root<PatientProgram> root = cq.from(PatientProgram.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (patient != null) {
			predicates.add(cb.equal(root.get("patient"), patient));
		}
		if (program != null) {
			predicates.add(cb.equal(root.get("program"), program));
		}
		if (minEnrollmentDate != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("dateEnrolled"), minEnrollmentDate));
		}
		if (maxEnrollmentDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("dateEnrolled"), maxEnrollmentDate));
		}
		if (minCompletionDate != null) {
			predicates.add(cb.or(
				cb.isNull(root.get("dateCompleted")),
				cb.greaterThanOrEqualTo(root.get("dateCompleted"), minCompletionDate)
			));
		}
		if (maxCompletionDate != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("dateCompleted"), maxCompletionDate));
		}
		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		cq.where(cb.and(predicates.toArray(new Predicate[]{})))
			.orderBy(cb.asc(root.get("dateEnrolled")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort,
	 *      java.util.Collection)
	 */
	@Override
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
			query.setParameter("patientIds", cohort.getMemberIds());
		}
		if (programs != null) {
			query.setParameter("programs", programs);
		}
		return query.getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deletePatientProgram(org.openmrs.PatientProgram)
	 */
	@Override
	public void deletePatientProgram(PatientProgram patientProgram) throws DAOException {
		sessionFactory.getCurrentSession().delete(patientProgram);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#saveConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	@Override
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
	@Override
	public List<ConceptStateConversion> getAllConceptStateConversions() throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ConceptStateConversion> cq = cb.createQuery(ConceptStateConversion.class);
		cq.from(ConceptStateConversion.class);

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
	 */
	@Override
	public ConceptStateConversion getConceptStateConversion(Integer conceptStateConversionId) {
		return sessionFactory.getCurrentSession().get(ConceptStateConversion.class,
		    conceptStateConversionId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#deleteConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	@Override
	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().delete(csc);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(org.openmrs.ProgramWorkflow,
	 *      org.openmrs.Concept)
	 */
	@Override
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		if (workflow == null || trigger == null) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ConceptStateConversion> cq = cb.createQuery(ConceptStateConversion.class);
		Root<ConceptStateConversion> root = cq.from(ConceptStateConversion.class);

		cq.where(cb.and(
			cb.equal(root.get("programWorkflow"), workflow),
			cb.equal(root.get("concept"), trigger)
		));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversionByUuid(java.lang.String)
	 */
	@Override
	public ConceptStateConversion getConceptStateConversionByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, ConceptStateConversion.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientProgramByUuid(java.lang.String)
	 */
	@Override
	public PatientProgram getPatientProgramByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, PatientProgram.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramByUuid(java.lang.String)
	 */
	@Override
	public Program getProgramByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Program.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getState(Integer)
	 */
	@Override
	public ProgramWorkflowState getState(Integer stateId) {
		return sessionFactory.getCurrentSession().get(ProgramWorkflowState.class, stateId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getStateByUuid(java.lang.String)
	 */
	@Override
	public ProgramWorkflowState getStateByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, ProgramWorkflowState.class, uuid);
	}
	
	@Override
	public PatientState getPatientStateByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, PatientState.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getWorkflow(Integer)
	 */
	@Override
	public ProgramWorkflow getWorkflow(Integer workflowId) {
		return sessionFactory.getCurrentSession().get(ProgramWorkflow.class, workflowId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getWorkflowByUuid(java.lang.String)
	 */
	@Override
	public ProgramWorkflow getWorkflowByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, ProgramWorkflow.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<Program> getProgramsByConcept(Concept concept) {
		String pq = "select distinct p from Program p where p.concept = :concept";
		Query pquery = sessionFactory.getCurrentSession().createQuery(pq);
		pquery.setParameter("concept", concept);
		return pquery.getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept) {
		String wq = "select distinct w from ProgramWorkflow w where w.concept = :concept";
		Query wquery = sessionFactory.getCurrentSession().createQuery(wq);
		wquery.setParameter("concept", concept);
		return wquery.getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowStatesByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept) {
		String sq = "select distinct s from ProgramWorkflowState s where s.concept = :concept";
		Query squery = sessionFactory.getCurrentSession().createQuery(sq);
		squery.setParameter("concept", concept);
		return squery.getResultList();
	}
        
	@Override
	public List<ProgramAttributeType> getAllProgramAttributeTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ProgramAttributeType> cq = cb.createQuery(ProgramAttributeType.class);
		cq.from(ProgramAttributeType.class);

		return session.createQuery(cq).getResultList();
	}

	@Override
	public ProgramAttributeType getProgramAttributeType(Integer id) {
		return sessionFactory.getCurrentSession().get(ProgramAttributeType.class, id);
	}

	@Override
	public ProgramAttributeType getProgramAttributeTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, ProgramAttributeType.class, uuid);
	}

	@Override
	public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType programAttributeType) {
		sessionFactory.getCurrentSession().saveOrUpdate(programAttributeType);
		return programAttributeType;
	}

	@Override
	public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, PatientProgramAttribute.class, uuid);
	}

	@Override
	public void purgeProgramAttributeType(ProgramAttributeType type) {
		sessionFactory.getCurrentSession().delete(type);
	}

	@Override
	public List<PatientProgram> getPatientProgramByAttributeNameAndValue(String attributeName, String attributeValue) {
		FlushMode flushMode = sessionFactory.getCurrentSession().getHibernateFlushMode();
		sessionFactory.getCurrentSession().setHibernateFlushMode(FlushMode.MANUAL);
		Query query;
		try {
			query = sessionFactory.getCurrentSession().createQuery(
					"SELECT pp FROM patient_program pp " +
							"INNER JOIN pp.attributes attr " +
							"INNER JOIN attr.attributeType attr_type " +
							"WHERE attr.valueReference = :attributeValue " +
							"AND attr_type.name = :attributeName " +
							"AND pp.voided = 0")
					.setParameter("attributeName", attributeName)
					.setParameter("attributeValue", attributeValue);
			return query.getResultList();
		} finally {
			sessionFactory.getCurrentSession().setHibernateFlushMode(flushMode);
		}
	}

	@Override
	public Map<Object, Object> getPatientProgramAttributeByAttributeName(List<Integer> patientIds, String attributeName) {
		Map<Object, Object> patientProgramAttributes = new HashMap<>();
		if (patientIds.isEmpty() || attributeName == null) {
			return patientProgramAttributes;
		}
		String commaSeperatedPatientIds = StringUtils.join(patientIds, ",");
		List<Object> list = sessionFactory.getCurrentSession().createSQLQuery(
				"SELECT p.patient_id as person_id, " +
						" concat('{',group_concat(DISTINCT (coalesce(concat('\"',ppt.name,'\":\"', COALESCE (cn.name, ppa.value_reference),'\"'))) SEPARATOR ','),'}') AS patientProgramAttributeValue  " +
						" from patient p " +
						" join patient_program pp on p.patient_id = pp.patient_id and p.patient_id in (" + commaSeperatedPatientIds + ")" +
						" join patient_program_attribute ppa on pp.patient_program_id = ppa.patient_program_id and ppa.voided=0" +
						" join program_attribute_type ppt on ppa.attribute_type_id = ppt.program_attribute_type_id and ppt.name ='" + attributeName + "' "+
						" LEFT OUTER JOIN concept_name cn on ppa.value_reference = cn.concept_id and cn.concept_name_type= 'FULLY_SPECIFIED' and cn.voided=0 and ppt.datatype like '%ConceptDataType%'" +
						" group by p.patient_id")
				.addScalar("person_id", StandardBasicTypes.INTEGER)
				.addScalar("patientProgramAttributeValue", StandardBasicTypes.STRING)
				.list();

		for (Object o : list) {
			Object[] arr = (Object[]) o;
			patientProgramAttributes.put(arr[0], arr[1]);
		}

		return patientProgramAttributes;

	}
}
