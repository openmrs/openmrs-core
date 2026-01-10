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
import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited

/**
 * Privilege
 * 
 * @version 1.0
 */
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "privilege")
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "name", nullable = true))
)
open class Privilege : BaseChangeableOpenmrsMetadata {
    
    @Id
    @Column(name = "privilege", length = 250)
    open var privilege: String? = null
    
    /** default constructor */
    constructor()
    
    /** constructor with id */
    constructor(privilege: String?) {
        this.privilege = privilege
    }
    
    constructor(privilege: String?, description: String?) {
        this.privilege = privilege
        this.description = description
    }
    
    override fun getName(): String? = privilege
    
    override fun toString(): String = privilege ?: ""
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override fun getId(): Int? {
        throw UnsupportedOperationException()
    }
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.setId
     */
    override fun setId(id: Int?) {
        throw UnsupportedOperationException()
    }
    
    companion object {
        const val serialVersionUID = 312L
    }
}
