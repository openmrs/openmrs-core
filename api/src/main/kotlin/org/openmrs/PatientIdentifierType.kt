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

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.hibernate.type.SqlTypes
import java.io.Serializable

/**
 * PatientIdentifierType
 */
@Entity
@Table(name = "patient_identifier_type")
@Audited
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "name", nullable = false, length = 50)),
    AttributeOverride(name = "description", column = Column(name = "description", length = 65535))
)
class PatientIdentifierType() : BaseChangeableOpenmrsMetadata(), Serializable {

    /**
     * Enumerates the possible ways that location may be applicable for a particular Patient
     * Identifer Type
     */
    enum class LocationBehavior {
        /**
         * Indicates that location is required for the current identifier type
         */
        REQUIRED,

        /**
         * Indicates that location is not used for the current identifier type
         */
        NOT_USED
    }

    /**
     * Enumeration for the way to handle uniqueness among identifiers for a given identifier type
     */
    enum class UniquenessBehavior {
        /**
         * Indicates that identifiers should be globally unique
         */
        UNIQUE,

        /**
         * Indicates that duplicates identifiers are allowed
         */
        NON_UNIQUE,

        /**
         * Indicates that identifiers should be unique only across a location if the identifier's
         * location property is not null
         */
        LOCATION
    }

    @DocumentId
    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "patient_identifier_type_id")
    var patientIdentifierTypeId: Int? = null

    @Column(name = "format")
    var format: String? = null

    @GenericField
    @Column(name = "required", nullable = false)
    var required: Boolean? = false

    @Column(name = "format_description", length = 250)
    var formatDescription: String? = null

    @Column(name = "validator", length = 200)
    var validator: String? = null

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "location_behavior", length = 50)
    var locationBehavior: LocationBehavior? = null

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uniqueness_behavior", length = 50)
    var uniquenessBehavior: UniquenessBehavior? = null

    /** constructor with id */
    constructor(patientIdentifierTypeId: Int?) : this() {
        this.patientIdentifierTypeId = patientIdentifierTypeId
    }

    fun getRequired(): Boolean = required ?: false

    /**
     * @return Whether this identifier type has a validator.
     */
    fun hasValidator(): Boolean = StringUtils.isNotEmpty(validator)

    override fun toString(): String = name ?: ""

    override fun getId(): Int? = patientIdentifierTypeId

    override fun setId(id: Int?) {
        patientIdentifierTypeId = id
    }

    companion object {
        private const val serialVersionUID = 211231L
    }
}
