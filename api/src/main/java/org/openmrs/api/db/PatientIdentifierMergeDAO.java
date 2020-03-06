package org.openmrs.api.db;

import java.util.List;

import org.openmrs.PatientIdentifierType;

/**
 * Database methods for the PatientIdentifierMerge
 * 
 * @see org.openmrs.api.context.Context
 */
public interface PatientIdentifierMergeDAO {
	
	public void mergePatientIdentifier(PatientIdentifierType main, List<PatientIdentifierType> toBeMerged) throws DAOException ;
	
}
