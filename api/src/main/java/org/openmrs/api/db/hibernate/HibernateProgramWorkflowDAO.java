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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
		return (Program) sessionFactory.getCurrentSession().get(Program.class, programId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getAllPrograms(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Program> getAllPrograms(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Program.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByName(String, boolean)
	 */
	@Override
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
	@Override
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
		return (PatientProgram) sessionFactory.getCurrentSession().get(PatientProgram.class, patientProgramId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@Override
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
	@SuppressWarnings("unchecked")
	public List<ConceptStateConversion> getAllConceptStateConversions() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
	 */
	@Override
	public ConceptStateConversion getConceptStateConversion(Integer conceptStateConversionId) {
		return (ConceptStateConversion) sessionFactory.getCurrentSession().get(ConceptStateConversion.class,
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
	@Override
	public ConceptStateConversion getConceptStateConversionByUuid(String uuid) {
		return (ConceptStateConversion) sessionFactory.getCurrentSession().createQuery(
		    "from ConceptStateConversion csc where csc.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getPatientProgramByUuid(java.lang.String)
	 */
	@Override
	public PatientProgram getPatientProgramByUuid(String uuid) {
		return (PatientProgram) sessionFactory.getCurrentSession().createQuery(
		    "from PatientProgram pp where pp.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramByUuid(java.lang.String)
	 */
	@Override
	public Program getProgramByUuid(String uuid) {
		return (Program) sessionFactory.getCurrentSession().createQuery("from Program p where p.uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getState(Integer)
	 */
	@Override
	public ProgramWorkflowState getState(Integer stateId) {
		return (ProgramWorkflowState) sessionFactory.getCurrentSession().get(ProgramWorkflowState.class, stateId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getStateByUuid(java.lang.String)
	 */
	@Override
	public ProgramWorkflowState getStateByUuid(String uuid) {
		return (ProgramWorkflowState) sessionFactory.getCurrentSession().createQuery(
		    "from ProgramWorkflowState pws where pws.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public PatientState getPatientStateByUuid(String uuid) {
		return (PatientState) sessionFactory.getCurrentSession().createQuery("from PatientState pws where pws.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getWorkflow(Integer)
	 */
	@Override
	public ProgramWorkflow getWorkflow(Integer workflowId) {
		return (ProgramWorkflow) sessionFactory.getCurrentSession().get(ProgramWorkflow.class, workflowId);
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getWorkflowByUuid(java.lang.String)
	 */
	@Override
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
        
        @Override
        public List<ProgramAttributeType> getAllProgramAttributeTypes() {
            return sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).list();
        }

        @Override
        public ProgramAttributeType getProgramAttributeType(Integer id) {
            return (ProgramAttributeType) sessionFactory.getCurrentSession().get(ProgramAttributeType.class, id);
        }

        @Override
        public ProgramAttributeType getProgramAttributeTypeByUuid(String uuid) {
            return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).add(
                    Restrictions.eq("uuid", uuid)).uniqueResult();
        }

        @Override
        public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType programAttributeType) {
            sessionFactory.getCurrentSession().saveOrUpdate(programAttributeType);
            return programAttributeType;
        }

        @Override
        public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
            return (PatientProgramAttribute) sessionFactory.getCurrentSession().createCriteria(PatientProgramAttribute.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
        }

        @Override
        public void purgeProgramAttributeType(ProgramAttributeType type) {
            sessionFactory.getCurrentSession().delete(type);
        }

        @Override
        public List<PatientProgram> getPatientProgramByAttributeNameAndValue(String attributeName, String attributeValue) {
            FlushMode flushMode = sessionFactory.getCurrentSession().getFlushMode();
            sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
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
                return query.list();
            } finally {
                sessionFactory.getCurrentSession().setFlushMode(flushMode);
            }
        }

        @Override
        public Map<Object, Object> getPatientProgramAttributeByAttributeName(List<Integer> patientIds, String attributeName) {
            Map<Object, Object> patientProgramAttributes = new HashMap<>();
            if (patientIds.isEmpty() || attributeName == null) {
                return patientProgramAttributes;
            }
            String commaSeperatedPatientIds = StringUtils.join(patientIds, ",");
            List list = sessionFactory.getCurrentSession().createSQLQuery(
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
