package org.openmrs.api.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.DataEntryStatistic;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;

/**
 * Admin-related database functions
 * @version 1.0
 */
public interface AdministrationDAO {
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @throws DAOException
	 */
	public void createEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws DAOException
	 */
	public void updateEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws DAOException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws DAOException;

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @throws DAOException
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws DAOException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws DAOException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException;

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws DAOException
	 */
	public void createTribe(Tribe tribe) throws DAOException;

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws DAOException
	 */
	public void updateTribe(Tribe tribe) throws DAOException;

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws DAOException
	 */
	public void deleteTribe(Tribe tribe) throws DAOException;	
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws DAOException
	 */
	public void retireTribe(Tribe tribe) throws DAOException;	

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws DAOException
	 */
	public void unretireTribe(Tribe tribe) throws DAOException;	
	
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @throws DAOException
	 */
	public void createFieldType(FieldType fieldType) throws DAOException;

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws DAOException
	 */
	public void updateFieldType(FieldType fieldType) throws DAOException;

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws DAOException
	 */
	public void deleteFieldType(FieldType fieldType) throws DAOException;
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @throws DAOException
	 */
	public void createMimeType(MimeType mimeType) throws DAOException;

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws DAOException
	 */
	public void updateMimeType(MimeType mimeType) throws DAOException;

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws DAOException
	 */
	public void deleteMimeType(MimeType mimeType) throws DAOException;	

	/**
	 * Create a new Location
	 * @param Location to create
	 * @throws DAOException
	 */
	public void createLocation(Location location) throws DAOException;

	/**
	 * Update Location
	 * @param Location to update
	 * @throws DAOException
	 */
	public void updateLocation(Location location) throws DAOException;

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws DAOException
	 */
	public void deleteLocation(Location location) throws DAOException;	
	
	/**
	 * Create a new Role
	 * @param Role to create
	 * @throws DAOException
	 */
	public void createRole(Role role) throws DAOException;

	/**
	 * Update Role
	 * @param Role to update
	 * @throws DAOException
	 */
	public void updateRole(Role role) throws DAOException;

	/**
	 * Delete Role
	 * @param Role to delete
	 * @throws DAOException
	 */
	public void deleteRole(Role role) throws DAOException;	

	/**
	 * Create a new Privilege
	 * @param Privilege to create
	 * @throws DAOException
	 */
	public void createPrivilege(Privilege privilege) throws DAOException;

	/**
	 * Update Privilege
	 * @param Privilege to update
	 * @throws DAOException
	 */
	public void updatePrivilege(Privilege privilege) throws DAOException;

	/**
	 * Delete Privilege
	 * @param Privilege to delete
	 * @throws DAOException
	 */
	public void deletePrivilege(Privilege privilege) throws DAOException;	

	/**
	 * Create a new ConceptClass
	 * @param ConceptClass to create
	 * @throws DAOException
	 */
	public void createConceptClass(ConceptClass cc) throws DAOException;

	/**
	 * Update ConceptClass
	 * @param ConceptClass to update
	 * @throws DAOException
	 */
	public void updateConceptClass(ConceptClass cc) throws DAOException;

	/**
	 * Delete ConceptClass
	 * @param ConceptClass to delete
	 * @throws DAOException
	 */
	public void deleteConceptClass(ConceptClass cc) throws DAOException;	

	/**
	 * Create a new ConceptDatatype
	 * @param ConceptDatatype to create
	 * @throws DAOException
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws DAOException;

	/**
	 * Update ConceptDatatype
	 * @param ConceptDatatype to update
	 * @throws DAOException
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws DAOException;

	/**
	 * Delete ConceptDatatype
	 * @param ConceptDatatype to delete
	 * @throws DAOException
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws DAOException;	
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws DAOException
	 */
	public void createReport(Report r) throws DAOException;

	/**
	 * Update Report
	 * @param Report to update
	 * @throws DAOException
	 */
	public void updateReport(Report r) throws DAOException;

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws DAOException
	 */
	public void deleteReport(Report r) throws DAOException;
	
	/**
	 * Create a new Report Object
	 * @param Report Object to create
	 * @throws DAOException
	 */
	public void createReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Update Report Object
	 * @param Report Object to update
	 * @throws DAOException
	 */
	public void updateReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Delete Report Object
	 * @param Report Objectto delete
	 * @throws DAOException
	 */
	public void deleteReportObject(Integer reportObjectId) throws DAOException;

	/**
	 * Iterates over the words in names and synonyms (for each locale) and updates the concept word business table 
	 * @param concept
	 * @throws DAOException
	 */
	public void updateConceptWord(Concept concept) throws DAOException;
	
	/**
	 * Updates the concept set derived business table for this concept (bursting the concept sets) 
	 * @param concept
	 * @throws DAOException
	 */
	public void updateConceptSetDerived(Concept concept) throws DAOException;
	
	/**
	 * Iterates over all concepts calling updateConceptSetDerived(concept)
	 * @throws DAOException
	 */
	public void updateConceptSetDerived() throws DAOException;
	
	public void createConceptProposal(ConceptProposal cp) throws DAOException;
	
	public void updateConceptProposal(ConceptProposal cp) throws DAOException;
	
	public void mrnGeneratorLog(String site, Integer start, Integer count) throws DAOException;
	
	public Collection getMRNGeneratorLog() throws DAOException;
	
	public String getGlobalProperty(String propertyName) throws DAOException;

	public List<GlobalProperty> getGlobalProperties() throws DAOException;

	public void setGlobalProperties(List<GlobalProperty> props) throws DAOException;
	
	public void deleteGlobalProperty(String propertyName) throws DAOException;

	public void setGlobalProperty(GlobalProperty gp) throws DAOException;

	public void addGlobalProperty(String propertyName, String propertyValue) throws DAOException;

	public List<DataEntryStatistic> getDataEntryStatistics(Date fromDate, Date toDate, String encounterUserColumn, String orderUserColumn, String groupBy) throws DAOException;

	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException;
}