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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.MedicationDispense;
import org.openmrs.api.APIException;
import org.openmrs.api.MedicationDispenseService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.MedicationDispenseDAO;
import org.openmrs.parameter.MedicationDispenseCriteria;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * This class implements the {@link MedicationDispenseService} interface
 * It defines the API for interacting with MedicationDispense objects.
 * @since 2.6.0
 */
@Transactional
public class MedicationDispenseServiceImpl extends BaseOpenmrsService implements MedicationDispenseService {

	private MedicationDispenseDAO medicationDispenseDAO;

	public void setMedicationDispenseDAO(MedicationDispenseDAO conditionDAO) {
		this.medicationDispenseDAO = conditionDAO;
	}

	public MedicationDispenseDAO getMedicationDispenseDAO() {
		return medicationDispenseDAO;
	}

	@Override
	@Transactional(readOnly = true)
	public MedicationDispense getMedicationDispense(Integer medicationDispenseId) {
		return medicationDispenseDAO.getMedicationDispense(medicationDispenseId);
	}

	@Override
	@Transactional(readOnly = true)
	public MedicationDispense getMedicationDispenseByUuid(String uuid) {
		return medicationDispenseDAO.getMedicationDispenseByUuid(uuid);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MedicationDispense> getMedicationDispenseByCriteria(MedicationDispenseCriteria criteria) {
		return medicationDispenseDAO.getMedicationDispenseByCriteria(criteria);
	}

	@Override
	public MedicationDispense saveMedicationDispense(MedicationDispense medicationDispense) {
		return medicationDispenseDAO.saveMedicationDispense(medicationDispense);
	}

	@Override
	public MedicationDispense voidMedicationDispense(
		MedicationDispense medicationDispense, String reason) {
		if (StringUtils.isBlank(reason)) {
			throw new IllegalArgumentException("voidReason cannot be null or empty");
		}
		// Actual voiding logic is done in the BaseVoidHandler
		return saveMedicationDispense(medicationDispense);
	}

	@Override
	public MedicationDispense unvoidMedicationDispense(MedicationDispense medicationDispense) {
		// Actual un-voiding logic is done in the BaseUnvoidHandler
		return saveMedicationDispense(medicationDispense);
	}

	@Override
	public void purgeMedicationDispense(MedicationDispense medicationDispense) throws APIException {
		medicationDispenseDAO.deleteMedicationDispense(medicationDispense);
	}
}
