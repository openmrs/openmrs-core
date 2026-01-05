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

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cascade
import org.hibernate.envers.Audited
import org.openmrs.annotation.AllowDirectAccess
import org.openmrs.annotation.DisableHandlers
import org.openmrs.api.context.Context
import org.openmrs.api.handler.VoidHandler
import java.util.ArrayDeque
import java.util.Date
import java.util.LinkedHashSet

/**
 * An Encounter represents one visit or interaction of a patient with a healthcare worker.
 * Every encounter can have 0 to n Observations and Orders associated with it.
 *
 * @see Obs
 * @see Order
 */
@Entity
@Table(name = "encounter")
@BatchSize(size = 25)
@Audited
class Encounter() : BaseChangeableOpenmrsData() {

    companion object {
        const val serialVersionUID: Long = 2L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "encounter_id")
    var encounterId: Int? = null

    @Column(name = "encounter_datetime", nullable = false, length = 19)
    var encounterDatetime: Date? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    var patient: Patient? = null

    @ManyToOne
    @JoinColumn(name = "location_id")
    var location: Location? = null

    @ManyToOne
    @JoinColumn(name = "form_id")
    var form: Form? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "encounter_type")
    var encounterType: EncounterType? = null

    @OneToMany(mappedBy = "encounter")
    private var _orders: MutableSet<Order>? = null

    @OneToMany(mappedBy = "encounter")
    private var _diagnoses: MutableSet<Diagnosis>? = null

    @OneToMany(mappedBy = "encounter")
    private var _conditions: MutableSet<Condition>? = null

    @OneToMany(mappedBy = "encounter")
    @Access(AccessType.FIELD)
    @OrderBy("concept_id")
    @BatchSize(size = 25)
    @AllowDirectAccess
    private var _obs: MutableSet<Obs>? = null

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "visit_id")
    var visit: Visit? = null

    @OneToMany(mappedBy = "encounter", cascade = [CascadeType.ALL])
    @OrderBy("provider_id")
    @DisableHandlers(handlerTypes = [VoidHandler::class])
    var encounterProviders: MutableSet<EncounterProvider> = LinkedHashSet()

    @OneToMany(mappedBy = "encounter")
    private var _allergies: MutableSet<Allergy>? = null

    constructor(encounterId: Int?) : this() {
        this.encounterId = encounterId
    }

    // Orders

    var orders: MutableSet<Order>
        get() {
            if (_orders == null) _orders = LinkedHashSet()
            return _orders!!
        }
        set(value) { _orders = value }

    fun addOrder(order: Order?) {
        order?.apply {
            encounter = this@Encounter
            orders.add(this)
        }
    }

    fun removeOrder(order: Order?) {
        _orders?.remove(order)
    }

    val orderGroups: List<OrderGroup>
        get() {
            val groups = mutableMapOf<String, OrderGroup>()
            _orders?.forEach { order ->
                order.orderGroup?.let { og ->
                    groups.getOrPut(og.uuid) { og }
                    og.addOrder(order, null)
                }
            }
            return groups.values.toList()
        }

    val ordersWithoutOrderGroups: List<Order>
        get() = orders.filter { it.orderGroup == null }

    // Obs

    var obs: MutableSet<Obs>
        get() {
            val ret = LinkedHashSet<Obs>()
            _obs?.forEach { o -> ret.addAll(getObsLeaves(o)) }
            return ret
        }
        set(value) { _obs = value }

    private fun getObsLeaves(obsParent: Obs): List<Obs> {
        val leaves = mutableListOf<Obs>()
        if (obsParent.hasGroupMembers()) {
            for (child in obsParent.groupMembers ?: emptySet()) {
                if (child.voided != true) {
                    if (!child.isObsGrouping) {
                        leaves.add(child)
                    } else {
                        leaves.addAll(getObsLeaves(child))
                    }
                }
            }
        } else if (obsParent.voided != true) {
            leaves.add(obsParent)
        }
        return leaves
    }

    private fun getFlattenedObsLeaves(obsParent: Obs, includeVoided: Boolean): Set<Obs> {
        val leaves = LinkedHashSet<Obs>()
        if (includeVoided || obsParent.voided != true) {
            leaves.add(obsParent)
            if (obsParent.hasGroupMembers()) {
                for (child in obsParent.getGroupMembers(includeVoided) ?: emptySet()) {
                    leaves.addAll(getFlattenedObsLeaves(child, includeVoided))
                }
            }
        }
        return leaves
    }

    fun getAllObs(includeVoided: Boolean = false): Set<Obs> {
        if (includeVoided && _obs != null) return _obs!!
        return _obs?.filter { includeVoided || it.voided != true }?.toSet() ?: emptySet()
    }

    fun getAllFlattenedObs(includeVoided: Boolean): Set<Obs> {
        val ret = LinkedHashSet<Obs>()
        _obs?.forEach { o ->
            if (includeVoided || o.voided != true) {
                ret.addAll(getFlattenedObsLeaves(o, includeVoided))
            }
        }
        return ret
    }

    fun getObsAtTopLevel(includeVoided: Boolean): Set<Obs> =
        getAllObs(includeVoided).filter { it.obsGroup == null }.toCollection(LinkedHashSet())

    fun addObs(observation: Obs?) {
        if (_obs == null) _obs = LinkedHashSet()
        observation ?: return

        _obs!!.add(observation)

        val obsToUpdate = ArrayDeque<Obs>()
        obsToUpdate.add(observation)
        val seenIt = LinkedHashSet<Obs>()

        while (obsToUpdate.isNotEmpty()) {
            val o = obsToUpdate.removeFirst()
            if (o in seenIt) continue
            seenIt.add(o)

            o.encounter = this
            if (o.obsDatetime == null) o.obsDatetime = encounterDatetime
            if (o.person == null) o.person = patient
            if (o.location == null) o.location = location

            o.getGroupMembers(true)?.let { obsToUpdate.addAll(it) }
        }
    }

    fun removeObs(observation: Obs?) {
        _obs?.remove(observation)
    }

    // Diagnoses

    var diagnoses: MutableSet<Diagnosis>
        get() {
            if (_diagnoses == null) _diagnoses = LinkedHashSet()
            return _diagnoses!!
        }
        set(value) { _diagnoses = value }

    fun hasDiagnosis(diagnosis: Diagnosis): Boolean = diagnosis in diagnoses

    // Conditions

    fun getConditions(includeVoided: Boolean = false): Set<Condition> =
        (_conditions ?: LinkedHashSet()).filter { includeVoided || it.voided != true }.toSet()

    var conditions: MutableSet<Condition>?
        get() = _conditions
        set(value) { _conditions = value }

    fun addCondition(condition: Condition?) {
        if (_conditions == null) _conditions = LinkedHashSet()
        condition?.apply {
            encounter = this@Encounter
            _conditions!!.add(this)
        }
    }

    fun removeCondition(condition: Condition) {
        (_conditions ?: LinkedHashSet())
            .filter { it.voided != true && it == condition }
            .forEach {
                it.voided = true
                it.dateVoided = Date()
                it.voidReason = "Voided by the API"
                it.voidedBy = Context.getAuthenticatedUser()
            }
    }

    // Allergies

    fun getAllergies(includeVoided: Boolean = false): Set<Allergy> =
        (_allergies ?: LinkedHashSet()).filter { includeVoided || it.voided != true }.toSet()

    var allergies: MutableSet<Allergy>?
        get() = _allergies
        set(value) { _allergies = value }

    fun addAllergy(allergy: Allergy?) {
        if (_allergies == null) _allergies = LinkedHashSet()
        allergy?.apply {
            encounter = this@Encounter
            _allergies!!.add(this)
        }
    }

    fun removeAllergy(allergy: Allergy) {
        (_allergies ?: LinkedHashSet())
            .filter { it.voided != true && it == allergy }
            .forEach {
                it.voided = true
                it.dateVoided = Date()
                it.voidReason = "Voided by the API"
                it.voidedBy = Context.getAuthenticatedUser()
            }
    }

    // Encounter Providers

    val activeEncounterProviders: Set<EncounterProvider>
        get() = encounterProviders.filterNot { it.voided == true }.toSet()

    fun getProvidersByRoles(includeVoided: Boolean = false): Map<EncounterRole, Set<Provider>> =
        encounterProviders
            .filter { includeVoided || it.voided != true }
            .groupBy({ it.encounterRole!! }, { it.provider!! })
            .mapValues { it.value.toSet() }

    fun getProvidersByRole(role: EncounterRole, includeVoided: Boolean = false): Set<Provider> =
        encounterProviders
            .filter { it.encounterRole == role && (includeVoided || it.voided != true) }
            .mapNotNull { it.provider }
            .toSet()

    fun addProvider(role: EncounterRole, provider: Provider) {
        // Check if provider already exists
        if (encounterProviders.any { it.encounterRole == role && it.provider == provider && it.voided != true }) {
            return
        }

        val ep = EncounterProvider().apply {
            encounter = this@Encounter
            encounterRole = role
            this.provider = provider
            dateCreated = Date()
            creator = Context.getAuthenticatedUser()
        }
        encounterProviders.add(ep)
    }

    fun setProvider(role: EncounterRole, provider: Provider) {
        var hasProvider = false
        for (ep in encounterProviders) {
            if (ep.encounterRole == role) {
                if (ep.provider != provider) {
                    ep.voided = true
                    ep.dateVoided = Date()
                    ep.voidedBy = Context.getAuthenticatedUser()
                } else if (ep.voided != true) {
                    hasProvider = true
                }
            }
        }

        if (!hasProvider) {
            addProvider(role, provider)
        }
    }

    fun removeProvider(role: EncounterRole, provider: Provider) {
        for (ep in encounterProviders) {
            if (ep.encounterRole == role && ep.provider == provider && ep.voided != true) {
                ep.voided = true
                ep.dateVoided = Date()
                ep.voidedBy = Context.getAuthenticatedUser()
                return
            }
        }
    }

    // Copy method

    fun copyAndAssignToAnotherPatient(patient: Patient): Encounter {
        val target = Encounter().apply {
            changedBy = this@Encounter.changedBy
            creator = this@Encounter.creator
            dateChanged = this@Encounter.dateChanged
            dateCreated = this@Encounter.dateCreated
            dateVoided = this@Encounter.dateVoided
            voided = this@Encounter.voided
            voidedBy = this@Encounter.voidedBy
            voidReason = this@Encounter.voidReason
            encounterDatetime = this@Encounter.encounterDatetime
            encounterType = this@Encounter.encounterType
            form = this@Encounter.form
            location = this@Encounter.location
            this.patient = patient
        }

        for (ep in encounterProviders) {
            val copy = ep.copy()
            copy.encounter = target
            target.encounterProviders.add(copy)
        }

        Context.getEncounterService().saveEncounter(target)

        for (obs in getAllObs()) {
            val obsCopy = Obs.newInstance(obs)
            obsCopy.encounter = target
            obsCopy.person = patient
            target.addObs(obsCopy)
        }

        return target
    }

    override fun toString(): String = buildString {
        append("Encounter: [")
        append(encounterId?.toString() ?: "(no ID)")
        append(" ")
        append(encounterDatetime?.toString() ?: "(no Date)")
        append(" ")
        append(encounterType?.name ?: "(no Type)")
        append(" ")
        append(location?.name ?: "(no Location)")
        append(" ")
        append(patient?.getPatientId()?.toString() ?: "(no Patient)")
        append(" ")
        append(form?.name ?: "(no Form)")
        append(" ")
        append("num Obs: ${getObsAtTopLevel(false).size} ")
        append("num Orders: ${orders.size}")
        append("]")
    }

    override var id: Integer?
        get() = encounterId?.let { Integer(it) }
        set(value) { encounterId = value?.toInt() }
}
