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

import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.ConditionService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConditionDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * This class implements the {@link ConditionService} interface
 * It defines the methods to handle the condition domain object
 *
 * @since 2.2
 */
@Transactional
public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {

	private ConditionDAO conditionDAO;

	public void setConditionDAO(ConditionDAO conditionDAO) {
		this.conditionDAO = conditionDAO;
	}

	public ConditionDAO getConditionDAO() {
		return conditionDAO;
	}

	/**
	 * Saves a condition
	 *
	 * @param condition - the condition to be saved
	 *                  
	 * @return the saved condition
	 */
	@Override
	public Condition saveCondition(Condition condition) {
		Date endDate = condition.getEndDate() != null ? condition.getEndDate() : new Date();
		if (condition.getEndReason() != null) {
			condition.setEndDate(endDate);
		}
		
		Condition existingCondition = getConditionByUuid(condition.getUuid());
		if (condition.equals(existingCondition)) {
			return existingCondition;
		}
		if (existingCondition == null) {
			return conditionDAO.saveCondition(condition);
		}

		condition = Condition.newInstance(condition);
		condition.setPreviousVersion(existingCondition);
		if (existingCondition.getClinicalStatus().equals(condition.getClinicalStatus())) {
			existingCondition.setVoided(true);
			conditionDAO.saveCondition(existingCondition);
			return conditionDAO.saveCondition(condition);
		}
		Date onSetDate = condition.getOnsetDate() != null ? condition.getOnsetDate() : new Date();
		existingCondition.setEndDate(onSetDate);
		conditionDAO.saveCondition(existingCondition);
		condition.setOnsetDate(onSetDate);

		return conditionDAO.saveCondition(condition);
	}

	/**
	 * Gets a condition based on the uuid
	 *
	 * @param uuid - uuid of the condition to be returned
	 *                
	 * @return the condition that is gotten by the given uuid
	 */
	@Override
	@Transactional(readOnly = true)
	public Condition getConditionByUuid(String uuid) {
		return conditionDAO.getConditionByUuid(uuid);
	}

	/**
	 * Gets a condition by id
	 *
	 * @param conditionId - the id of the Condition to retrieve
	 *                       
	 * @return the condition that is gotten by the given id
	 */
	@Override
	@Transactional(readOnly = true)
	public Condition getCondition(Integer conditionId) {
		return conditionDAO.getCondition(conditionId);
	}

	/**
	 * Gets a patient's active conditions
	 *
	 * @param patient - the patient to retrieve conditions for
	 *                   
	 * @return a list of the patient's active conditions
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Condition> getActiveConditions(Patient patient) {
		return conditionDAO.getActiveConditions(patient);
	}

	/**
	 * Voids a condition
	 *
	 * @param condition  - the condition to be voided
	 * @param voidReason - the reason for voiding the condition
	 *                   
	 * @return the voided condition
	 */
	@Override
	public Condition voidCondition(Condition condition, String voidReason) {
		return conditionDAO.saveCondition(condition);
	}

	/**
	 * Revive a condition
	 *
	 * @param condition Condition to unvoid
	 *                  
	 * @return the unvoided condition
	 */
	@Override
	public Condition unvoidCondition(Condition condition) {
		return Context.getConditionService().saveCondition(condition);
	}

	/**
	 * Completely remove a condition from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidCondition(Condition) for that one) If other things link to
	 * this condition, an error will be thrown.
	 *
	 * @param condition 
	 */
	@Override
	public void purgeCondition(Condition condition) {
		conditionDAO.deleteCondition(condition);
	}
}

