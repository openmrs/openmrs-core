package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.reporting.PatientSet;

public class HibernatePatientSetDAO implements PatientSetDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientSetDAO(Context c) {
		this.context = c;
	}
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Query query;
		
		StringBuffer queryString = new StringBuffer("select patientId from Patient patient");
		List<String> clauses = new ArrayList<String>();

		clauses.add("patient.voided = false");
		
		if (gender != null) {
			gender = gender.toUpperCase();
			clauses.add("patient.gender = :gender");
		}
		if (minBirthdate != null) {
			clauses.add("patient.birthdate >= :minBirthdate");
		}
		if (maxBirthdate != null) {
			clauses.add("patient.birthdate <= :maxBirthdate");
		}
		
		boolean first = true;
		for (String clause : clauses) {
			if (first) {
				queryString.append(" where ").append(clause);
				first = false;
			} else {
				queryString.append(" and ").append(clause);
			}
		}
		query = session.createQuery(queryString.toString());
		if (gender != null) {
			query.setString("gender", gender);
		}
		if (minBirthdate != null) {
			query.setDate("minBirthdate", minBirthdate);
		}
		if (maxBirthdate != null) {
			query.setDate("maxBirthdate", maxBirthdate);
		}
		
		List<Integer> patientIds = query.list();
		
		PatientSet ret = new PatientSet();
		ret.setPatientIds(new HashSet<Integer>(patientIds));

		HibernateUtil.commitTransaction();
		
		return ret;
	}

	private static final long MS_PER_YEAR = 365l * 24 * 60 * 60 * 1000l; 
	
	public Map<Integer, String> getShortPatientDescriptions(PatientSet patients) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Map<Integer, String> ret = new HashMap<Integer, String>();
		
		Set<Integer> ids = patients.getPatientIds();
		Query query = session.createQuery("select patient.patientId, patient.gender, patient.birthdate from Patient patient");
		
		List<Object[]> temp = query.list();
		
		long now = System.currentTimeMillis();
		for (Object[] results : temp) {
			if (!ids.contains(results[0])) { continue; }
			StringBuffer sb = new StringBuffer();
			if ("M".equals(results[1])) {
				sb.append("Male");
			} else {
				sb.append("Female");
			}
			Date bd = (Date) results[2];
			if (bd != null) {
				int age = (int) ((now - bd.getTime()) / MS_PER_YEAR);
				sb.append(", ").append(age).append(" years old");
			}
			ret.put((Integer) results[0], sb.toString()); 
		}
		
		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
}
