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
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * Represent allergen
 */
@Embeddable
class Allergen() {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "allergen_type")
    var allergenType: AllergenType? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "coded_allergen")
    var codedAllergen: Concept? = null
        set(value) {
            field = value
            if (value != null && value.uuid != getOtherNonCodedConceptUuid()) {
                nonCodedAllergen = null
            }
        }

    @Column(name = "non_coded_allergen")
    var nonCodedAllergen: String? = null
        set(value) {
            field = value
            if (StringUtils.isNotBlank(value) && codedAllergen != null
                && codedAllergen?.uuid != getOtherNonCodedConceptUuid()) {
                codedAllergen = null
            }
        }

    constructor(allergenType: AllergenType?, codedAllergen: Concept?, nonCodedAllergen: String?) : this() {
        this.allergenType = allergenType
        this.codedAllergen = codedAllergen
        this.nonCodedAllergen = nonCodedAllergen
    }

    val isCoded: Boolean
        get() = codedAllergen != null && codedAllergen?.uuid != getOtherNonCodedConceptUuid()

    override fun toString(): String =
        if (StringUtils.isNotBlank(nonCodedAllergen)) {
            nonCodedAllergen!!
        } else {
            codedAllergen?.getName()?.name ?: ""
        }

    /**
     * Checks if this allergen is the same as the given one
     *
     * @param allergen the given allergen to test with
     * @return true if the same, else false
     */
    fun isSameAllergen(allergen: Allergen): Boolean =
        if (isCoded) {
            allergen.codedAllergen != null && codedAllergen == allergen.codedAllergen
        } else {
            nonCodedAllergen != null && allergen.nonCodedAllergen != null
                && nonCodedAllergen.equals(allergen.nonCodedAllergen, ignoreCase = true)
        }

    companion object {
        @JvmStatic
        private var _otherNonCodedConceptUuid: String? = null

        /**
         * Sets other non coded concept uuid constant.
         *
         * @param otherNonCodedConceptUuid
         * @since 2.0
         */
        @JvmStatic
        fun setOtherNonCodedConceptUuid(otherNonCodedConceptUuid: String?) {
            _otherNonCodedConceptUuid = otherNonCodedConceptUuid
        }

        /**
         * Returns other non coded concept uuid constant.
         *
         * @return other non coded concept uuid constant
         * @since 2.0
         */
        @JvmStatic
        fun getOtherNonCodedConceptUuid(): String? = _otherNonCodedConceptUuid
    }
}
