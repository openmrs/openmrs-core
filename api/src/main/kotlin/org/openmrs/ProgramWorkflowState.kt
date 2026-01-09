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

import org.hibernate.envers.Audited

/**
 * ProgramWorkflowState
 */
@Audited
open class ProgramWorkflowState : BaseChangeableOpenmrsMetadata {
    
    companion object {
        private const val serialVersionUID = 1L
    }
    
    // ******************
    // Properties
    // ******************
    
    open var programWorkflowStateId: Int? = null
    
    open var programWorkflow: ProgramWorkflow? = null
    
    open var concept: Concept? = null
    
    open var initial: Boolean? = null
    
    open var terminal: Boolean? = null
    
    // ******************
    // Constructors
    // ******************
    
    /** Default Constructor */
    constructor()
    
    /** Constructor with id */
    constructor(programWorkflowStateId: Int?) {
        this.programWorkflowStateId = programWorkflowStateId
    }
    
    // ******************
    // Instance methods
    // ******************
    
    /** @see Object.toString */
    override fun toString(): String {
        return "State ${concept.toString()} initial=$initial terminal=$terminal"
    }
    
    // ******************
    // Property Access
    // ******************
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = programWorkflowStateId
        set(id) {
            programWorkflowStateId = id
        }
}
