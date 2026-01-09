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
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import org.openmrs.annotation.AllowDirectAccess

/**
 * Program
 */
@Entity
@Table(name = "program")
@Audited
@AttributeOverrides(AttributeOverride(name = "name", column = Column(name = "name", length = 100, nullable = true)))
open class Program : BaseChangeableOpenmrsMetadata {
    
    companion object {
        const val serialVersionUID = 3214567L
    }
    
    // ******************
    // Properties
    // ******************
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    open var programId: Int? = null
    
    @ManyToOne
    @JoinColumn(name = "concept_id", nullable = false)
    open var concept: Concept? = null
    
    /**
     * Represents the possible outcomes for this program. The concept should have answers or a
     * memberSet.
     */
    @ManyToOne
    @JoinColumn(name = "outcomes_concept_id")
    open var outcomesConcept: Concept? = null
    
    @AllowDirectAccess
    @OneToMany(mappedBy = "program", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dateCreated asc")
    open var allWorkflows: MutableSet<ProgramWorkflow> = mutableSetOf()
    
    // ******************
    // Constructors
    // ******************
    
    /** Default Constructor */
    constructor()
    
    /** Constructor with id */
    constructor(programId: Int?) {
        this.programId = programId
    }
    
    /**
     * Constructor with name
     *
     * @since 1.10
     */
    constructor(name: String?) {
        this.name = name
    }
    
    // ******************
    // Instance methods
    // ******************
    
    /**
     * Adds a new [ProgramWorkflow] to this Program
     *
     * @param workflow - the [ProgramWorkflow] to add
     */
    open fun addWorkflow(workflow: ProgramWorkflow) {
        workflow.program = this
        allWorkflows.add(workflow)
    }
    
    /**
     * Removes a [ProgramWorkflow] from this Program
     *
     * @param workflow - the [ProgramWorkflow] to remove
     */
    open fun removeWorkflow(workflow: ProgramWorkflow) {
        if (allWorkflows.contains(workflow)) {
            allWorkflows.remove(workflow)
            workflow.program = null
        }
    }
    
    /**
     * Retires a [ProgramWorkflow]
     *
     * @param workflow - the [ProgramWorkflow] to retire
     */
    open fun retireWorkflow(workflow: ProgramWorkflow) {
        workflow.retired = true
    }
    
    /**
     * Returns a [ProgramWorkflow] whose [Concept] has any [ConceptName] that matches
     * the given [name]
     *
     * @param name the [ProgramWorkflow] name, in any Locale
     * @return a [ProgramWorkflow] which has the passed [name] in any Locale
     */
    open fun getWorkflowByName(name: String?): ProgramWorkflow? {
        return allWorkflows.firstOrNull { it.concept?.isNamed(name) == true }
    }
    
    /** @see Object.toString */
    override fun toString(): String {
        return "Program(id=$programId, concept=$concept, workflows=$workflows)"
    }
    
    // ******************
    // Property Access
    // ******************
    
    /**
     * Get only the non-retired workflows
     *
     * @return Returns a Set<ProgramWorkflow> of all non-retired workflows
     */
    open val workflows: Set<ProgramWorkflow>
        get() = allWorkflows.filterNot { it.retired }.toSet()
    
    /**
     * Get the workflow with the specified ID
     *
     * @return the workflow matching the given id or null if none found
     * @since 1.6
     */
    open fun getWorkflow(programWorkflowId: Int?): ProgramWorkflow? {
        return workflows.firstOrNull { it.id == programWorkflowId }
    }
    
    /**
     * @since 1.5
     * @see org.openmrs.OpenmrsObject.getId
     */
    override var id: Int?
        get() = programId
        set(id) {
            programId = id
        }
}
