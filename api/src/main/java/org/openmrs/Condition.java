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

	private static final long serialVersionUID = 1L;

	private Integer conditionId;

	private CodedOrFreeText condition;

	private ConditionClinicalStatus clinicalStatus;

	private ConditionVerificationStatus verificationStatus;

	private Condition previousVersion;

	private String additionalDetail;

	private Date onsetDate;

	private Date endDate;
	
	private String endReason;

	private Patient patient;

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
	 * @param patient - the patient associated with the condition
	 */
	public Condition(CodedOrFreeText condition, ConditionClinicalStatus clinicalStatus,
		ConditionVerificationStatus verificationStatus, Condition previousVersion, String additionalDetail,
		Date onsetDate, Date endDate, Patient patient) {
		this.condition = condition;
		this.clinicalStatus = clinicalStatus;
		this.verificationStatus = verificationStatus;
		this.previousVersion = previousVersion;
		this.additionalDetail = additionalDetail;
		this.onsetDate = onsetDate;
		this.endDate = endDate;
		this.patient = patient;
	}

	public static Condition newInstance(Condition condition) {
		return copy(condition, new Condition());
	}

	public static Condition copy(Condition fromCondition, Condition toCondition) {
		toCondition.setPreviousVersion(fromCondition.getPreviousVersion());
		toCondition.setPatient(fromCondition.getPatient());
		toCondition.setClinicalStatus(fromCondition.getClinicalStatus());
		toCondition.setVerificationStatus(fromCondition.getVerificationStatus());
		toCondition.setCondition(fromCondition.getCondition());
		toCondition.setOnsetDate(fromCondition.getOnsetDate());
		toCondition.setAdditionalDetail(fromCondition.getAdditionalDetail());
		toCondition.setEndDate(fromCondition.getEndDate());
		toCondition.setVoided(fromCondition.getVoided());
		toCondition.setVoidedBy(fromCondition.getVoidedBy());
		toCondition.setVoidReason(fromCondition.getVoidReason());
		toCondition.setDateVoided(fromCondition.getDateVoided());
		return toCondition;
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
	 * @param conditionId the id of the condition to be set
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
	 * Gets the condition end date
	 *
	 * @return endDate - a date object that shows the end date of the condition
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Sets the end date
	 *
	 * @param endDate the end date to be set for the condition
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the condition end reason
	 *
	 * @return endReason - a string that shows the end reason of the condition
	 */
	public String getEndReason() {
		return endReason;
	}

	/**
	 * Sets the end reason
	 *
	 * @param endReason the end reason to be set for the condition
	 */
	public void setEndReason(String endReason) {
		this.endReason = endReason;
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

	/**
	 * Gets the patient associated with the condition
	 *
	 * @return patient - the patient object associated with the condition
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * Sets the patient associated with the condition
	 *
	 * @param patient - The patient object to be associated with condition
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		
		Condition condition = (Condition) o;
		
		if (!patient.equals(condition.patient)) {
			return false;
		}
		if (clinicalStatus != condition.clinicalStatus) {
			return false;
		}
		if (verificationStatus != condition.verificationStatus) {
			return false;
		}
		if (this.condition.getCoded() != null && !this.condition.getCoded().equals(condition.getCondition().getCoded())) {
			return false;
		}
		if (this.condition.getNonCoded() != null ?
				!this.condition.getNonCoded().equals(condition.getCondition().getNonCoded()) :
					condition.getCondition().getNonCoded() != null) {
			return false;
		}
		if (onsetDate != null ? !onsetDate.equals(condition.onsetDate) : condition.onsetDate != null) {
			return false;
		}
		if (additionalDetail != null ?
				!additionalDetail.equals(condition.additionalDetail) :
				condition.additionalDetail != null) {
			return false;
		}
		if (endDate != null ? !endDate.equals(condition.endDate) : condition.endDate != null) {
			return false;
		}
		return endReason != null ? endReason.equals(condition.endReason) : condition.endReason == null;
	}
}
