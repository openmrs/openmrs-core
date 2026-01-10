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

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.envers.Audited

/**
 * Defines a type of relationship between two people in the database.
 *
 * A relationship is two-way. There is a name for the relationship in both directions.
 *
 * For example:
 * a) physician Joe
 * b) patient Bob
 * Joe is the "physician of" Bob **and** Bob is the patient of Joe. Once you can establish one of
 * the two relationships, you automatically know the other.
 *
 * ALL relationships are two-way and can be defined as such.
 *
 * RelationshipTypes should be defined as **gender non-specific** For example: A mother and her
 * son. Instead of having a RelationshipType defined as mother-son, it should be defined as
 * Parent-child. (This avoids the duplicative types that would come out like father-son,
 * father-daughter, mother-daughter)
 *
 * In English, we run into a tricky RelationshipType with aunts and uncles. We have chosen to define
 * them as aunt/uncle-niece/nephew.
 */
@Audited
open class RelationshipType : BaseChangeableOpenmrsMetadata {
    
    open var relationshipTypeId: Int? = null
    
    open var aIsToB: String? = null
    
    open var bIsToA: String? = null
    
    open var weight: Int? = 0
    
    open var preferred: Boolean? = false
    
    /** default constructor */
    constructor()
    
    /** constructor with id */
    constructor(relationshipTypeId: Int?) {
        this.relationshipTypeId = relationshipTypeId
    }
    
    /**
     * "Preferred" relationship types are those that should be shown as default types when
     * adding/editing a person's relationships
     * 
     * @return the preferred status
     * 
     * @deprecated as of 2.0, use [getPreferred]
     */
    @Deprecated("as of 2.0, use getPreferred()", ReplaceWith("preferred"))
    @JsonIgnore
    fun isPreferred(): Boolean? = preferred
    
    override fun toString(): String = "${aIsToB ?: ""}/${bIsToA ?: ""}"
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override fun getId(): Int? = relationshipTypeId
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.setId
     */
    override fun setId(id: Int?) {
        relationshipTypeId = id
    }
    
    companion object {
        const val serialVersionUID = 4223L
    }
}
