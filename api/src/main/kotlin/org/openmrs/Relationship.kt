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
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import java.util.Date

/**
 * Relationship
 */
@Entity
@Table(name = "relationship")
@Audited
open class Relationship : BaseChangeableOpenmrsData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relationship_id_seq")
    @GenericGenerator(
        name = "relationship_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "relationship_relationship_id_seq")]
    )
    @Column(name = "relationship_id")
    open var relationshipId: Int? = null
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "person_a", nullable = false)
    open var personA: Person? = null
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "relationship", nullable = false)
    open var relationshipType: RelationshipType? = null
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "person_b", nullable = false)
    open var personB: Person? = null
    
    @Column(name = "start_date", length = 19)
    open var startDate: Date? = null
    
    @Column(name = "end_date", length = 19)
    open var endDate: Date? = null
    
    /** default constructor */
    constructor()
    
    /** constructor with id */
    constructor(relationshipId: Int?) {
        this.relationshipId = relationshipId
    }
    
    constructor(personA: Person?, personB: Person?, type: RelationshipType?) {
        this.personA = personA
        this.personB = personB
        this.relationshipType = type
    }
    
    /**
     * Does a shallow copy of this Relationship. Does NOT copy relationshipId
     * 
     * @return a copy of this relationship
     */
    fun copy(): Relationship = copyHelper(Relationship())
    
    /**
     * The purpose of this method is to allow subclasses of Relationship to delegate a portion of
     * their copy() method back to the superclass, in case the base class implementation changes.
     * 
     * @param target a Relationship that will have the state of this copied into it
     * @return the Relationship that was passed in, with state copied into it
     */
    protected open fun copyHelper(target: Relationship): Relationship {
        target.personA = personA
        target.relationshipType = relationshipType
        target.personB = personB
        target.creator = creator
        target.dateCreated = dateCreated
        target.voided = voided
        target.voidedBy = voidedBy
        target.dateVoided = dateVoided
        target.voidReason = voidReason
        return target
    }
    
    override fun toString(): String {
        val relType = relationshipType?.aIsToB ?: "NULL"
        return "$personA is the $relType of $personB"
    }
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override fun getId(): Int? = relationshipId
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.setId
     */
    override fun setId(id: Int?) {
        relationshipId = id
    }
    
    companion object {
        const val serialVersionUID = 323423L
    }
}
