/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EventListeners;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.ModuleUtil;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.util.OpenmrsConstants;

/**
 * Default implementation of the administration services.  This class should
 * not be used on its own.  The current OpenMRS implementation
 * should be fetched from the Context
 * 
 * @see org.openmrs.api.AdministrationService
 * @see org.openmrs.api.context.Context
 */
public class AdministrationServiceImpl extends BaseOpenmrsService implements AdministrationService {
	
	protected Log log = LogFactory.getLog(getClass());
	
	protected AdministrationDAO dao;
	private EventListeners eventListeners;
	
	/**
	 * Default empty constructor
	 */
	public AdministrationServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#setAdministrationDAO(org.openmrs.api.db.AdministrationDAO)
	 */
	public void setAdministrationDAO(AdministrationDAO dao) {
		this.dao = dao;
	}
	
	public void setEventListeners(EventListeners eventListeners) {
		this.eventListeners = eventListeners;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createEncounterType(org.openmrs.EncounterType)
	 * @deprecated
	 */
	public void createEncounterType(EncounterType encounterType)
	        throws APIException {
		Context.getEncounterService().saveEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateEncounterType(org.openmrs.EncounterType)
	 * @deprecated
	 */
	public void updateEncounterType(EncounterType encounterType)
	        throws APIException {
		Context.getEncounterService().saveEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteEncounterType(org.openmrs.EncounterType)
	 * @deprecated
	 */
	public void deleteEncounterType(EncounterType encounterType)
	        throws APIException {
		Context.getEncounterService().purgeEncounterType(encounterType);
	}

	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	public void createPatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	public void updatePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)}
	 */
	public void deletePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().purgePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * Create a new Tribe
	 * 
	 * @param Tribe to create
	 * @throws APIException
	 */
	public void createTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		dao.createTribe(tribe);
	}

