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

import jakarta.persistence.AssociationOverride
import jakarta.persistence.AssociationOverrides
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
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
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.Audited
import org.hibernate.type.SqlTypes
import org.openmrs.util.OpenmrsUtil
import java.util.Date

/**
 * The condition class records detailed information about a condition, problem, diagnosis, or other
 * situation or issue. This records information about a disease/illness identified from diagnosis or
 * identification of health issues/situations that require ongoing monitoring.
 *
 * @see [https://www.hl7.org/fhir/condition.html](https://www.hl7.org/fhir/condition.html)
 * @since 2.2
 */
@Entity
@Table(name = "conditions")
@Audited
class Condition() : BaseFormRecordableOpenmrsData() {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    var conditionId: Int? = null
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "nonCoded", column = Column(name = "condition_non_coded"))
    )
    @AssociationOverrides(
        AssociationOverride(name = "coded", joinColumns = [JoinColumn(name = "condition_coded")]),
        AssociationOverride(name = "specificName", joinColumns = [JoinColumn(name = "condition_coded_name")])
    )
    var condition: CodedOrFreeText? = null
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "clinical_status")
    var clinicalStatus: ConditionClinicalStatus? = null
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "verification_status")
    var verificationStatus: ConditionVerificationStatus? = null
    
    @ManyToOne
    @JoinColumn(name = "previous_version")
    var previousVersion: Condition? = null
    
    @Column(name = "additional_detail")
    var additionalDetail: String? = null
    
    private var _onsetDate: Date? = null
    var onsetDate: Date?
        get() = _onsetDate?.clone() as Date?
        set(value) {
            _onsetDate = value?.let { Date(it.time) }
        }
    
    private var _endDate: Date? = null
    var endDate: Date?
        get() = _endDate?.clone() as Date?
        set(value) {
            _endDate = value?.let { Date(it.time) }
        }
    
    @Transient
    var endReason: String? = null
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "encounter_id")
    var encounter: Encounter? = null
    
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
    constructor(
        condition: CodedOrFreeText?,
        clinicalStatus: ConditionClinicalStatus?,
        verificationStatus: ConditionVerificationStatus?,
        previousVersion: Condition?,
        additionalDetail: String?,
        onsetDate: Date?,
        endDate: Date?,
        patient: Patient?
    ) : this() {
        this.condition = condition
        this.clinicalStatus = clinicalStatus
        this.verificationStatus = verificationStatus
        this.previousVersion = previousVersion
        this.additionalDetail = additionalDetail
        this._onsetDate = onsetDate?.let { Date(it.time) }
        this._endDate = endDate?.let { Date(it.time) }
        this.patient = patient
    }
    
    override var id: Int?
        get() = conditionId
        set(value) {
            conditionId = value
        }
    
    /**
     * Compares properties with those in the given Condition to determine if they have the same meaning
     * This method will return true immediately following the creation of a Condition from another Condition
     * @see Condition.newInstance
     * This method will return false if any value is different, excepting identity data (id, uuid)
     * If the given instance is null, this will return false
     * @param c the Condition to compare against 	
     * @return true if the given Condition has the same meaningful properties as the passed Condition
     * @since 2.6.1
     */
    fun matches(c: Condition?): Boolean {
        if (c == null) {
            return false
        }
        val coft1 = condition ?: CodedOrFreeText()
        val coft2 = c.condition ?: CodedOrFreeText()
        
        return OpenmrsUtil.nullSafeEquals(previousVersion, c.previousVersion) &&
            OpenmrsUtil.nullSafeEquals(patient, c.patient) &&
            OpenmrsUtil.nullSafeEquals(encounter, c.encounter) &&
            OpenmrsUtil.nullSafeEquals(formNamespaceAndPath, c.formNamespaceAndPath) &&
            OpenmrsUtil.nullSafeEquals(clinicalStatus, c.clinicalStatus) &&
            OpenmrsUtil.nullSafeEquals(verificationStatus, c.verificationStatus) &&
            OpenmrsUtil.nullSafeEquals(coft1.coded, coft2.coded) &&
            OpenmrsUtil.nullSafeEquals(coft1.specificName, coft2.specificName) &&
            OpenmrsUtil.nullSafeEquals(coft1.nonCoded, coft2.nonCoded) &&
            OpenmrsUtil.nullSafeEquals(_onsetDate, c._onsetDate) &&
            OpenmrsUtil.nullSafeEquals(additionalDetail, c.additionalDetail) &&
            OpenmrsUtil.nullSafeEquals(_endDate, c._endDate) &&
            OpenmrsUtil.nullSafeEquals(endReason, c.endReason) &&
            OpenmrsUtil.nullSafeEquals(voided, c.voided) &&
            OpenmrsUtil.nullSafeEquals(voidedBy, c.voidedBy) &&
            OpenmrsUtil.nullSafeEquals(voidReason, c.voidReason) &&
            OpenmrsUtil.nullSafeEquals(dateVoided, c.dateVoided)
    }
    
    companion object {
        private const val serialVersionUID = 1L
        
        /**
         * Creates a new Condition instance from the passed condition such that the newly created Condition
         * matches the passed Condition @see Condition.matches, but does not equal the passed Condition (uuid, id differ)
         * @param condition the Condition to copy
         * @return a new Condition that is a copy of the passed condition
         */
        @JvmStatic
        fun newInstance(condition: Condition): Condition = copy(condition, Condition())
        
        /**
         * Copies property values from the fromCondition to the toCondition such that fromCondition
         * matches toCondition @see Condition.matches, but does not equal toCondition (uuid, id differ)
         * @param fromCondition the Condition to copy from
         * @param toCondition the Condition to copy into                      
         * @return a new Condition that is a copy of the passed condition
         */
        @JvmStatic
        fun copy(fromCondition: Condition, toCondition: Condition): Condition {
            toCondition.previousVersion = fromCondition.previousVersion
            toCondition.patient = fromCondition.patient
            toCondition.encounter = fromCondition.encounter
            toCondition.formNamespaceAndPath = fromCondition.formNamespaceAndPath
            toCondition.clinicalStatus = fromCondition.clinicalStatus
            toCondition.verificationStatus = fromCondition.verificationStatus
            toCondition.condition = fromCondition.condition
            toCondition.onsetDate = fromCondition.onsetDate
            toCondition.additionalDetail = fromCondition.additionalDetail
            toCondition.endDate = fromCondition.endDate
            toCondition.endReason = fromCondition.endReason
            toCondition.voided = fromCondition.voided
            toCondition.voidedBy = fromCondition.voidedBy
            toCondition.voidReason = fromCondition.voidReason
            toCondition.dateVoided = fromCondition.dateVoided
            return toCondition
        }
    }
}
