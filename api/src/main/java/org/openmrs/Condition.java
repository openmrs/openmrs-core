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

import org.openmrs.util.OpenmrsUtil;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * The condition class records detailed information about a condition, problem, diagnosis, or other
 * situation or issue. This records information about a disease/illness identified from diagnosis or
 * identification of health issues/situations that require ongoing monitoring.
 *
 * @see <a href=
 *      "https://www.hl7.org/fhir/condition.html">https://www.hl7.org/fhir/condition.html</a>
 * @since 2.2
 */
@Entity
@Table(name = "conditions")
public class Condition extends BaseFormRecordableOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "condition_id")
	private Integer conditionId;
	
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "nonCoded", column = @Column(name = "condition_non_coded")) })
	@AssociationOverrides({ @AssociationOverride(name = "coded", joinColumns = @JoinColumn(name = "condition_coded")),
	        @AssociationOverride(name = "specificName", joinColumns = @JoinColumn(name = "condition_coded_name")) })
	private CodedOrFreeText condition;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "clinical_status")
	private ConditionClinicalStatus clinicalStatus;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status")
	private ConditionVerificationStatus verificationStatus;
	
	@ManyToOne
	@JoinColumn(name = "previous_version")
	private Condition previousVersion;
	
	@Column(name = "additional_detail")
	private String additionalDetail;
	
	@Column(name = "onset_date")
	private Date onsetDate;
	
	@Column(name = "end_date")
	private Date endDate;
	
	@Transient
	private String endReason;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	public Condition() {
	}
	
	/**
	 * Convenience constructor to instantiate a condition class with all the necessary parameters
	 *
	 * @param condition - the condition to be set
	 * @param clinicalStatus - the clinical status of the condition to be set
	 * @param verificationStatus - the verification status of the condition, describing if the condition
	 *            is confirmed or not
	 * @param previousVersion - the previous version of the condition to be set
	 * @param additionalDetail - additional details of the condition to be set
	 * @param onsetDate - the date the condition is set
	 * @param patient - the patient associated with the condition
	 */
	public Condition(CodedOrFreeText condition, ConditionClinicalStatus clinicalStatus,
	    ConditionVerificationStatus verificationStatus, Condition previousVersion, String additionalDetail, Date onsetDate,
	    Date endDate, Patient patient) {
		this.condition = condition;
		this.clinicalStatus = clinicalStatus;
		this.verificationStatus = verificationStatus;
		this.previousVersion = previousVersion;
		this.additionalDetail = additionalDetail;
		this.onsetDate = onsetDate != null ? new Date(onsetDate.getTime()) : null;
		this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
		this.patient = patient;
	}

	/**
	 * Creates a new Condition instance from the passed condition such that the newly created Condition
	 * matches the passed Condition @see Condition#matches, but does not equal the passed Condition (uuid, id differ)
	 * @param condition the Condition to copy
	 * @return a new Condition that is a copy of the passed condition
	 */
	public static Condition newInstance(Condition condition) {
		return copy(condition, new Condition());
	}

	/**
	 * Copies property values from the fromCondition to the toCondition such that fromCondition
	 * matches toCondition @see Condition#matches, but does not equal toCondition (uuid, id differ)
	 * @param fromCondition the Condition to copy from
	 * @param toCondition the Condition to copy into                      
	 * @return a new Condition that is a copy of the passed condition
	 */
	public static Condition copy(Condition fromCondition, Condition toCondition) {
		toCondition.setPreviousVersion(fromCondition.getPreviousVersion());
		toCondition.setPatient(fromCondition.getPatient());
		toCondition.setEncounter(fromCondition.getEncounter());
		toCondition.setFormNamespaceAndPath(fromCondition.getFormNamespaceAndPath());
		toCondition.setClinicalStatus(fromCondition.getClinicalStatus());
		toCondition.setVerificationStatus(fromCondition.getVerificationStatus());
		toCondition.setCondition(fromCondition.getCondition());
		toCondition.setOnsetDate(fromCondition.getOnsetDate());
		toCondition.setAdditionalDetail(fromCondition.getAdditionalDetail());
		toCondition.setEndDate(fromCondition.getEndDate());
		toCondition.setEndReason(fromCondition.getEndReason());
		toCondition.setVoided(fromCondition.getVoided());
		toCondition.setVoidedBy(fromCondition.getVoidedBy());
		toCondition.setVoidReason(fromCondition.getVoidReason());
		toCondition.setDateVoided(fromCondition.getDateVoided());
		return toCondition;
	}

	/**
	 * Compares properties with those in the given Condition to determine if they have the same meaning
	 * This method will return true immediately following the creation of a Condition from another Condition
	 * @see Condition#newInstance(Condition)
	 * This method will return false if any value is different, excepting identity data (id, uuid)
	 * If the given instance is null, this will return false
	 * @param c the Condition to compare against 	
	 * @return true if the given Condition has the same meaningful properties as the passed Condition
	 * @since 2.6.1
	 */
	public boolean matches(Condition c) {
		if (c == null) {
			return false;
		}
		CodedOrFreeText coft1 = getCondition() == null ? new CodedOrFreeText() : getCondition();
		CodedOrFreeText coft2 = c.getCondition() == null ? new CodedOrFreeText() : c.getCondition();
		
		boolean ret = (OpenmrsUtil.nullSafeEquals(getPreviousVersion(), c.getPreviousVersion()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getPatient(), c.getPatient()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getEncounter(), c.getEncounter()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getFormNamespaceAndPath(), c.getFormNamespaceAndPath()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getClinicalStatus(), c.getClinicalStatus()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getVerificationStatus(), c.getVerificationStatus()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(coft1.getCoded(), coft2.getCoded()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(coft1.getSpecificName(), coft2.getSpecificName()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(coft1.getNonCoded(), coft2.getNonCoded()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getOnsetDate(), c.getOnsetDate()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getAdditionalDetail(), c.getAdditionalDetail()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getEndDate(), c.getEndDate()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getEndReason(), c.getEndReason()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getVoided(), c.getVoided()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getVoidedBy(), c.getVoidedBy()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getVoidReason(), c.getVoidReason()));
		ret = ret && (OpenmrsUtil.nullSafeEquals(getDateVoided(), c.getDateVoided()));
		return ret;
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
	 * @return verificationStatus - a ConditionVerificationStatus object that defines the verification
	 *         status of the condition
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
	 * @return onsetDate - a date object that shows the onset date which is the date the condition was
	 *         set
	 */
	public Date getOnsetDate() {
		return onsetDate != null ? (Date) onsetDate.clone() : null;
	}
	
	/**
	 * Sets the onset date
	 *
	 * @param onsetDate the onset date of the condition to be set
	 */
	public void setOnsetDate(Date onsetDate) {
		this.onsetDate = onsetDate != null ? new Date(onsetDate.getTime()) : null;
	}
	
	/**
	 * Gets the condition end date
	 *
	 * @return endDate - a date object that shows the end date of the condition
	 */
	public Date getEndDate() {
		return endDate != null ? (Date) endDate.clone() : null;
	}
	
	/**
	 * Sets the end date
	 *
	 * @param endDate the end date to be set for the condition
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
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
	
	/**
	 * Basic property getter for encounter
	 * 
	 * @return encounter - the associated encounter
	 * @since 2.4.0, 2.3.1
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * Basic property setter for encounter
	 *  
	 * @param encounter - the encounter to set
	 * @since 2.4.0, 2.3.1
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

}
