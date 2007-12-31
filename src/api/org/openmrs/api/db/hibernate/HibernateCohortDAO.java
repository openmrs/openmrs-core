package org.openmrs.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Cohort;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.api.db.DAOException;

public class HibernateCohortDAO implements CohortDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	public HibernateCohortDAO() { }
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void createCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().persist(cohort);
	}

	public Cohort getCohort(Integer id) throws DAOException {
		return (Cohort) sessionFactory.getCurrentSession().get(Cohort.class, id);
	}
	
	public List<Cohort> getCohorts() throws DAOException {
		return (List<Cohort>) sessionFactory.getCurrentSession().createQuery("from Cohort order by name").list();
	}
	
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws DAOException {
		Query query = sessionFactory.getCurrentSession().createQuery("from Cohort c where :patientId in elements(c.memberIds)");
		query.setInteger("patientId", patientId);
		return (List<Cohort>) query.list();
	}

    public void updateCohort(Cohort cohort) throws DAOException {
    	sessionFactory.getCurrentSession().update(cohort);
    }

}
