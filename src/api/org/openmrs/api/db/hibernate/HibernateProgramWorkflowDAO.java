package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;

public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	private HibernateProgramWorkflowDAO() { }
	
	public HibernateProgramWorkflowDAO(Context context) {
		this.context = context;
	}

	public void createProgram(Program program) throws DAOException {
		log.debug("Creating new program");
		// log.debug("\twith " + program.getWorkflows().size() + " workflows: " + program.getWorkflows());
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			/*
			if (program.getWorkflows() != null) {
				for (ProgramWorkflow w : program.getWorkflows()) {
					session.save(w);
				}
			}
			*/
			session.save(program);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to create program", e);
			HibernateUtil.rollbackTransaction();
		}
	}

	public Program getProgram(Integer id) throws DAOException {
		log.info("Get program " + id);
		Program program = new Program();
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			program = (Program) session.get(Program.class, id);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return program;
	}

	public List<Program> getPrograms() throws DAOException {
		log.info("Getting all programs from the database");
		List<Program> programs = new ArrayList<Program>();
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			programs = session.createQuery("from Program").list();
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return programs;
	}

	public ProgramWorkflow getProgramWorkflowByConceptId(Integer id) throws DAOException {
		Session session = HibernateUtil.currentSession();
		ProgramWorkflow w = null;
		try {
			HibernateUtil.beginTransaction();
			w = (ProgramWorkflow) session.createQuery("from ProgramWorkflow w where w.concept.conceptId = :id")
				.setInteger("id", id)
				.uniqueResult();
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return w;
	}

	public void createPatientProgram(PatientProgram p) throws DAOException {
		if (p.getPatient() == null || p.getPatient().getPatientId() == null
				|| p.getProgram() == null || p.getProgram().getProgramId() == null) {
			throw new DAOException("can't create a PatientProgram without already-persisted patient and program");
		}
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.save(p);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to create PatientProgram", e);
			HibernateUtil.rollbackTransaction();
		}
	}
	
	public void updatePatientProgram(PatientProgram p) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.update(p);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to update PatientProgram", e);
			HibernateUtil.rollbackTransaction();
		}
	}

	public PatientProgram getPatientProgram(Integer id) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			PatientProgram program = (PatientProgram) session.get(PatientProgram.class, id);
			HibernateUtil.commitTransaction();
			return program;
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return null;
	}

	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			Criteria criteria = session.createCriteria(PatientProgram.class);
			criteria.add(Restrictions.eq("patient", patient));
			criteria.addOrder(org.hibernate.criterion.Order.desc("dateEnrolled"));
			criteria.addOrder(org.hibernate.criterion.Order.desc("dateCompleted"));
			patientPrograms.addAll(criteria.list());
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return patientPrograms;
	}
	
}
