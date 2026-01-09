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
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.openmrs.util.OpenmrsUtil
import java.io.Serializable

/**
 * PersonAttributeType
 */
@Entity
@Table(name = "person_attribute_type")
@Audited
class PersonAttributeType() : BaseChangeableOpenmrsMetadata(), Serializable, Comparable<PersonAttributeType> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_attribute_type_id")
    var personAttributeTypeId: Int? = null

    @Column(name = "format", length = 50)
    var format: String? = null

    @Column(name = "foreign_key")
    var foreignKey: Int? = null

    @Column(name = "sort_weight", nullable = false)
    var sortWeight: Double? = null

    @GenericField
    @Column(name = "searchable", nullable = false)
    var searchable: Boolean? = false

    @ManyToOne
    @JoinColumn(name = "edit_privilege")
    var editPrivilege: Privilege? = null

    /** constructor with id */
    constructor(myPersonAttributeTypeId: Int?) : this() {
        this.personAttributeTypeId = myPersonAttributeTypeId
    }

    /**
     * @return the searchable status
     *
     * @deprecated as of 2.0, use [getSearchable]
     */
    @Deprecated("as of 2.0, use getSearchable()")
    @JsonIgnore
    fun isSearchable(): Boolean = getSearchable()

    fun getSearchable(): Boolean = searchable ?: false

    override fun toString(): String = name ?: ""

    override fun getId(): Int? = personAttributeTypeId

    override fun setId(id: Int?) {
        personAttributeTypeId = id
    }

    /**
     * @deprecated since 1.12. Use DefaultComparator instead.
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    @Deprecated("since 1.12. Use DefaultComparator instead.")
    override fun compareTo(other: PersonAttributeType): Int {
        return DefaultComparator().compare(this, other)
    }

    /**
     * Provides a default comparator.
     * @since 1.12
     */
    class DefaultComparator : Comparator<PersonAttributeType>, Serializable {
        override fun compare(pat1: PersonAttributeType, pat2: PersonAttributeType): Int {
            return OpenmrsUtil.compareWithNullAsGreatest(pat1.personAttributeTypeId, pat2.personAttributeTypeId)
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 2112313431211L
    }
}
