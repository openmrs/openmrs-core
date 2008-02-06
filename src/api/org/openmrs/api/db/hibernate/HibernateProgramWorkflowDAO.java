package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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

public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateProgramWorkflowDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}

	public void createOrUpdateProgram(Program program) throws DAOException {
		log.debug("Creating or updating program " + program);
		if (program.getWorkflows() != null)
			log.debug("\twith " + program.getWorkflows().size() + " workflows: " + program.getWorkflows());
		
		if (program.getProgramId() == null)
			sessionFactory.getCurrentSession().save(program);
		else
			sessionFactory.getCurrentSession().merge(program);
		
	}

	public Program getProgram(Integer id) throws DAOException {
		return (Program) sessionFactory.getCurrentSession().get(Program.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Program> getPrograms() throws DAOException {
		List<Program> programs = new ArrayList<Program>();
		programs.addAll(sessionFactory.getCurrentSession().createQuery("from Program").list());
		return programs;
	}

	public ProgramWorkflow findWorkflowByProgramAndConcept(Integer programId, Integer conceptId) throws DAOException {
		ProgramWorkflow w = null;
		w = (ProgramWorkflow) sessionFactory.getCurrentSession().createQuery("from ProgramWorkflow w where w.program.programid = :programId and w.concept.conceptId = :conceptId")
			.setInteger("programId", programId)
			.setInteger("conceptId", conceptId)
			.uniqueResult();
		
		return w;
	}

	public void createPatientProgram(PatientProgram p) throws DAOException {
		if (p.getPatient() == null || p.getPatient().getPatientId() == null
				|| p.getProgram() == null || p.getProgram().getProgramId() == null) {
			throw new DAOException("can't create a PatientProgram without already-persisted patient and program");
		}
		sessionFactory.getCurrentSession().save(p);
		
	}
	
	public void updatePatientProgram(PatientProgram p) throws DAOException {
		sessionFactory.getCurrentSession().update(p);
	}

	public PatientProgram getPatientProgram(Integer id) throws DAOException {
		return (PatientProgram) sessionFactory.getCurrentSession().get(PatientProgram.class, id);
	}

	
	public PatientState getPatientState(Integer id) throws DAOException {
		return (PatientState)sessionFactory.getCurrentSession().get(PatientState.class, id);
	}

	@SuppressWarnings("unchecked")
	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.addOrder(org.hibernate.criterion.Order.desc("dateEnrolled"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("dateCompleted"));
		patientPrograms.addAll(criteria.list());
		
		return patientPrograms;
	}
	
	public ProgramWorkflow getWorkflow(Integer id) {
		return (ProgramWorkflow) sessionFactory.getCurrentSession().get(ProgramWorkflow.class, id);
	}

	public void createWorkflow(ProgramWorkflow w) {
		sessionFactory.getCurrentSession().save(w);
	}
	
	public void updateWorkflow(ProgramWorkflow w) {
		sessionFactory.getCurrentSession().update(w);	
	}
	
	public List<ProgramWorkflowState> getStates(boolean includeVoided) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ProgramWorkflowState.class);
		if (!includeVoided)
			crit.add(Expression.eq("voided", false));
		crit.addOrder(Order.asc("programWorkflow.programWorkflowId"));
		crit.addOrder(Order.asc("programWorkflowStateId"));
		return crit.list();
	}

	public ProgramWorkflowState getState(Integer id) {
		return (ProgramWorkflowState) sessionFactory.getCurrentSession().get(ProgramWorkflowState.class, id);
	}

	@SuppressWarnings("unchecked")
	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate) {
		String sql = "select patient_id " +
				"from patient_program " +
				"where voided = false " +
				"  and program_id = :programId ";
		if (toDate != null) {
			sql += "and (date_enrolled is null or date_enrolled <= :toDate) ";
		}
		if (fromDate != null) {
			sql += "and (date_completed is null or date_completed >= :fromDate) ";
		}
		Query q = sessionFactory.getCurrentSession().createSQLQuery(sql);
		q.setInteger("programId", program.getProgramId());
		if (toDate != null)
			q.setDate("toDate", toDate);
		if (fromDate != null)
			q.setDate("fromDate", fromDate);
		Set<Integer> ret = new HashSet<Integer>();
		for (Integer ptId : (List<Integer>) q.list()) {
			ret.add(ptId);
		}
		return ret;
	}
	
	public void createConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().save(csc);	
	}

	public void updateConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().update(csc);	
	}

	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		sessionFactory.getCurrentSession().delete(csc);	
	}

	public ConceptStateConversion getConceptStateConversion(Integer id) {
		log.debug("In getCsc with id of " + id.toString());
		ConceptStateConversion csc = (ConceptStateConversion) sessionFactory.getCurrentSession().get(ConceptStateConversion.class, id);
		if ( csc != null ) log.debug("Csc is " + csc);
		else log.debug("csc is null back from hibernate");
		return csc;
	}
	
	public List<ConceptStateConversion> getAllConversions() throws DAOException {
		log.debug("In getAllconversions");
		
		List<ConceptStateConversion> conversions = new ArrayList<ConceptStateConversion>();
		conversions.addAll(sessionFactory.getCurrentSession().createCriteria(ConceptStateConversion.class).list());
		
		if ( conversions == null ) log.debug("Conversions are null");
		else log.debug("conversions is size " + conversions.size());
		
		return conversions;
	}

	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		log.debug("In getCsc with workflow: " + workflow + ", and triger of " + trigger);

		ConceptStateConversion csc = null;

		if ( workflow != null && trigger != null ) {
			csc = (ConceptStateConversion)sessionFactory.getCurrentSession().createQuery("from ConceptStateConversion c where c.programWorkflow.programWorkflowId = :workflowId and c.concept.conceptId = :conceptId")
				.setInteger("workflowId", workflow.getProgramWorkflowId())
				.setInteger("conceptId", trigger.getConceptId())
				.uniqueResult();
		}
		
		return csc;
	}	
}
