package org.openmrs.api.db.hibernate;

import java.util.List;

import org.hibernate.Query;
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
	 */
	@Override
	public void mergePatientIdentifier(PatientIdentifierType main, PatientIdentifierType toBeMerged) throws DAOException {
		String nameOfMain = main.getName();
		String nameOfToBeMerged = toBeMerged.getName();
		// Merge the patient identifiers of nameOfMain and nameOfToBeMerged
		String queryString = "UPDATE patient_identifier SET identifier_type = (SELECT patient_identifier_type_id FROM patient_identifier_type WHERE NAME = :mainName) " + 
				"WHERE identifier_type = (SELECT patient_identifier_type_id FROM patient_identifier_type WHERE NAME = :mergedName)";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("mainName", nameOfMain);
		query.setParameter("mergedName", nameOfToBeMerged);
		query.executeUpdate();

		// Delete the patientIdentifierType that was merged
		String deleteString = "DELETE FROM patient_identifier_type WHERE NAME = :mergedName";
		SQLQuery deleteQuery = sessionFactory.getCurrentSession().createSQLQuery(deleteString);
		deleteQuery.setParameter("mergedName", nameOfToBeMerged);
		deleteQuery.executeUpdate();
		}
	}