	/**
	 * Update Tribe
	 * 
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		dao.updateTribe(tribe);
	}

	/**
	 * Delete Tribe
	 * 
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		dao.deleteTribe(tribe);
	}
	
	/**
	 * Retire Tribe
	 * 
	 * @param Tribe to retire
	 * @throws APIException
	 */
	public void retireTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		dao.retireTribe(tribe);
	}

	/**
	 * Unretire Tribe
	 * 
	 * @param Tribe to unretire
	 * @throws APIException
	 */
	public void unretireTribe(Tribe tribe) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_TRIBES))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_MANAGE_TRIBES);

		dao.unretireTribe(tribe);
	}
	
	/**
	 * @deprecated
	 */
	public void createFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().saveFieldType(fieldType);
	}

	/**
	 * @deprecated
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().saveFieldType(fieldType);
	}

	/**
	 * @deprecated
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().purgeFieldType(fieldType);
	}
	
	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 */
	public void createMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().saveMimeType(mimeType);
	}

	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().saveMimeType(mimeType);
	}

	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#purgeMimeType(MimeType)}
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().purgeMimeType(mimeType);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createLocation(org.openmrs.Location)
	 * @deprecated
	 */
	public void createLocation(Location location) throws APIException {
		Context.getLocationService().saveLocation(location);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateLocation(org.openmrs.Location)
	 * @deprecated
	 */
	public void updateLocation(Location location) throws APIException {
		Context.getLocationService().saveLocation(location);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteLocation(org.openmrs.Location)
	 * @deprecated
	 */
	public void deleteLocation(Location location) throws APIException {
		Context.getLocationService().purgeLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createRole(org.openmrs.Role)
	 * @deprecated
	 */
	public void createRole(Role role) throws APIException {
		Context.getUserService().saveRole(role);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateRole(org.openmrs.Role)
	 * @deprecated
	 */
	public void updateRole(Role role) throws APIException {
		Context.getUserService().saveRole(role);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteRole(org.openmrs.Role)
	 * @deprecated
	 */
	public void deleteRole(Role role) throws APIException {
		Context.getUserService().purgeRole(role);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createPrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	public void createPrivilege(Privilege privilege) throws APIException {
		Context.getUserService().savePrivilege(privilege);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updatePrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	public void updatePrivilege(Privilege privilege) throws APIException {
		Context.getUserService().savePrivilege(privilege);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deletePrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	public void deletePrivilege(Privilege privilege) throws APIException {
		Context.getUserService().purgePrivilege(privilege);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void createConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().saveConceptClass(cc);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().saveConceptClass(cc);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void deleteConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().purgeConceptClass(cc);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().saveConceptDatatype(cd);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().saveConceptDatatype(cd);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().purgeConceptDatatype(cd);
	}
	
	/**
	 * Create a new Report
	 * 
	 * @param Report to create
	 * @throws APIException
	 */
	public void createReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_ADD_REPORTS);

		dao.createReport(report);
	}

	/**
	 * Update Report
	 * 
	 * @param Report to update
	 * @throws APIException
	 */
	public void updateReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_EDIT_REPORTS);

		dao.updateReport(report);
	}

	/**
	 * Delete Report
	 * 
	 * @param Report to delete
	 * @throws APIException
	 */
	public void deleteReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_DELETE_REPORTS);

		dao.deleteReport(report);
	}
	
	/**
	 * Create a new Report Object
	 * 
	 * @param Report Object to create
	 * @throws APIException
	 */
	public void createReportObject(AbstractReportObject reportObject)
	        throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS);

		dao.createReportObject(reportObject);
	}

	/**
	 * Update Report Object
	 * 
	 * @param Report Object to update
	 * @throws APIException
	 */
	public void updateReportObject(AbstractReportObject reportObject)
	        throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS);

		dao.updateReportObject(reportObject);
	}

	/**
	 * Delete Report Object
	 * 
	 * @param Report Object to delete
	 * @throws APIException
	 */
	public void deleteReportObject(Integer reportObjectId) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS);

		dao.deleteReportObject(reportObjectId);
	}

	/**
	 * @deprecated moved to ConceptServiceImpl
	 */
	public void updateConceptWord(Concept concept) throws APIException {
		Context.getConceptService().updateConceptWord(concept);
	}
	
	/**
	 * @deprecated moved to ConceptServiceImpl
	 */
	public void updateConceptWords() throws APIException {
		Context.getConceptService().updateConceptWords();
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptWords(Integer conceptIdStart, Integer conceptIdEnd)
	        throws APIException {
		Context.getConceptService().updateConceptWords(conceptIdStart, conceptIdEnd);
		}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptSetDerived(Concept concept) throws APIException {
		Context.getConceptService().updateConceptSetDerived(concept);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptSetDerived() throws APIException {
		Context.getConceptService().updateConceptSetDerived();
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	public void createConceptProposal(ConceptProposal cp) throws APIException {
		Context.getConceptService().saveConceptProposal(cp);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	public void updateConceptProposal(ConceptProposal cp) throws APIException {
		Context.getConceptService().saveConceptProposal(cp);
	}

	/**
	 * @deprecated moved to ConceptService
	 */
	public void mapConceptProposalToConcept(ConceptProposal cp,
	        Concept mappedConcept) throws APIException {
		Context.getConceptService().mapConceptProposalToConcept(cp, mappedConcept);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 * @see org.openmrs.api.AdministrationService#rejectConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void rejectConceptProposal(ConceptProposal cp) {
		Context.getConceptService().rejectConceptProposal(cp);
	}
		
	/**
	 * @see org.openmrs.api.AdministrationService#mrnGeneratorLog(java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		dao.mrnGeneratorLog(site, start, count);
		}
		
	/**
	 * @see org.openmrs.api.AdministrationService#getMRNGeneratorLog()
	 */
	public Collection<?> getMRNGeneratorLog() throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: "
			        + OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		return dao.getMRNGeneratorLog();
		}
			
	/**
	 * Static-ish variable used to cache the system variables.  This is not
	 * static so that every time a module is loaded or removed the variable
	 * is destroyed (along with the administration service) and recreated the
	 * next time it is called 
	 */
	protected SortedMap<String, String> systemVariables = null;
			
	/**
	 * @see org.openmrs.api.AdministrationService#getSystemVariables()
	 */
	public SortedMap<String, String> getSystemVariables() throws APIException {
		if (systemVariables == null) {
			systemVariables = new TreeMap<String, String>();

			
			// Added the server's fully qualified domain name
			try {
				systemVariables.put("OPENMRS_HOSTNAME", 
				                    InetAddress.getLocalHost().getCanonicalHostName());
			} 
			catch (UnknownHostException e) { 
				systemVariables.put("OPENMRS_HOSTNAME", 
				                    "Unknown host: " + e.getMessage());				
			}
			
			systemVariables.put("OPENMRS_VERSION",
			                    String.valueOf(OpenmrsConstants.OPENMRS_VERSION));
			systemVariables.put("DATABASE_VERSION_EXPECTED",
			                    String.valueOf(OpenmrsConstants.DATABASE_VERSION_EXPECTED));
			systemVariables.put("DATABASE_VERSION",
			                    String.valueOf(OpenmrsConstants.DATABASE_VERSION));
			systemVariables.put("DATABASE_NAME", OpenmrsConstants.DATABASE_NAME);
			systemVariables.put("DATABASE_BUSINESS_NAME",
			                    OpenmrsConstants.DATABASE_BUSINESS_NAME);
			systemVariables.put("OBSCURE_PATIENTS",
			                    String.valueOf(OpenmrsConstants.OBSCURE_PATIENTS));
			systemVariables.put("OBSCURE_PATIENTS_FAMILY_NAME",
			                    OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME);
			systemVariables.put("OBSCURE_PATIENTS_GIVEN_NAME",
			                    OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME);
			systemVariables.put("OBSCURE_PATIENTS_MIDDLE_NAME",
			                    OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME);
			systemVariables.put("STOP_WORDS", OpenmrsConstants.STOP_WORDS()
			                                                  .toString());
			systemVariables.put("MODULE_REPOSITORY_PATH",
			                    ModuleUtil.getModuleRepository().getAbsolutePath());
			systemVariables.put("OPERATING_SYSTEM_KEY",
			                    String.valueOf(OpenmrsConstants.OPERATING_SYSTEM_KEY));
			systemVariables.put("OPERATING_SYSTEM",
			                    String.valueOf(OpenmrsConstants.OPERATING_SYSTEM));
		}
		
		return systemVariables;
	}
		
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperty(java.lang.String)
	 */
	public String getGlobalProperty(String propertyName) throws APIException {
		// This method should not have any authorization check
		return dao.getGlobalProperty(propertyName);
	}
		
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperty(java.lang.String, java.lang.String)
	 */
	public String getGlobalProperty(String propertyName, String defaultValue) throws APIException {
		String s = getGlobalProperty(propertyName);
		if (s == null)
			return defaultValue;
		return s;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperties()
	 * @deprecated
	 */
	public List<GlobalProperty> getGlobalProperties() throws APIException {
		return getAllGlobalProperties();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperties(java.util.List)
	 * @deprecated
	 */
	public void setGlobalProperties(List<GlobalProperty> props) throws APIException {
		saveGlobalProperties(props);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteGlobalProperty(java.lang.String)
	 * @deprecated
	 */
	public void deleteGlobalProperty(String propertyName) throws APIException {
		purgeGlobalProperty(new GlobalProperty(propertyName));
	}

	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperty(java.lang.String, java.lang.String)
	 * @deprecated
	 */
	public void setGlobalProperty(String propertyName, String propertyValue) throws APIException {
		saveGlobalProperty(new GlobalProperty(propertyName, propertyValue));
	}

	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperty(org.openmrs.GlobalProperty)
	 * @deprecated
	 */
	public void setGlobalProperty(GlobalProperty gp) throws APIException {
		saveGlobalProperty(gp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#addGlobalProperty(org.openmrs.GlobalProperty)
	 * @deprecated
	 */
	public void addGlobalProperty(GlobalProperty gp) {
		setGlobalProperty(gp);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#addGlobalProperty(java.lang.String, java.lang.String)
	 * @deprecated
	 */
	public void addGlobalProperty(String propertyName, String propertyValue) throws APIException {
		//dao.addGlobalProperty(propertyName, propertyValue);
		saveGlobalProperty(new GlobalProperty(propertyName, propertyValue));
	}
	
	/**
     * @see org.openmrs.api.AdministrationService#getAllGlobalProperties()
     */
    public List<GlobalProperty> getAllGlobalProperties() throws APIException {
	    return dao.getAllGlobalProperties();
    }

	/**
     * @see org.openmrs.api.AdministrationService#purgeGlobalProperty(org.openmrs.GlobalProperty)
     */
    public void purgeGlobalProperty(GlobalProperty globalProperty)
            throws APIException {
    	dao.deleteGlobalProperty(globalProperty);
    }

	/**
     * @see org.openmrs.api.AdministrationService#saveGlobalProperties(java.util.List)
     */
    public List<GlobalProperty> saveGlobalProperties(List<GlobalProperty> props)
            throws APIException {
	    log.debug("saving a list of global properties");
		
		// delete all properties not in this new list
		for (GlobalProperty gp : getGlobalProperties()) {
			if (!props.contains(gp))
				purgeGlobalProperty(gp);
		}
		
		// add all of the new properties
		for (GlobalProperty prop : props) {
			if (prop.getProperty() != null && prop.getProperty().length() > 0) {
				saveGlobalProperty(prop);
			}
		}
    	
    	return props;
    }

	/**
     * @see org.openmrs.api.AdministrationService#saveGlobalProperty(org.openmrs.GlobalProperty)
     */
    public GlobalProperty saveGlobalProperty(GlobalProperty gp)
            throws APIException {
    	
    	// only try to save it if the global property has a key
    	if (gp.getProperty() != null && gp.getProperty().length() > 0) {
    		dao.saveGlobalProperty(gp);
    		notifyGlobalPropertyChange(gp);
    		return gp;
    	}
    	
    	return gp;
    }

	/**
	 * @see org.openmrs.api.AdministrationService#getDataEntryStatistics(java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<DataEntryStatistic> getDataEntryStatistics(Date fromDate,
	        Date toDate, String encounterUserColumn, String orderUserColumn,
	        String groupBy) throws APIException {
		return dao.getDataEntryStatistics(fromDate,
                                         toDate,
                                         encounterUserColumn,
                                         orderUserColumn,
                                         groupBy);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#executeSQL(java.lang.String, boolean)
	 */
	public List<List<Object>> executeSQL(String sql, boolean selectOnly)
	        throws APIException {
		if (sql == null || sql.trim().equals(""))
			return null;

		return dao.executeSQL(sql, selectOnly);
	}

	/**
     * @see org.openmrs.api.AdministrationService#addGlobalPropertyListener(java.lang.String, org.openmrs.api.GlobalPropertyListener)
     */
    public void addGlobalPropertyListener(GlobalPropertyListener listener) {
    	eventListeners.getGlobalPropertyListeners().add(listener);
    }

	/**
     * @see org.openmrs.api.AdministrationService#removeGlobalPropertyListener(java.lang.String, org.openmrs.api.GlobalPropertyListener)
     */
    public void removeGlobalPropertyListener(GlobalPropertyListener listener) {
    	eventListeners.getGlobalPropertyListeners().remove(listener);
    }

	/**
     * Calls global property listeners registered for this create/change
     * 
     * @param gp
     */
    private void notifyGlobalPropertyChange(GlobalProperty gp) {
    	for (GlobalPropertyListener listener : eventListeners.getGlobalPropertyListeners())
    		if (listener.supportsPropertyName(gp.getProperty()))
    			listener.globalPropertyChanged(gp);
    }
    
	/**
     * Calls global property listeners registered for this delete
     * 
     * @param propertyName
     */
    private void notifyGlobalPropertyDelete(String propertyName) {
    	for (GlobalPropertyListener listener : eventListeners.getGlobalPropertyListeners())
    		if (listener.supportsPropertyName(propertyName))
    			listener.globalPropertyDeleted(propertyName);
    }
    
}
