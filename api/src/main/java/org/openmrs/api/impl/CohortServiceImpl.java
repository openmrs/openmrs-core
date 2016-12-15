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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.User;
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

	protected final Log log = LogFactory.getLog(this.getClass());

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
	 * @see org.openmrs.api.CohortService#getCohort(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public Cohort getCohort(Integer id) {
		return dao.getCohort(id);
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
		if (!cohort.contains(patient.getPatientId())) {
			CohortMembership cohortMembership = new CohortMembership(patient);
			Context.getCohortService().addMembershipToCohort(cohort, cohortMembership);
			Context.getCohortService().saveCohort(cohort);
		}
		return cohort;
	}

	/**
	 * @see org.openmrs.api.CohortService#removePatientFromCohort(org.openmrs.Cohort,
	 *      org.openmrs.Patient)
	 */
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) {
		if (cohort.contains(patient.getPatientId())) {
			cohort.removeMember(patient);
			Context.getCohortService().saveCohort(cohort);
		}
		return cohort;
	}

	/**
	 * @see org.openmrs.api.CohortService#getCohortsContainingPatient(org.openmrs.Patient)
	 */
	@Transactional(readOnly = true)
	public List<Cohort> getCohortsContainingPatient(Patient patient, Boolean voided) {
		return dao.getCohortsContainingPatientId(patient.getPatientId(), voided);
	}
	
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

	/**
	 * @see org.openmrs.api.CohortService#addMembershipToCohort(org.openmrs.Cohort,
	 * 		org.openmrs.CohortMembership)
	 */
	public Cohort addMembershipToCohort(Cohort cohort, CohortMembership cohortMembership) throws APIException {
		cohort.addMembership(cohortMembership);
		Context.getCohortService().saveCohort(cohort);
		return cohort;
	}

	/**
	 * @see org.openmrs.api.CohortService#removeMemberShipFromCohort(org.openmrs.Cohort,
	 * 		org.openmrs.CohortMembership)
	 */
	public Cohort removeMemberShipFromCohort(Cohort cohort, CohortMembership cohortMembership) throws APIException {
		if (cohort.contains(cohortMembership.getPatient())) {
			cohort.removeMembership(cohortMembership);
			Context.getCohortService().saveCohort(cohort);
		}
		return cohort;
	}

	/**
	 * @see org.openmrs.api.CohortService#patientVoided(org.openmrs.Patient)
	 */
	public void patientVoided(Patient patient) throws APIException {
		List<Cohort> cohorts = Context.getCohortService().getCohortsContainingPatient(patient);
		for (Cohort cohort : cohorts) {
			List<CohortMembership> membersToVoid = cohort.getMembers().stream()
					.filter(m -> !m.getVoided() && patient.getVoided() && m.getPatient().getPatientId().equals(patient.getPatientId()))
					.collect(Collectors.toList());
			for (CohortMembership member : membersToVoid) {
				member.setVoided(patient.getVoided());
				member.setDateVoided(patient.getDateVoided());
				member.setVoidedBy(patient.getVoidedBy());
				member.setVoidReason(patient.getVoidReason());
			}
			cohort.removeMember(patient);
			Context.getCohortService().saveCohort(cohort);
		}
	}
	
	/**
	 * @see org.openmrs.api.CohortService#patientUnvoided(Patient, User, Date, String)
	 */
	public void patientUnvoided(Patient patient, User voidedBy, Date dateVoided, String voidReason) throws APIException {
		List<Cohort> cohorts = Context.getCohortService().getCohortsContainingPatient(patient);
		for (Cohort cohort : cohorts) {
			List<CohortMembership> membersToUnvoid = cohort.getMembers().stream()
					.filter(m -> m.getVoided() &&
							m.getPatient().getPatientId().equals(patient.getPatientId()) &&
							m.getVoidedBy().equals(voidedBy) &&
							DateUtils.isSameDay(m.getDateVoided(), dateVoided) &&
							m.getVoidReason().equals(voidReason))
					.collect(Collectors.toList());
			if (!membersToUnvoid.isEmpty()) {
				for (CohortMembership member : membersToUnvoid) {
					member.setVoided(false);
					member.setDateVoided(null);
					member.setVoidedBy(null);
					member.setVoidReason(null);
				}
			}
			Context.getCohortService().saveCohort(cohort);
		}
	}
}
