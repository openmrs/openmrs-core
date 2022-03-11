/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.VisitDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.ValidateUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the {@link VisitService}. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context.
 *
 * @since 1.9
 */
@Transactional
public class VisitServiceImpl extends BaseOpenmrsService implements VisitService {
	
	private VisitDAO dao;
	
	/**
	 * Method used to inject the visit data access object.
	 *
	 * @param dao the visit data access object.
	 */
	public void setVisitDAO(VisitDAO dao) {
		this.dao = dao;
	}
	
	public VisitDAO getVisitDAO() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitType> getAllVisitTypes() {
		return getVisitDAO().getAllVisitTypes();
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitTypes(boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitType> getAllVisitTypes(boolean includeRetired) {
		return dao.getAllVisitTypes(includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitType getVisitType(Integer visitTypeId) {
		return getVisitDAO().getVisitType(visitTypeId);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitType getVisitTypeByUuid(String uuid) {
		return getVisitDAO().getVisitTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypes(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitType> getVisitTypes(String fuzzySearchPhrase) {
		return getVisitDAO().getVisitTypes(fuzzySearchPhrase);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisitType(org.openmrs.VisitType)
	 */
	@Override
	public VisitType saveVisitType(VisitType visitType) throws APIException {
		ValidateUtil.validate(visitType);
		return getVisitDAO().saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#retireVisitType(org.openmrs.VisitType, java.lang.String)
	 */
	@Override
	public VisitType retireVisitType(VisitType visitType, String reason) {
		return Context.getVisitService().saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#unretireVisitType(org.openmrs.VisitType)
	 */
	@Override
	public VisitType unretireVisitType(VisitType visitType) {
		return Context.getVisitService().saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisitType(org.openmrs.VisitType)
	 */
	@Override
	public void purgeVisitType(VisitType visitType) {
		getVisitDAO().purgeVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisits()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getAllVisits() throws APIException {
		return dao.getVisits(null, null, null, null, null, null, null, null, null, true, false);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisit(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisit(Integer visitId) throws APIException {
		return dao.getVisit(visitId);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisitByUuid(String uuid) throws APIException {
		return dao.getVisitByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisit(org.openmrs.Visit)
	 */
	@Override
	public Visit saveVisit(Visit visit) throws APIException {
		if (visit.getVisitId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_VISITS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_VISITS);
		}
		
		CustomDatatypeUtil.saveAttributesIfNecessary(visit);
		return dao.saveVisit(visit);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#endVisit(org.openmrs.Visit, java.util.Date)
	 */
	@Override
	public Visit endVisit(Visit visit, Date stopDate) {
		if (stopDate == null) {
			stopDate = new Date();
		}
		
		visit.setStopDatetime(stopDate);
		
		return Context.getVisitService().saveVisit(visit);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#voidVisit(org.openmrs.Visit, java.lang.String)
	 */
	@Override
	public Visit voidVisit(Visit visit, String reason) throws APIException {
		return dao.saveVisit(visit);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#unvoidVisit(org.openmrs.Visit)
	 */
	@Override
	public Visit unvoidVisit(Visit visit) throws APIException {
		return Context.getVisitService().saveVisit(visit);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisit(org.openmrs.Visit)
	 */
	@Override
	public void purgeVisit(Visit visit) throws APIException {
		if (visit.getVisitId() == null) {
			return;
		}
		if (!Context.getEncounterService().getEncountersByVisit(visit, true).isEmpty()) {
			throw new APIException("Visit.purge.inUse", (Object[]) null);
		}
		dao.deleteVisit(visit);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisits(Collection, Collection, Collection, Collection, Date, Date, Date, Date, Map, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, Map<VisitAttributeType, Object> attributeValues,
	        boolean includeInactive, boolean includeVoided) throws APIException {
		
		Map<VisitAttributeType, String> serializedAttributeValues = CustomDatatypeUtil.getValueReferences(attributeValues);
		return dao.getVisits(visitTypes, patients, locations, indications, minStartDatetime, maxStartDatetime,
		    minEndDatetime, maxEndDatetime, serializedAttributeValues, includeInactive, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitsByPatient(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getVisitsByPatient(Patient patient) throws APIException {
		//Don't bother to hit the database
		if (patient == null || patient.getId() == null) {
			return Collections.emptyList();
		}
		
		return Context.getVisitService().getVisits(null, Collections.singletonList(patient), null, null, null, null, null,
		    null, null, true, false);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getActiveVisitsByPatient(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getActiveVisitsByPatient(Patient patient) throws APIException {
		return Context.getVisitService().getVisitsByPatient(patient, false, false);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitsByPatient(org.openmrs.Patient, boolean, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getVisitsByPatient(Patient patient, boolean includeInactive, boolean includeVoided)
	        throws APIException {
		if (patient == null || patient.getId() == null) {
			return Collections.emptyList();
		}
		
		return dao.getVisits(null, Collections.singletonList(patient), null, null, null, null, null, null, null,
		    includeInactive, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitAttributeTypes()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VisitAttributeType> getAllVisitAttributeTypes() {
		return dao.getAllVisitAttributeTypes();
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitAttributeType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeType(Integer id) {
		return dao.getVisitAttributeType(id);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeTypeByUuid(String uuid) {
		return dao.getVisitAttributeTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	public VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType) {
		return dao.saveVisitAttributeType(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#retireVisitAttributeType(org.openmrs.VisitAttributeType,
	 *      java.lang.String)
	 */
	@Override
	public VisitAttributeType retireVisitAttributeType(VisitAttributeType visitAttributeType, String reason) {
		return dao.saveVisitAttributeType(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#unretireVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	public VisitAttributeType unretireVisitAttributeType(VisitAttributeType visitAttributeType) {
		return Context.getVisitService().saveVisitAttributeType(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	public void purgeVisitAttributeType(VisitAttributeType visitAttributeType) {
		dao.deleteVisitAttributeType(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitAttributeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttribute getVisitAttributeByUuid(String uuid) {
		return dao.getVisitAttributeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#stopVisits(Date)
	 */
	@Override
	public void stopVisits(Date maximumStartDate) {
		
		final List<VisitType> visitTypesToStop = getVisitTypesToStop();
		
		if (maximumStartDate == null) {
			maximumStartDate = new Date();
		}
			
		if (visitTypesToStop.isEmpty()) {
			return;
		}
		
		int counter = 0;
		Date stopDate = new Date();
		Visit nextVisit = dao.getNextVisit(null, visitTypesToStop, maximumStartDate);
		while (nextVisit != null) {
			nextVisit.setStopDatetime(stopDate);
			dao.saveVisit(nextVisit);
			if (counter++ > 50) {
				//ensure changes are persisted to DB before reclaiming memory
				Context.flushSession();
				Context.clearSession();
				counter = 0;
			}
					
		nextVisit = dao.getNextVisit(nextVisit, visitTypesToStop, maximumStartDate);
		}
	}
	
	/**
	 * @see org.openmrs.api.VisitService#isSuitableVisit(Visit, Location, Date)
	 */
	@Override
	public boolean isSuitableVisit(Visit visit, Location location, Date when) {
		if (OpenmrsUtil.compare(when, visit.getStartDatetime()) < 0) {
			return false;
		}
		if (OpenmrsUtil.compareWithNullAsLatest(when, visit.getStopDatetime()) > 0) {
			return false;
		}
		return isSameOrAncestor(visit.getLocation(), location);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#ensureVisit(Patient, Date, Location)
	 */
    @Override
	@Transactional
	public Visit ensureVisit(Patient patient, Date visitTime, Location department) {
		if (visitTime == null) {
			visitTime = new Date();
		}
		Visit visit = null;
		List<Patient> patientList = Collections.singletonList(patient);
		
		// visits that have not ended by the encounter date.
		List<Visit> candidates = dao.getVisits(null, patientList, null, null, null, visitTime, null, null, null, true,
		    false);
		
		if (candidates != null) {
			for (Visit candidate : candidates) {
				if (isSuitableVisit(candidate, department, visitTime)) {
					return candidate;
				}
			}
		}
		if (visit == null) {
			visit = buildVisit(patient, department, visitTime);
			dao.saveVisit(visit);
		}
		return visit;
	}
	
	/**
	 * @see org.openmrs.api.VisitService#ensureActiveVisit(Patient, Date, Location)
	 */
	@Override
	@Transactional
	public Visit ensureActiveVisit(Patient patient, Location department) {
		Visit activeVisit = getActiveVisitHelper(patient, department);
		if (activeVisit == null) {
			activeVisit = buildVisit(patient, department, new Date());
			dao.saveVisit(activeVisit);
		}
		return activeVisit;
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getLocationThatSupportsVisits(Location)
	 */
	@Override
	public Location getLocationThatSupportsVisits(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location does not support visits");
		} else if (location.hasTag(OpenmrsConstants.LOCATION_TAG_SUPPORTS_VISITS)) {
			return location;
		} else {
			return getLocationThatSupportsVisits(location.getParentLocation());
		}
	}
	
	/**
	 * Anything that calls this needs to be @Transactional
	 * @param patient is the patient whose visit is tp get 
	 * @param department is the location of the visit
	 * @return visit
	 */
	@Transactional(readOnly = true)
	private Visit getActiveVisitHelper(Patient patient, Location department) {
		List<Visit> candidates = getVisitsByPatient(patient);
		Visit ret = null;
		for (Visit candidate : candidates) {
			if (isSuitableVisit(candidate, department, new Date())) {
				ret = candidate;
			}
		}
		
		return ret;
	}
	
	/**
	 * @param a is the parent location 
	 * @param b is the child or parent location 
	 * @return true if a.equals(b) or a is an ancestor of b.
	 */
	private boolean isSameOrAncestor(Location a, Location b) {
		if (a == null || b == null) {
			return a == null && b == null;
		}
		return a.equals(b) || isSameOrAncestor(a, b.getParentLocation());
	}
	
	private Visit buildVisit(Patient patient, Location location, Date when) {
		final Visit visit = new Visit();
		visit.setPatient(patient);
		visit.setLocation(getLocationThatSupportsVisits(location));
		VisitType visitType = new VisitType();
		visitType.setName(OpenmrsConstants.GP_AT_FACILITY_VISIT_TYPE);
		Context.getVisitService().saveVisitType(visitType);
		visit.setVisitType(visitType);
		visit.setStartDatetime(when);
		return visit;
	}
	private List<VisitType> getVisitTypesToStop() {
		String gpValue = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE);
		if (StringUtils.isBlank(gpValue)) {
			return Collections.emptyList();
		} else {
			String[] visitTypeNames = getVisitTypeNamesFromGlobalPropertyValue(gpValue);
			return getVisitTypesFromVisitTypeNames(visitTypeNames);
		}
	}
	
	private String[] getVisitTypeNamesFromGlobalPropertyValue(String commaSeparatedNames) {
		String[] result = StringUtils.split(commaSeparatedNames.trim(), ",");
		for (int i = 0; i < result.length; i++) {
			String currName = result[i];
			result[i] = currName.trim().toLowerCase();
		}
		return result;
	}
	
	private List<VisitType> getVisitTypesFromVisitTypeNames(String[] visitTypeNames) {
		List<VisitType> result = new ArrayList<>();
		for (VisitType visitType : Context.getVisitService().getAllVisitTypes()) {
			if (ArrayUtils.contains(visitTypeNames, visitType.getName().toLowerCase())) {
				result.add(visitType);
			}
		}
		return result;
	}

}
