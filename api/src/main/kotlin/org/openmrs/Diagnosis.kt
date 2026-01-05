/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.AssociationOverride
import jakarta.persistence.AssociationOverrides
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.Audited
import org.hibernate.type.SqlTypes
import java.util.LinkedHashSet

/**
 * Diagnosis class defines the identification of the nature of an illness or other problem by
 * examination of the symptoms during an encounter (visit or interaction of a patient with a
 * healthcare worker).
 *
 * @since 2.2
 */
@Entity
@Table(name = "encounter_diagnosis")
@Audited
class Diagnosis() : BaseCustomizableData<DiagnosisAttribute>(), FormRecordable {

    companion object {
        private const val serialVersionUID: Long = 1L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id")
    var diagnosisId: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "encounter_id")
    var encounter: Encounter? = null

    @Embedded
    @AttributeOverrides(AttributeOverride(name = "nonCoded", column = Column(name = "diagnosis_non_coded")))
    @AssociationOverrides(
        AssociationOverride(name = "coded", joinColumns = [JoinColumn(name = "diagnosis_coded")]),
        AssociationOverride(name = "specificName", joinColumns = [JoinColumn(name = "diagnosis_coded_name")])
    )
    var diagnosis: CodedOrFreeText? = null

    @ManyToOne
    @JoinColumn(name = "condition_id")
    var condition: Condition? = null

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50)
    var certainty: ConditionVerificationStatus? = null

    @Column(name = "dx_rank", nullable = false)
    var rank: Int? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null

    @Access(AccessType.PROPERTY)
    @OneToMany(mappedBy = "diagnosis", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("voided asc")
    @BatchSize(size = 100)
    private var _attributes: MutableSet<DiagnosisAttribute> = LinkedHashSet()

    override fun getAttributes(): MutableSet<DiagnosisAttribute> = _attributes

    override fun setAttributes(attributes: MutableSet<DiagnosisAttribute>?) {
        _attributes = attributes ?: LinkedHashSet()
    }

    @Column(name = "form_namespace_and_path")
    var formNamespaceAndPath: String? = null

    /**
     * Constructor with all required fields.
     */
    constructor(
        encounter: Encounter?,
        diagnosis: CodedOrFreeText?,
        certainty: ConditionVerificationStatus?,
        rank: Int?,
        patient: Patient?
    ) : this() {
        this.encounter = encounter
        this.diagnosis = diagnosis
        this.certainty = certainty
        this.rank = rank
        this.patient = patient
    }

    /**
     * Constructor with all required fields including form namespace and path.
     * @since 2.5.0
     */
    constructor(
        encounter: Encounter?,
        diagnosis: CodedOrFreeText?,
        certainty: ConditionVerificationStatus?,
        rank: Int?,
        patient: Patient?,
        formNamespaceAndPath: String?
    ) : this(encounter, diagnosis, certainty, rank, patient) {
        this.formNamespaceAndPath = formNamespaceAndPath
    }

    /**
     * Gets the namespace for the form field that was used to capture the details in the form.
     * @since 2.5.0
     */
    override fun getFormFieldNamespace(): String? =
        BaseFormRecordableOpenmrsData.getFormFieldNamespace(formNamespaceAndPath)

    /**
     * Gets the path for the form field that was used to capture the details in the form.
     * @since 2.5.0
     */
    override fun getFormFieldPath(): String? =
        BaseFormRecordableOpenmrsData.getFormFieldPath(formNamespaceAndPath)

    /**
     * Sets the namespace and path of the form field that was used to capture the details in the form.
     * @since 2.5.0
     */
    override fun setFormField(namespace: String?, formFieldPath: String?) {
        formNamespaceAndPath = BaseFormRecordableOpenmrsData.getFormNamespaceAndPath(namespace, formFieldPath)
    }

    override var id: Integer?
        get() = diagnosisId?.let { Integer(it) }
        set(value) { diagnosisId = value?.toInt() }
}
