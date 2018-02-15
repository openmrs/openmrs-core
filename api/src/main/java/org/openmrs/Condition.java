/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import java.util.Date;

/**
 * The condition class records detailed information about a condition, problem, diagnosis, or other situation or issue.
 * This records information about a disease/illness identified from diagnosis
 * or identification of health issues/situations that require ongoing monitoring.
 * 
 * @see <a href="https://www.hl7.org/fhir/condition.html">https://www.hl7.org/fhir/condition.html</a>
 * 
 * @since 2.2
 */
public class Condition extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 1L;

	private Integer conditionId;
	
	private CodedOrFreeText condition;

	private ConditionClinicalStatus clinicalStatus;

	private ConditionVerificationStatus verificationStatus;

	private Condition previousVersion;

	private String additionalDetail;

	private Date onsetDate;

	public Condition() {
	}

	/**
	 * Convenience constructor to instantiate a condition class with all the necessary parameters
	 * 
	 * @param condition - the condition to be set
	 * @param clinicalStatus - the clinical status of the condition to be set
	 * @param verificationStatus - the verification status of the condition, describing if the condition is confirmed or not
	 * @param previousVersion - the previous version of the condition to be set
	 * @param additionalDetail - additional details of the condition to be set
	 * @param onsetDate - the date the condition is set
	 */
	public Condition(CodedOrFreeText condition, ConditionClinicalStatus clinicalStatus,
		ConditionVerificationStatus verificationStatus, Condition previousVersion, String additionalDetail,
		Date onsetDate) {
		this.condition = condition;
		this.clinicalStatus = clinicalStatus;
		this.verificationStatus = verificationStatus;
		this.previousVersion = previousVersion;
		this.additionalDetail = additionalDetail;
		this.onsetDate = onsetDate;
	}

	/**
	 * Gets the condition id 
	 * 
	 * @return conditionId - the id of the condition
	 */
	public Integer getConditionId() {
		return conditionId;
	}

	/**
	 * Sets the condition id
	 * 
	 * @param conditionId the id of the codition to be set
	 */
	public void setConditionId(Integer conditionId) {
		this.conditionId = conditionId;
	}

	/**
	 * Gets the condition that has been set
	 * 
	 * @return condition - a CodedOrFreeText object that defines the condition
	 */
	public CodedOrFreeText getCondition() {
		return condition;
	}

	/**
	 * Sets the condition
	 * 
	 * @param condition the condition to be set
	 */
	public void setCondition(CodedOrFreeText condition) {
		this.condition = condition;
	}

	/**
	 * Gets the clinical status of the condition
	 * 
	 * @return clinicalStatus - a ConditionClinicalStatus object that defines the clinical status
	 */
	public ConditionClinicalStatus getClinicalStatus() {
		return clinicalStatus;
	}

	/**
	 * Sets the clinical status of the condition
	 * 
	 * @param clinicalStatus the clinical status of the condition to be set
	 */
	public void setClinicalStatus(ConditionClinicalStatus clinicalStatus) {
		this.clinicalStatus = clinicalStatus;
	}

	/**
	 * Gets the verification status of the condition
	 * 
	 * @return verificationStatus - a ConditionVerificationStatus object that defines the verification status of the condition
	 */
	public ConditionVerificationStatus getVerificationStatus() {
		return verificationStatus;
	}

	/**
	 * Sets the verification status of the condition
	 * 
	 * @param verificationStatus the verification status of the condition to be set
	 */
	public void setVerificationStatus(ConditionVerificationStatus verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	/**
	 * Gets the previous version of the condition
	 *
	 * @return previousVersion - a condition object showing the previous version of the condition
	 */
	public Condition getPreviousVersion() {
		return previousVersion;
	}

	/**
	 * Sets the previous version of the condition
	 *
	 * @param previousVersion the previous version of the condition to be set
	 */
	public void setPreviousVersion(Condition previousVersion) {
		this.previousVersion = previousVersion;
	}

	/**
	 * Gets the addition detail of the condition
	 * 
	 * @return additionalDetail - a string showing additional detail of the condition
	 */
	public String getAdditionalDetail() {
		return additionalDetail;
	}

	/**
	 * Sets the additional detail of the condition
	 * 
	 * @param additionalDetail the additional detail of the condition to be set
	 */
	public void setAdditionalDetail(String additionalDetail) {
		this.additionalDetail = additionalDetail;
	}

	/**
	 * Gets the onset date of the condition
	 * 
	 * @return onsetDate - a date object that shows the onset date which is the date the condition was set
	 */
	public Date getOnsetDate() {
		return onsetDate;
	}

	/**
	 * Sets the onset date
	 * 
	 * @param onsetDate the onset date of the condition to be set
	 */
	public void setOnsetDate(Date onsetDate) {
		this.onsetDate = onsetDate;
	}

	/**
	 * @return id - The unique Identifier for the object
	 */
	@Override
	public Integer getId() {
		return getConditionId();
	}

	/**
	 * @param id - The unique Identifier for the object
	 */
	@Override
	public void setId(Integer id) {
		setConditionId(id);
	}

}
