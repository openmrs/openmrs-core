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

import org.hibernate.annotations.BatchSize;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Diagnosis class defines the identification of the nature of an illness or other problem by
 * examination of the symptoms during an encounter (visit or interaction of a patient with a
 * healthcare worker).
 * 
 * @since 2.2
 */
@Entity
@Table(name = "encounter_diagnosis")
public class Diagnosis extends BaseCustomizableData<DiagnosisAttribute> implements FormRecordable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "diagnosis_id")
	private Integer diagnosisId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "nonCoded", column = @Column(name = "diagnosis_non_coded")) })
	@AssociationOverrides({ @AssociationOverride(name = "coded", joinColumns = @JoinColumn(name = "diagnosis_coded")),
	        @AssociationOverride(name = "specificName", joinColumns = @JoinColumn(name = "diagnosis_coded_name")) })
	private CodedOrFreeText diagnosis;
	
	@ManyToOne
	@JoinColumn(name = "condition_id")
	private Condition condition;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ConditionVerificationStatus certainty;
	
	@Column(name="dx_rank", nullable = false)
	private Integer rank;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Access(AccessType.PROPERTY)
	@OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("voided asc")
	@BatchSize(size = 100)
	private Set<DiagnosisAttribute> attributes = new LinkedHashSet<>();

	@Column(name="form_namespace_and_path")
	private String formNamespaceAndPath;

	/**
	 * Default no-arg Constructor; instantiates a new Diagnosis without passing any initial values.
	 */
	public Diagnosis() {
	}
	
	/**
	 * @param encounter the encounter for this diagnosis
	 * @param diagnosis the diagnosis to set
	 * @param certainty the certainty for the diagnosis
	 * @param rank the rank of the diagnosis
	 */
	public Diagnosis(Encounter encounter, CodedOrFreeText diagnosis, ConditionVerificationStatus certainty, Integer rank,
			Patient patient) {
		this.encounter = encounter;
		this.diagnosis = diagnosis;
		this.certainty = certainty;
		this.rank = rank;
		this.patient = patient;
	}

	/**
	 * @param encounter the encounter for this diagnosis
	 * @param diagnosis the diagnosis to set
	 * @param certainty the certainty for the diagnosis
	 * @param rank the rank of the diagnosis
	 * @param patient the patient diagnosed
	 * @param formNamespaceAndPath the form namespace and path
	 * @since 2.5.0
	 */
	public Diagnosis(Encounter encounter, CodedOrFreeText diagnosis, ConditionVerificationStatus certainty, Integer rank, Patient patient, String formNamespaceAndPath) {
		this.encounter = encounter;
		this.diagnosis = diagnosis;
		this.certainty = certainty;
		this.rank = rank;
		this.patient = patient;
		this.formNamespaceAndPath = formNamespaceAndPath;
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
	public void setDiagnosis(CodedOrFreeText diagnosis) {
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
	public void setCertainty(ConditionVerificationStatus certainty) {
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
	public void setRank(Integer rank) {
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
	public void setCondition(Condition condition) {
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

	/**
	 * Gets the form namespace and path
	 *
	 * @return Returns the formNamespaceAndPath.
	 * @since 2.5.0
	 */
	public String getFormNamespaceAndPath() {
		return formNamespaceAndPath;
	}

	/**
	 * Sets the form namespace and path
	 *
	 * @param formNamespaceAndPath the form namespace and path to set
	 * @since 2.5.0
	 */
	public void setFormNamespaceAndPath(String formNamespaceAndPath) {
		this.formNamespaceAndPath = formNamespaceAndPath;
	}

	/**
	 * Gets the namespace for the form field that was used to capture the details in the form
	 *
	 * @return the namespace
	 * @since 2.5.0
	 * <strong>Should</strong> return the namespace for a form field with or without a path otherwise null
	 */
	@Override
	public String getFormFieldNamespace() {
		return BaseFormRecordableOpenmrsData.getFormFieldNamespace(formNamespaceAndPath);
	}

	/**
	 * Gets the path for the form field that was used to capture the details in the form
	 *
	 * @return the form field path
	 * @since 2.5.0
	 * <strong>Should</strong> return the path for a form field with or without a namespace otherwise null
	 */
	@Override
	public String getFormFieldPath() {
		return BaseFormRecordableOpenmrsData.getFormFieldPath(formNamespaceAndPath);
	}

	/**
	 * Sets the namespace and path of the form field that was used to capture the details in the form.
	 *
	 * @param namespace the namespace of the form field
	 * @param formFieldPath the path of the form field
	 * @since 2.5.0
	 * <strong>Should</strong> set the underlying formNamespaceAndPath in the correct pattern
	 */
	@Override
	public void setFormField(String namespace, String formFieldPath) {
		formNamespaceAndPath = BaseFormRecordableOpenmrsData.getFormNamespaceAndPath(namespace, formFieldPath);
	}
}
