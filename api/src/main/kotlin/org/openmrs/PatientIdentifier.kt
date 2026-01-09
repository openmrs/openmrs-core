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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import org.hibernate.search.engine.backend.types.Sortable
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.api.db.hibernate.search.SearchAnalysis
import org.openmrs.util.OpenmrsUtil
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * A Patient can have zero to n identifying PatientIdentifier(s). PatientIdentifiers
 * are anything from medical record numbers, to social security numbers, to driver's licenses. The
 * type of identifier is defined by the PatientIdentifierType. A PatientIdentifier also contains a
 * Location.
 *
 * @see org.openmrs.PatientIdentifierType
 */
@Indexed
@Audited
@Entity
@Table(name = "patient_identifier")
class PatientIdentifier() : BaseChangeableOpenmrsData(), Serializable, Cloneable, Comparable<PatientIdentifier> {

    @DocumentId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_identifier_id", nullable = false)
    var patientIdentifierId: Int? = null

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @IndexedEmbedded(includeEmbeddedObjectId = true)
    @AssociationInverseSide(inversePath = [ObjectPath([PropertyValue(propertyName = "identifiers")])])
    var patient: Patient? = null

    @FullTextField(name = "identifierPhrase", analyzer = SearchAnalysis.PHRASE_ANALYZER)
    @FullTextField(name = "identifierExact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "identifierStart", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "identifierAnywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @KeywordField(name = "identifierExact_sort", sortable = Sortable.YES)
    @Column(name = "identifier", length = 50, nullable = false)
    var identifier: String? = null

    @ManyToOne
    @JoinColumn(name = "identifier_type", nullable = false)
    @IndexedEmbedded(includeEmbeddedObjectId = true)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    var identifierType: PatientIdentifierType? = null

    @ManyToOne
    @JoinColumn(name = "location_id")
    var location: Location? = null

    @ManyToOne
    @JoinColumn(name = "patient_program_id")
    var patientProgram: PatientProgram? = null

    @Column(name = "preferred", nullable = false)
    @GenericField
    var preferred: Boolean? = false

    /**
     * Convenience constructor for creating a basic identifier
     *
     * @param identifier String identifier
     * @param type PatientIdentifierType
     * @param location Location of the identifier
     */
    constructor(identifier: String?, type: PatientIdentifierType?, location: Location?) : this() {
        this.identifier = identifier
        this.identifierType = type
        this.location = location
    }

    /**
     * Compares this PatientIdentifier object to the given otherIdentifier. This method differs from
     * [equals] in that this method compares the inner fields of each identifier for
     * equality. Note: Null/empty fields on `otherIdentifier` /will not/ cause a false value to be returned
     *
     * @param otherIdentifier PatientiIdentifier with which to compare
     * @return boolean true/false whether or not they are the same names
     */
    fun equalsContent(otherIdentifier: PatientIdentifier): Boolean {
        var returnValue = true

        // these are the methods to compare.
        val methods = arrayOf("getIdentifier", "getIdentifierType", "getLocation")

        val identifierClass = this.javaClass

        // loop over all of the selected methods and compare this and other
        for (methodName in methods) {
            try {
                val method = identifierClass.getMethod(methodName)

                val thisValue = method.invoke(this)
                val otherValue = method.invoke(otherIdentifier)

                if (otherValue != null) {
                    returnValue = returnValue && otherValue == thisValue
                }

            } catch (e: NoSuchMethodException) {
                log.warn("No such method for comparison $methodName", e)
            } catch (e: Exception) {
                log.error("Error while comparing identifiers", e)
            }
        }

        return returnValue
    }

    fun getPreferred(): Boolean = preferred ?: false

    /**
     * @return the preferred status
     *
     * @deprecated as of 2.0, use [getPreferred]
     */
    @Deprecated("as of 2.0, use getPreferred()")
    @JsonIgnore
    fun isPreferred(): Boolean = getPreferred()

    override fun toString(): String = identifier ?: ""

    override fun getId(): Int? = patientIdentifierId

    override fun setId(id: Int?) {
        patientIdentifierId = id
    }

    /**
     * bitwise copy of the PatientIdentifier object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
     * PatientIdentifier.patient object in this object AND the cloned object will point at the same
     * patient
     *
     * @return New PatientIdentifier object
     * @since 2.2.0
     */
    public override fun clone(): Any {
        return try {
            super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError("PatientIdentifier should be cloneable")
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @deprecated since 1.12. Use DefaultComparator instead.
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    @Deprecated("since 1.12. Use DefaultComparator instead.")
    override fun compareTo(other: PatientIdentifier): Int {
        return DefaultComparator().compare(this, other)
    }

    /**
     * Provides a default comparator.
     * @since 1.12
     */
    class DefaultComparator : Comparator<PatientIdentifier>, Serializable {
        override fun compare(pi1: PatientIdentifier, pi2: PatientIdentifier): Int {
            var retValue = pi1.voided!!.compareTo(pi2.voided!!)
            if (retValue == 0) {
                retValue = pi1.getPreferred().compareTo(pi2.getPreferred())
            }
            if (retValue == 0) {
                retValue = OpenmrsUtil.compareWithNullAsLatest(pi1.dateCreated, pi2.dateCreated)
            }
            if (pi1.identifierType == null && pi2.identifierType == null) {
                return 0
            }
            if (pi1.identifierType == null && pi2.identifierType != null) {
                retValue = 1
            }
            if (pi1.identifierType != null && pi2.identifierType == null) {
                retValue = -1
            }
            if (retValue == 0) {
                retValue = OpenmrsUtil.compareWithNullAsGreatest(
                    pi1.identifierType?.patientIdentifierTypeId,
                    pi2.identifierType?.patientIdentifierTypeId
                )
            }
            if (retValue == 0) {
                retValue = OpenmrsUtil.compareWithNullAsGreatest(pi1.identifier, pi2.identifier)
            }

            // if we've gotten this far, just check all identifier values.  If they are
            // equal, leave the objects at 0.  If not, arbitrarily pick retValue=1
            // and return that (they are not equal).
            if (retValue == 0 && !pi1.equalsContent(pi2)) {
                retValue = 1
            }

            return retValue
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 1123121L
        private val log = LoggerFactory.getLogger(PatientIdentifier::class.java)
    }
}
