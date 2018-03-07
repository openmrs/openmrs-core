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

/**
 * Diagnosis class defines the identification of the nature of an illness or other problem 
 * by examination of the symptoms during an encounter (visit or interaction of a patient with a healthcare worker).
 * 
 * @since 2.2
 */
public class Diagnosis extends BaseChangeableOpenmrsData {

	private static final long serialVersionUID = 1L;

	private Integer diagnosisId;
	
	private Encounter encounter;
	
	private CodedOrFreeText diagnosis;
	
	private Condition condition;

	private ConditionVerificationStatus certainty;
	
	private Integer rank;

	private Patient patient;
	
	public Diagnosis() {
	}

	/**
	 * @param encounter the encounter for this diagnosis
	 * @param diagnosis the diagnosis to set  
	 * @param certainty the certainty for the diagnosis
	 * @param rank the rank of the diagnosis
	 */
	public Diagnosis(Encounter encounter, CodedOrFreeText diagnosis,ConditionVerificationStatus certainty,
					 Integer rank, Patient patient) {
		this.encounter = encounter;
		this.diagnosis = diagnosis;
		this.certainty = certainty;
		this.rank = rank;
		this.patient = patient;
	}
	
	/**
	 * Gets the diagnosis identifier.
	 * 
	 * @return the diagnosis identifier of this diagnosis
	 */
	@Override
	public Integer getId() {
		return getDiagnosisId();
	}

	/**
	 * Sets diagnosis identifier
	 * 
	 * @param diagnosisId the diagnosis identifier to set for this diagnosis
	 */
	@Override
	public void setId(Integer diagnosisId) {
		this.setDiagnosisId(diagnosisId);
	}

	/**
	 * Gets the diagnosis id.
	 *
	 * @return the diagnosis id of this diagnosis
	 */
	public Integer getDiagnosisId() {
		return diagnosisId;
	}

	/**
	 * Sets diagnosis id
	 *
	 * @param diagnosisId the diagnosis id to set for this diagnosis
	 */
	public void setDiagnosisId(Integer diagnosisId) {
		this.diagnosisId = diagnosisId;
	}
	
	/**
	 * Gets the encounter associated with this diagnosis.
	 * 
	 * @return encounter associated with this diagnosis
	 */
	public Encounter getEncounter() {
		return encounter;
	}

	/**
	 * Sets the encounter associated with this diagnosis.
	 * 
	 * @param encounter the encounter to set for this diagnosis
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/**
	 * Gets the diagnosis.
	 * 
	 * @return diagnosis 
	 */
	public CodedOrFreeText getDiagnosis() {
		return diagnosis;
	}

	/**
	 * Sets the diagnosis.
	 * 
	 * @param diagnosis the diagnosis to set
	 */
	public void setDiagnosis(CodedOrFreeText diagnosis)  {
		this.diagnosis = diagnosis;
	}

	/**
	 * Gets the diagnosis certainty.
	 * 
	 * @return certainty the certainty value to set for this diagnosis
	 */
	public ConditionVerificationStatus getCertainty() {
		return certainty;
	}

	/**
	 * Sets the diagnosis certainty
	 * 
	 * @param certainty the condition verification status to set for this diagnosis 
	 */
	public void setCertainty(ConditionVerificationStatus certainty)  {
		this.certainty = certainty;
	}

	/**
	 * Gets the diagnosis rank.
	 * 
	 * @return the rank of this diagnosis
	 */
	public Integer getRank() {
		return rank;
	}

	/**
	 * Sets diagnosis rank
	 * 
	 * @param rank the rank to set for this diagnosis.
	 */
	public void setRank(Integer rank)   {
		this.rank = rank;
	}

	/**
	 * Gets the diagnosis condition.
	 * 
	 * @return condition that this diagnosis is associated with.
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Sets diagnosis condition
	 * 
	 * @param condition the condition to set for this diagnosis.
	 */
	public void setCondition(Condition condition)   {
		this.condition = condition;
	}

	/**
	 * Gets the patient associated with the diagnosis
	 *
	 * @return patient - the patient object associated with the diagnosis
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * Sets patient with this diagnosis
	 *
	 * @param patient the patient with this diagnosis.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
}
