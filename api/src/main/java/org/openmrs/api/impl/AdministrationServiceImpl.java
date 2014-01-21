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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EventListeners;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.util.HttpClient;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Default implementation of the administration services. This class should not be used on its own.
 * The current OpenMRS implementation should be fetched from the Context
 * 
 * @see org.openmrs.api.AdministrationService
 * @see org.openmrs.api.context.Context
 */
@Transactional
public class AdministrationServiceImpl extends BaseOpenmrsService implements AdministrationService, GlobalPropertyListener {
	
	protected Log log = LogFactory.getLog(getClass());
	
	protected AdministrationDAO dao;
	
	private EventListeners eventListeners;
	
	/**
	 * An always up-to-date collection of the allowed locales.
	 */
	private GlobalLocaleList globalLocaleList;
	
	private HttpClient implementationIdHttpClient;
	
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
	@Deprecated
	public void createEncounterType(EncounterType encounterType) throws APIException {
		Context.getEncounterService().saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateEncounterType(org.openmrs.EncounterType)
	 * @deprecated
	 */
	@Deprecated
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		Context.getEncounterService().saveEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteEncounterType(org.openmrs.EncounterType)
	 * @deprecated
	 */
	@Deprecated
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		Context.getEncounterService().purgeEncounterType(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Deprecated
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#savePatientIdentifierType(PatientIdentifierType)}
	 */
	@Deprecated
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().savePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.PatientService#purgePatientIdentifierType(PatientIdentifierType)}
	 */
	@Deprecated
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Context.getPatientService().purgePatientIdentifierType(patientIdentifierType);
	}
	
	/**
	 * Create a new Tribe
	 * 
	 * @param tribe Tribe to create
	 * @throws APIException
	 * @deprecated
	 */
	@Deprecated
	public void createTribe(Tribe tribe) throws APIException {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * Update Tribe
	 * 
	 * @param tribe Tribe to update
	 * @throws APIException
	 * @deprecated
	 */
	@Deprecated
	public void updateTribe(Tribe tribe) throws APIException {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * Delete Tribe
	 * 
	 * @param tribe Tribe to delete
	 * @throws APIException
	 * @deprecated
	 */
	@Deprecated
	public void deleteTribe(Tribe tribe) throws APIException {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * Retire Tribe
	 * 
	 * @param tribe Tribe to retire
	 * @throws APIException
	 * @deprecated
	 */
	@Deprecated
	public void retireTribe(Tribe tribe) throws APIException {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * Unretire Tribe
	 * 
	 * @param tribe Tribe to unretire
	 * @throws APIException
	 * @deprecated
	 */
	@Deprecated
	public void unretireTribe(Tribe tribe) throws APIException {
		throw new APIException("The Tribe object is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public void createFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().saveFieldType(fieldType);
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public void updateFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().saveFieldType(fieldType);
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public void deleteFieldType(FieldType fieldType) throws APIException {
		Context.getFormService().purgeFieldType(fieldType);
	}
	
	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 */
	@Deprecated
	public void createMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().saveMimeType(mimeType);
	}
	
	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#saveMimeType(MimeType)}
	 */
	@Deprecated
	public void updateMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().saveMimeType(mimeType);
	}
	
	/**
	 * @deprecated use {@link org.openmrs.api.ObsService#purgeMimeType(MimeType)}
	 */
	@Deprecated
	public void deleteMimeType(MimeType mimeType) throws APIException {
		Context.getObsService().purgeMimeType(mimeType);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createLocation(org.openmrs.Location)
	 * @deprecated
	 */
	@Deprecated
	public void createLocation(Location location) throws APIException {
		Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateLocation(org.openmrs.Location)
	 * @deprecated
	 */
	@Deprecated
	public void updateLocation(Location location) throws APIException {
		Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteLocation(org.openmrs.Location)
	 * @deprecated
	 */
	@Deprecated
	public void deleteLocation(Location location) throws APIException {
		Context.getLocationService().purgeLocation(location);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createRole(org.openmrs.Role)
	 * @deprecated
	 */
	@Deprecated
	public void createRole(Role role) throws APIException {
		Context.getUserService().saveRole(role);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateRole(org.openmrs.Role)
	 * @deprecated
	 */
	@Deprecated
	public void updateRole(Role role) throws APIException {
		Context.getUserService().saveRole(role);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteRole(org.openmrs.Role)
	 * @deprecated
	 */
	@Deprecated
	public void deleteRole(Role role) throws APIException {
		Context.getUserService().purgeRole(role);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createPrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	@Deprecated
	public void createPrivilege(Privilege privilege) throws APIException {
		Context.getUserService().savePrivilege(privilege);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updatePrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	@Deprecated
	public void updatePrivilege(Privilege privilege) throws APIException {
		Context.getUserService().savePrivilege(privilege);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deletePrivilege(org.openmrs.Privilege)
	 * @deprecated
	 */
	@Deprecated
	public void deletePrivilege(Privilege privilege) throws APIException {
		Context.getUserService().purgePrivilege(privilege);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void createConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().saveConceptClass(cc);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void updateConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().saveConceptClass(cc);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void deleteConceptClass(ConceptClass cc) throws APIException {
		Context.getConceptService().purgeConceptClass(cc);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void createConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().saveConceptDatatype(cd);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void updateConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().saveConceptDatatype(cd);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void deleteConceptDatatype(ConceptDatatype cd) throws APIException {
		Context.getConceptService().purgeConceptDatatype(cd);
	}
	
	/**
	 * Create a new Report
	 * 
	 * @param report Report to create
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void createReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_REPORTS);
		
		dao.createReport(report);
	}
	
	/**
	 * Update Report
	 * 
	 * @param report Report to update
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void updateReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_REPORTS);
		
		dao.updateReport(report);
	}
	
	/**
	 * Delete Report
	 * 
	 * @param report Report to delete
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void deleteReport(Report report) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_REPORTS);
		
		dao.deleteReport(report);
	}
	
	/**
	 * Create a new Report Object
	 * 
	 * @param reportObject Report Object to create
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void createReportObject(AbstractReportObject reportObject) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_REPORT_OBJECTS);
		
		dao.createReportObject(reportObject);
	}
	
	/**
	 * Update Report Object
	 * 
	 * @param reportObject Report Object to update
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void updateReportObject(AbstractReportObject reportObject) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_REPORT_OBJECTS);
		
		dao.updateReportObject(reportObject);
	}
	
	/**
	 * Delete Report Object
	 * 
	 * @param reportObjectId Internal Integer identifier of Report Object to delete
	 * @deprecated see reportingcompatibility module
	 * @throws APIException
	 */
	@Deprecated
	public void deleteReportObject(Integer reportObjectId) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_REPORT_OBJECTS);
		
		dao.deleteReportObject(reportObjectId);
	}
	
	/**
	 * @deprecated moved to ConceptServiceImpl
	 */
	@Deprecated
	public void updateConceptWord(Concept concept) throws APIException {
		Context.getConceptService().updateConceptWord(concept);
	}
	
	/**
	 * @deprecated moved to ConceptServiceImpl
	 */
	@Deprecated
	public void updateConceptWords() throws APIException {
		Context.getConceptService().updateConceptWords();
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void updateConceptWords(Integer conceptIdStart, Integer conceptIdEnd) throws APIException {
		Context.getConceptService().updateConceptWords(conceptIdStart, conceptIdEnd);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void createConceptProposal(ConceptProposal cp) throws APIException {
		Context.getConceptService().saveConceptProposal(cp);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void updateConceptProposal(ConceptProposal cp) throws APIException {
		Context.getConceptService().saveConceptProposal(cp);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 */
	@Deprecated
	public void mapConceptProposalToConcept(ConceptProposal cp, Concept mappedConcept) throws APIException {
		Context.getConceptService().mapConceptProposalToConcept(cp, mappedConcept);
	}
	
	/**
	 * @deprecated moved to ConceptService
	 * @see org.openmrs.api.AdministrationService#rejectConceptProposal(org.openmrs.ConceptProposal)
	 */
	@Deprecated
	public void rejectConceptProposal(ConceptProposal cp) {
		Context.getConceptService().rejectConceptProposal(cp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#mrnGeneratorLog(java.lang.String,
	 *      java.lang.Integer, java.lang.Integer)
	 * @deprecated
	 */
	@Deprecated
	public void mrnGeneratorLog(String site, Integer start, Integer count) throws APIException {
		if (!Context.hasPrivilege(PrivilegeConstants.EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + PrivilegeConstants.EDIT_PATIENTS);
		
		dao.mrnGeneratorLog(site, start, count);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getMRNGeneratorLog()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public Collection<?> getMRNGeneratorLog() throws APIException {
		if (!Context.hasPrivilege(PrivilegeConstants.EDIT_PATIENTS))
			throw new APIAuthenticationException("Privilege required: " + PrivilegeConstants.EDIT_PATIENTS);
		
		return dao.getMRNGeneratorLog();
	}
	
	/**
	 * Static-ish variable used to cache the system variables. This is not static so that every time
	 * a module is loaded or removed the variable is destroyed (along with the administration
	 * service) and recreated the next time it is called
	 */
	protected SortedMap<String, String> systemVariables = null;
	
	/**
	 * Set of locales which can be used to present messages in the user interface. Created lazily as
	 * needed by {@link #getAllowedLocales()}.
	 */
	private HashSet<Locale> presentationLocales;
	
	/**
	 * @see org.openmrs.api.AdministrationService#getSystemVariables()
	 */
	@Transactional(readOnly = true)
	public SortedMap<String, String> getSystemVariables() throws APIException {
		if (systemVariables == null) {
			systemVariables = new TreeMap<String, String>();
			
			// Added the server's fully qualified domain name
			try {
				systemVariables.put("OPENMRS_HOSTNAME", InetAddress.getLocalHost().getCanonicalHostName());
			}
			catch (UnknownHostException e) {
				systemVariables.put("OPENMRS_HOSTNAME", "Unknown host: " + e.getMessage());
			}
			
			systemVariables.put("OPENMRS_VERSION", String.valueOf(OpenmrsConstants.OPENMRS_VERSION));
			systemVariables.put("DATABASE_NAME", OpenmrsConstants.DATABASE_NAME);
			systemVariables.put("DATABASE_BUSINESS_NAME", OpenmrsConstants.DATABASE_BUSINESS_NAME);
			systemVariables.put("OBSCURE_PATIENTS", String.valueOf(OpenmrsConstants.OBSCURE_PATIENTS));
			systemVariables.put("OBSCURE_PATIENTS_FAMILY_NAME", OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME);
			systemVariables.put("OBSCURE_PATIENTS_GIVEN_NAME", OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME);
			systemVariables.put("OBSCURE_PATIENTS_MIDDLE_NAME", OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME);
			systemVariables.put("MODULE_REPOSITORY_PATH", ModuleUtil.getModuleRepository().getAbsolutePath());
			systemVariables.put("OPERATING_SYSTEM_KEY", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM_KEY));
			systemVariables.put("OPERATING_SYSTEM", String.valueOf(OpenmrsConstants.OPERATING_SYSTEM));
		}
		
		return systemVariables;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperty(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public String getGlobalProperty(String propertyName) throws APIException {
		// This method should not have any authorization check
		if (propertyName == null)
			return null;
		
		return dao.getGlobalProperty(propertyName);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperty(java.lang.String,
	 *      java.lang.String)
	 */
	@Transactional(readOnly = true)
	public String getGlobalProperty(String propertyName, String defaultValue) throws APIException {
		String s = Context.getAdministrationService().getGlobalProperty(propertyName);
		if (s == null)
			return defaultValue;
		return s;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertyObject(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public GlobalProperty getGlobalPropertyObject(String propertyName) {
		return dao.getGlobalPropertyObject(propertyName);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperties()
	 * @deprecated
	 */
	@Deprecated
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalProperties() throws APIException {
		return Context.getAdministrationService().getAllGlobalProperties();
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperties(java.util.List)
	 * @deprecated
	 */
	@Deprecated
	public void setGlobalProperties(List<GlobalProperty> props) throws APIException {
		Context.getAdministrationService().saveGlobalProperties(props);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteGlobalProperty(java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	public void deleteGlobalProperty(String propertyName) throws APIException {
		Context.getAdministrationService().purgeGlobalProperty(new GlobalProperty(propertyName));
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public void setGlobalProperty(String propertyName, String propertyValue) throws APIException {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(propertyName);
		if (gp == null) {
			gp = new GlobalProperty();
			gp.setProperty(propertyName);
		}
		gp.setPropertyValue(propertyValue);
		dao.saveGlobalProperty(gp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateGlobalProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public void updateGlobalProperty(String propertyName, String propertyValue) throws IllegalStateException {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(propertyName);
		if (gp == null) {
			throw new IllegalStateException("Global property with the given propertyName does not exist" + propertyName);
		}
		gp.setPropertyValue(propertyValue);
		dao.saveGlobalProperty(gp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#setGlobalProperty(org.openmrs.GlobalProperty)
	 * @deprecated
	 */
	@Deprecated
	public void setGlobalProperty(GlobalProperty gp) throws APIException {
		Context.getAdministrationService().saveGlobalProperty(gp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#addGlobalProperty(org.openmrs.GlobalProperty)
	 * @deprecated
	 */
	@Deprecated
	public void addGlobalProperty(GlobalProperty gp) {
		Context.getAdministrationService().setGlobalProperty(gp);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#addGlobalProperty(java.lang.String,
	 *      java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	public void addGlobalProperty(String propertyName, String propertyValue) throws APIException {
		//dao.addGlobalProperty(propertyName, propertyValue);
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(propertyName, propertyValue));
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getAllGlobalProperties()
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getAllGlobalProperties() throws APIException {
		return dao.getAllGlobalProperties();
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertiesByPrefix(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix) {
		return dao.getGlobalPropertiesByPrefix(prefix);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertiesBySuffix(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix) {
		return dao.getGlobalPropertiesBySuffix(suffix);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#purgeGlobalProperty(org.openmrs.GlobalProperty)
	 */
	public void purgeGlobalProperty(GlobalProperty globalProperty) throws APIException {
		notifyGlobalPropertyDelete(globalProperty.getProperty());
		dao.deleteGlobalProperty(globalProperty);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#saveGlobalProperties(java.util.List)
	 */
	public List<GlobalProperty> saveGlobalProperties(List<GlobalProperty> props) throws APIException {
		log.debug("saving a list of global properties");
		
		// add all of the new properties
		for (GlobalProperty prop : props) {
			if (prop.getProperty() != null && prop.getProperty().length() > 0) {
				Context.getAdministrationService().saveGlobalProperty(prop);
			}
		}
		
		return props;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#saveGlobalProperty(org.openmrs.GlobalProperty)
	 */
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws APIException {
		// only try to save it if the global property has a key
		if (gp.getProperty() != null && gp.getProperty().length() > 0) {
			CustomDatatypeUtil.saveIfDirty(gp);
			dao.saveGlobalProperty(gp);
			notifyGlobalPropertyChange(gp);
			return gp;
		}
		
		return gp;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#executeSQL(java.lang.String, boolean)
	 */
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws APIException {
		if (sql == null || sql.trim().equals(""))
			return null;
		
		return dao.executeSQL(sql, selectOnly);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#addGlobalPropertyListener(GlobalPropertyListener)
	 */
	public void addGlobalPropertyListener(GlobalPropertyListener listener) {
		eventListeners.getGlobalPropertyListeners().add(listener);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#removeGlobalPropertyListener(GlobalPropertyListener)
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
	
	/**
	 * @see org.openmrs.api.AdministrationService#getImplementationId()
	 */
	@Transactional(readOnly = true)
	public ImplementationId getImplementationId() throws APIException {
		String property = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
		
		// fail early if no gp has been defined yet
		if (property == null)
			return null;
		
		try {
			ImplementationId implId = OpenmrsUtil.getSerializer().read(ImplementationId.class, property);
			
			return implId;
		}
		catch (Exception e) {
			log.debug("Error while getting implementation id", e);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#setImplementationId(org.openmrs.ImplementationId)
	 */
	public void setImplementationId(ImplementationId implementationId) throws APIException {
		
		if (implementationId == null)
			return;
		
		// check the validity of this implementation id with the server
		String description = implementationId.getDescription();
		try {
			// check that source id is valid
			description = checkImplementationIdValidity(implementationId.getImplementationId(), description,
			    implementationId.getPassphrase());
			
			// save the server's description back to this concept source object
			implementationId.setDescription(description);
			
			boolean foundMatchingSource = false;
			// loop over the concept sources to make sure one exists for this hl7Code/implementationId
			List<ConceptSource> sources = Context.getConceptService().getAllConceptSources();
			if (sources != null) {
				for (ConceptSource source : sources) {
					if (implementationId.getImplementationId().equals(source.getHl7Code())) {
						foundMatchingSource = true;
					}
				}
			}
			
			// if no ConceptSource currently exists with this implementationId, save this implId
			// as a new ConceptSource
			if (!foundMatchingSource) {
				ConceptSource newConceptSource = new ConceptSource();
				newConceptSource.setName(implementationId.getName());
				newConceptSource.setDescription(implementationId.getDescription());
				newConceptSource.setHl7Code(implementationId.getImplementationId());
				if (Context.getAuthenticatedUser() == null) // (hackish)
					newConceptSource.setCreator(new User(1)); // fake the user because no one is logged in
				Context.getConceptService().saveConceptSource(newConceptSource);
			}
			
			// serialize and save the ImplementationId to the global properties table
			StringWriter stringWriter = new StringWriter();
			OpenmrsUtil.getSerializer().write(implementationId, stringWriter);
			Context.getAdministrationService().saveGlobalProperty(
			    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, stringWriter.toString()));
		}
		catch (APIException e) {
			throw e;
		}
		catch (Exception e) {
			// pass any other exceptions on up the train
			throw new APIException(e);
		}
		finally {
			// save an empty concept source to the database when something fails?
		}
	}
	
	/**
	 * Checks the remote server for this exact implementation id. Returns the description if 1)
	 * there is no implementation id or 2) there is a implementation id and this passphrase matches
	 * it. In the case of 1), this implementation id and passphrase are saved to the remote server's
	 * database
	 * 
	 * @param implementationId
	 * @param description
	 * @param passphrase
	 * @return the stored description on the remote server
	 * @throws APIException
	 * @throws UnsupportedEncodingException
	 */
	private String checkImplementationIdValidity(String implementationId, String description, String passphrase)
	        throws APIException {
		
		if (!StringUtils.hasLength(implementationId))
			throw new APIException("The implementationid cannot be empty");
		if (!StringUtils.hasLength(description))
			throw new APIException("The description cannot be empty");
		if (!StringUtils.hasLength(passphrase))
			throw new APIException("The passphrase cannot be empty");
		
		// set up the data map to post to the openmrs server
		Map<String, String> data = new HashMap<String, String>();
		data.put("implementationId", implementationId);
		data.put("description", description);
		data.put("passphrase", passphrase);
		
		String response = implementationIdHttpClient.post(data);
		response = response.trim();
		
		if ("".equals(response)) {
			String ms = Context.getMessageSourceService().getMessage("ImplementationId.connectionError",
			    new String[] { implementationId }, Context.getLocale());
			throw new APIException(ms);
		}
		
		if (log.isDebugEnabled())
			log.debug("Response: " + response);
		
		if (response.startsWith("Success")) {
			response = response.replace("Success", "");
			return response.trim();
		}
		
		String ms = Context.getMessageSourceService().getMessage("ImplementationId.invalidIdorPassphrase",
		    new String[] { description }, Context.getLocale());
		throw new APIException(ms);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getAllowedLocales()
	 */
	@Transactional(readOnly = true)
	public List<Locale> getAllowedLocales() {
		// lazy-load the global locale list and initialize with current global property value
		if (globalLocaleList == null) {
			globalLocaleList = new GlobalLocaleList();
			Context.getAdministrationService().addGlobalPropertyListener(globalLocaleList);
		}
		
		Set<Locale> allowedLocales = globalLocaleList.getAllowedLocales();
		
		// update the GlobalLocaleList.allowedLocales by faking a global property change
		if (allowedLocales == null) {
			// use a default language of "english" if they have cleared this GP for some reason
			String currentPropertyValue = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, LocaleUtility.getDefaultLocale().toString());
			GlobalProperty allowedLocalesProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST,
			        currentPropertyValue);
			globalLocaleList.globalPropertyChanged(allowedLocalesProperty);
			allowedLocales = globalLocaleList.getAllowedLocales();
		}
		
		// allowedLocales is guaranteed to not be null at this point
		return new ArrayList<Locale>(allowedLocales);
	}
	
	/**
	 * Used by spring to set the GlobalLocaleList on this implementation
	 * 
	 * @param gll the GlobalLocaleList object that is registered to the GlobalPropertyListeners as
	 *            well
	 */
	public void setGlobalLocaleList(GlobalLocaleList gll) {
		globalLocaleList = gll;
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getPresentationLocales()
	 */
	@Transactional(readOnly = true)
	public Set<Locale> getPresentationLocales() {
		if (presentationLocales == null) {
			presentationLocales = new HashSet<Locale>();
			Collection<Locale> messageLocales = Context.getMessageSourceService().getLocales();
			List<Locale> allowedLocales = getAllowedLocales();
			for (Locale possibleLocale : messageLocales) {
				if (allowedLocales.contains(possibleLocale)) {
					presentationLocales.add(possibleLocale);
				} else {
					// to be sure, check for language-only matches
					for (Locale allowedLocale : allowedLocales) {
						if (("".equals(allowedLocale.getCountry()) || "".equals(possibleLocale.getCountry()))
						        && (allowedLocale.getLanguage().equals(possibleLocale.getLanguage()))) {
							presentationLocales.add(possibleLocale);
							break;
						}
					}
				}
			}
		}
		return presentationLocales;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (newValue.getProperty().equals(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST)) {
			// reset the calculated locale values
			presentationLocales = null;
		}
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	public void globalPropertyDeleted(String propertyName) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertyByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public GlobalProperty getGlobalPropertyByUuid(String uuid) {
		return dao.getGlobalPropertyByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertyValue(java.lang.String,
	 *      java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(String propertyName, T defaultValue) throws APIException {
		if (defaultValue == null)
			throw new IllegalArgumentException("The defaultValue argument cannot be null");
		
		String propVal = Context.getAdministrationService().getGlobalProperty(propertyName);
		if (!StringUtils.hasLength(propVal))
			return defaultValue;
		
		try {
			return (T) defaultValue.getClass().getDeclaredConstructor(String.class).newInstance(propVal);
		}
		catch (InstantiationException e) {
			throw new APIException(defaultValue.getClass().getName() + " is not able to be instantiated with value: "
			        + propVal, e);
		}
		catch (NoSuchMethodException e) {
			throw new APIException(defaultValue.getClass().getName() + " does not have a string constructor", e);
		}
		catch (Exception e) {
			log.error("Unable to turn value '" + propVal + "' into type " + defaultValue.getClass().getName(), e);
			return defaultValue;
		}
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getSystemInformation()
	 */
	@Transactional(readOnly = true)
	public Map<String, Map<String, String>> getSystemInformation() throws APIException {
		Map<String, Map<String, String>> systemInfoMap = new LinkedHashMap<String, Map<String, String>>();
		
		systemInfoMap.put("SystemInfo.title.openmrsInformation", new LinkedHashMap<String, String>() {
			
			private static final long serialVersionUID = 1L;
			
			{
				put("SystemInfo.OpenMRSInstallation.systemDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar
				        .getInstance().getTime()));
				put("SystemInfo.OpenMRSInstallation.systemTime", new SimpleDateFormat("HH:mm:ss").format(Calendar
				        .getInstance().getTime()));
				put("SystemInfo.OpenMRSInstallation.openmrsVersion", OpenmrsConstants.OPENMRS_VERSION);
				try {
					put("SystemInfo.hostname", InetAddress.getLocalHost().getCanonicalHostName());
				}
				catch (UnknownHostException e) {
					put("SystemInfo.hostname", "Unknown host: " + e.getMessage());
				}
			}
		});
		
		systemInfoMap.put("SystemInfo.title.javaRuntimeEnvironmentInformation", new LinkedHashMap<String, String>() {
			
			Properties properties = System.getProperties();
			
			private static final long serialVersionUID = 1L;
			
			{
				put("SystemInfo.JavaRuntimeEnv.operatingSystem", properties.getProperty("os.name"));
				put("SystemInfo.JavaRuntimeEnv.operatingSystemArch", properties.getProperty("os.arch"));
				put("SystemInfo.JavaRuntimeEnv.operatingSystemVersion", properties.getProperty("os.version"));
				put("SystemInfo.JavaRuntimeEnv.javaVersion", properties.getProperty("java.version"));
				put("SystemInfo.JavaRuntimeEnv.javaVendor", properties.getProperty("java.vendor"));
				put("SystemInfo.JavaRuntimeEnv.jvmVersion", properties.getProperty("java.vm.version"));
				put("SystemInfo.JavaRuntimeEnv.jvmVendor", properties.getProperty("java.vm.vendor"));
				put("SystemInfo.JavaRuntimeEnv.javaRuntimeName", properties.getProperty("java.runtime.name"));
				put("SystemInfo.JavaRuntimeEnv.javaRuntimeVersion", properties.getProperty("java.runtime.version"));
				put("SystemInfo.JavaRuntimeEnv.userName", properties.getProperty("user.name"));
				put("SystemInfo.JavaRuntimeEnv.systemLanguage", properties.getProperty("user.language"));
				put("SystemInfo.JavaRuntimeEnv.systemTimezone", properties.getProperty("user.timezone"));
				put("SystemInfo.JavaRuntimeEnv.fileSystemEncoding", properties.getProperty("sun.jnu.encoding"));
				put("SystemInfo.JavaRuntimeEnv.userDirectory", properties.getProperty("user.dir"));
				put("SystemInfo.JavaRuntimeEnv.tempDirectory", properties.getProperty("java.io.tmpdir"));
			}
		});
		
		systemInfoMap.put("SystemInfo.title.memoryInformation", new LinkedHashMap<String, String>() {
			
			private static final long serialVersionUID = 1L;
			
			Runtime runtime = Runtime.getRuntime();
			
			{
				put("SystemInfo.Memory.totalMemory", convertToMegaBytes(runtime.totalMemory()));
				put("SystemInfo.Memory.freeMemory", convertToMegaBytes(runtime.freeMemory()));
				put("SystemInfo.Memory.maximumHeapSize", convertToMegaBytes(runtime.maxMemory()));
				
			}
		});
		
		systemInfoMap.put("SystemInfo.title.dataBaseInformation", new LinkedHashMap<String, String>() {
			
			Properties properties = Context.getRuntimeProperties();
			
			private static final long serialVersionUID = 1L;
			
			{
				put("SystemInfo.Database.name", OpenmrsConstants.DATABASE_NAME);
				put("SystemInfo.Database.connectionURL", properties.getProperty("connection.url"));
				put("SystemInfo.Database.userName", properties.getProperty("connection.username"));
				put("SystemInfo.Database.driver", properties.getProperty("hibernate.connection.driver_class"));
				put("SystemInfo.Database.dialect", properties.getProperty("hibernate.dialect"));
				
			}
		});
		
		systemInfoMap.put("SystemInfo.title.moduleInformation", new LinkedHashMap<String, String>() {
			
			private static final long serialVersionUID = 1L;
			
			{
				put("SystemInfo.Module.repositoryPath", ModuleUtil.getModuleRepository().getAbsolutePath());
				Collection<Module> loadedModules = ModuleFactory.getLoadedModules();
				for (Module module : loadedModules) {
					String moduleInfo = module.getVersion() + " "
					        + (module.isStarted() ? "" : Context.getMessageSourceService().getMessage("Module.notStarted"));
					put(module.getName(), moduleInfo);
				}
			}
		});
		
		return systemInfoMap;
	}
	
	/**
	 * @param bytes to be converted into mega bytes
	 * @return memory in mega bytes
	 */
	private String convertToMegaBytes(long bytes) {
		int ONE_KILO_BYTE = 1024;
		return String.valueOf(bytes / ONE_KILO_BYTE / ONE_KILO_BYTE) + " MB";
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#purgeGlobalProperties(java.util.List)
	 */
	@Override
	public void purgeGlobalProperties(List<GlobalProperty> globalProperties) throws APIException {
		for (GlobalProperty globalProperty : globalProperties) {
			Context.getAdministrationService().purgeGlobalProperty(globalProperty);
		}
	}
	
	/**
	 * @see AdministrationService#getMaximumPropertyLength(Class, String)
	 */
	@Override
	@Transactional(readOnly = true)
	public int getMaximumPropertyLength(Class<? extends OpenmrsObject> aClass, String fieldName) {
		return dao.getMaximumPropertyLength(aClass, fieldName);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#validate(java.lang.Object, Errors)
	 */
	@Override
	@Transactional(readOnly = true)
	public void validate(Object object, Errors errors) throws APIException {
		if (object == null)
			throw new APIException(Context.getMessageSourceService().getMessage("error.null"));
		
		dao.validate(object, errors);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#getSearchLocales(org.openmrs.User)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Locale> getSearchLocales() throws APIException {
		Set<Locale> locales = new LinkedHashSet<Locale>();
		
		Locale currentLocale = Context.getLocale();
		
		locales.add(currentLocale); //the currently used full locale
		
		//the currently used language
		locales.add(new Locale(currentLocale.getLanguage()));
		
		//add user's proficient locales
		User user = Context.getAuthenticatedUser();
		if (user != null) {
			List<Locale> proficientLocales = user.getProficientLocales();
			if (proficientLocales != null) {
				locales.addAll(proficientLocales);
			}
		}
		
		//limit locales to only allowed locales
		List<Locale> allowedLocales = Context.getAdministrationService().getAllowedLocales();
		if (allowedLocales != null) {
			Set<Locale> retainLocales = new HashSet<Locale>();
			
			for (Locale allowedLocale : allowedLocales) {
				retainLocales.add(allowedLocale);
				retainLocales.add(new Locale(allowedLocale.getLanguage()));
			}
			
			locales.retainAll(retainLocales);
		}
		
		return new ArrayList<Locale>(locales);
	}
	
	@Override
	public void setImplementationIdHttpClient(HttpClient implementationIdHttpClient) {
		this.implementationIdHttpClient = implementationIdHttpClient;
	}
	
}
