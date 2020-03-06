package org.openmrs.api.db.hibernate;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientIdentifierMergeDAO;

public class HibernatePatientIdentifierMergeDAO implements PatientIdentifierMergeDAO {
	
	/**
	 * Hibernate session factory
	 */
	protected SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
     * @param main  main patient identifier type to be merged into
     * @param toBeMerged a list of patient identifier types that are merged to main
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Integer)
	 */
	@Override
	public void mergePatientIdentifier(PatientIdentifierType main, List<PatientIdentifierType> toBeMerged) throws DAOException {
		String nameOfMain = main.getName();
		for (int i = 0; i < toBeMerged.size(); i++) {
			// Change identifier_type in patient_identifier to the merged one
			String nameOfToBeMerged = toBeMerged.get(i).getName();
			String queryString = "UPDATE patient_identifier SET identifier_type = (SELECT patient_identifier_type_id FROM patient_identifier_type WHERE NAME = '" + nameOfMain + "') WHERE identifier_type = (SELECT patient_identifier_type_id FROM patient_identifier_type WHERE NAME = '" + nameOfToBeMerged + "');";
			System.out.print(queryString);
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
			query.executeUpdate();
			// Delete the patientIdentifierType that was merged
			sessionFactory.getCurrentSession().delete(toBeMerged.get(i));
		}
	}
	
	

}
