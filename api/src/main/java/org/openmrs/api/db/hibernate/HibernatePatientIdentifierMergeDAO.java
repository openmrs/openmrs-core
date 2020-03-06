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

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
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
		
		// Check if there are duplicate patient identifiers. Throw DAOException if there is
		String checkDuplicates = "SELECT  COUNT(identifier) FROM patient_identifier WHERE identifier_type = :mainName or identifier_type = :mergedName GROUP BY identifier HAVING COUNT(identifier) > 1";
		SQLQuery queryDuplicates = sessionFactory.getCurrentSession().createSQLQuery(checkDuplicates);
		queryDuplicates.setParameter("mainName", main.getPatientIdentifierTypeId());
		queryDuplicates.setParameter("mergedName", toBeMerged.getPatientIdentifierTypeId());
		if (queryDuplicates.list().size() >= 1) {
			throw new DAOException("Can't automatically merge. Duplicate patient identifiers");
		}
		
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

