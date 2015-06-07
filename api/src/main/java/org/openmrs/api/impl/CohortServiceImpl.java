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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * API functions related to Cohorts
 */
@Transactional
public class CohortServiceImpl extends BaseOpenmrsService implements CohortService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private CohortDAO dao;
	
	/**
	 * @see org.openmrs.api.CohortService#setCohortDAO(org.openmrs.api.db.CohortDAO)
	 */
	public void setCohortDAO(CohortDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.CohortService#saveCohort(org.openmrs.Cohort)
	 */
	public Cohort saveCohort(Cohort cohort) throws APIException {
		if (cohort.getCohortId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_COHORTS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_COHORTS);
		}
		if (cohort.getName() == null) {
			throw new APIException("Cohort.save.nameRequired", (Object[]) null);
		}
		if (cohort.getDescription() == null) {
			throw new APIException("Cohort.save.descriptionRequired", (Object[]) null);
		}
		if (log.isInfoEnabled()) {
			log.info("Saving cohort " + cohort);
		}
		
		return dao.saveCohort(cohort);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#createCohort(org.openmrs.Cohort)
	 * @deprecated
	 */
	@Deprecated
	public Cohort createCohort(Cohort cohort) {
		return Context.getCohortService().saveCohort(cohort);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohort(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Cohort getCohort(Integer id) {
		return dao.getCohort(id);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohorts()
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	@Deprecated
	public List<Cohort> getCohorts() {
		return Context.getCohortService().getAllCohorts();
	}
	
	/**
	 * @see org.openmrs.api.CohortService#voidCohort(org.openmrs.Cohort, java.lang.String)
	 */
	public Cohort voidCohort(Cohort cohort, String reason) {
		// other setters done by the save handlers
		return Context.getCohortService().saveCohort(cohort);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohortByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Cohort getCohortByUuid(String uuid) {
		return dao.getCohortByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#addPatientToCohort(org.openmrs.Cohort,
	 *      org.openmrs.Patient)
	 */
	public Cohort addPatientToCohort(Cohort cohort, Patient patient) {
		if (!cohort.contains(patient)) {
			cohort.getMemberIds().add(patient.getPatientId());
			Context.getCohortService().saveCohort(cohort);
		}
		return cohort;
	}
	
	/**
	 * @see org.openmrs.api.CohortService#removePatientFromCohort(org.openmrs.Cohort,
	 *      org.openmrs.Patient)
	 */
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) {
		if (cohort.contains(patient)) {
			cohort.getMemberIds().remove(patient.getPatientId());
			Context.getCohortService().saveCohort(cohort);
		}
		return cohort;
	}
	
	/**
	 * @see org.openmrs.api.CohortService#updateCohort(org.openmrs.Cohort)
	 * @deprecated
	 */
	@Deprecated
	public Cohort updateCohort(Cohort cohort) {
		return Context.getCohortService().saveCohort(cohort);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohortsContainingPatient(org.openmrs.Patient)
	 */
	@Transactional(readOnly = true)
	public List<Cohort> getCohortsContainingPatient(Patient patient) {
		return dao.getCohortsContainingPatientId(patient.getPatientId());
	}
	
	@Transactional(readOnly = true)
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) {
		return dao.getCohortsContainingPatientId(patientId);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohorts(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Cohort> getCohorts(String nameFragment) throws APIException {
		return dao.getCohorts(nameFragment);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getAllCohorts()
	 */
	@Transactional(readOnly = true)
	public List<Cohort> getAllCohorts() throws APIException {
		return Context.getCohortService().getAllCohorts(false);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getAllCohorts(boolean)
	 */
	@Transactional(readOnly = true)
	public List<Cohort> getAllCohorts(boolean includeVoided) throws APIException {
		return dao.getAllCohorts(includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#getCohort(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Cohort getCohort(String name) throws APIException {
		return dao.getCohort(name);
	}
	
	/**
	 * @see org.openmrs.api.CohortService#purgeCohort(org.openmrs.Cohort)
	 */
	public Cohort purgeCohort(Cohort cohort) throws APIException {
		return dao.deleteCohort(cohort);
	}
	
}
