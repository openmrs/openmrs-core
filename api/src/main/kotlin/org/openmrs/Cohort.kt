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
import java.util.Date
import java.util.TreeSet

/**
 * This class represents a list of patientIds.
 */
@Audited
class Cohort() : BaseChangeableOpenmrsData() {
    
    var cohortId: Int? = null
    
    var name: String? = null
    
    var description: String? = null
    
    private var memberships: MutableCollection<CohortMembership> = TreeSet()
    
    /**
     * Convenience constructor to create a Cohort object that has an primarykey/internal identifier
     * of [cohortId]
     *
     * @param cohortId the internal identifier for this cohort
     */
    constructor(cohortId: Int?) : this() {
        this.cohortId = cohortId
    }
    
    /**
     * This constructor does not check whether the database contains patients with the given ids,
     * but [org.openmrs.api.CohortService.saveCohort] will.
     * 
     * @param name
     * @param description optional description
     * @param ids option array of Integer ids
     */
    constructor(name: String?, description: String?, ids: Array<Int>?) : this() {
        this.name = name
        this.description = description
        ids?.forEach { addMember(it) }
    }
    
    /**
     * This constructor does not check whether the database contains patients with the given ids,
     * but [org.openmrs.api.CohortService.saveCohort] will.
     * 
     * @param name
     * @param description optional description
     * @param patients optional array of patients
     */
    constructor(name: String?, description: String?, patients: Array<Patient>?) : this(name, description, null as Array<Int>?) {
        patients?.forEach { addMembership(CohortMembership(it.patientId)) }
    }
    
    /**
     * This constructor does not check whether the database contains patients with the given ids,
     * but [org.openmrs.api.CohortService.saveCohort] will.
     * 
     * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
     *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
     */
    constructor(patientsOrIds: Collection<*>?) : this(null, null, patientsOrIds)
    
    /**
     * This constructor does not check whether the database contains patients with the given ids,
     * but [org.openmrs.api.CohortService.saveCohort] will.
     * 
     * @param name
     * @param description optional description
     * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
     *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
     */
    constructor(name: String?, description: String?, patientsOrIds: Collection<*>?) : this(name, description, null as Array<Int>?) {
        patientsOrIds?.forEach { o ->
            when (o) {
                is Patient -> addMembership(CohortMembership(o.patientId))
                is Int -> addMembership(CohortMembership(o))
            }
        }
    }
    
    /**
     * Convenience constructor taking in a string that is a list of comma separated patient ids This
     * constructor does not check whether the database contains patients with the given ids, but
     * [org.openmrs.api.CohortService.saveCohort] will.
     * 
     * @param commaSeparatedIds
     */
    constructor(commaSeparatedIds: String) : this() {
        commaSeparatedIds.split(',')
            .map { it.trim() }
            .forEach { addMembership(CohortMembership(it.toInt())) }
    }
    
    /**
     * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
     * @return Returns a comma-separated list of patient ids in the cohort.
     */
    @Deprecated("Use getMemberships() instead", ReplaceWith("getMemberships()"))
    fun getCommaSeparatedPatientIds(): String = memberIds.joinToString(",")
    
    fun contains(patientId: Int?): Boolean =
        memberships.any { it.patientId == patientId && !it.voided }
    
    override fun toString(): String = buildString {
        append("Cohort id=").append(cohortId)
        name?.let { append(" name=").append(it) }
        append(" size=").append(memberships.size)
    }
    
    fun addMember(memberId: Int?) {
        addMembership(CohortMembership(memberId))
    }
    
    /**
     * @since 2.1.0
     */
    fun addMembership(cohortMembership: CohortMembership?): Boolean {
        cohortMembership?.let {
            it.cohort = this
            return memberships.add(it)
        }
        return false
    }
    
    /**
     * @since 2.1.0
     */
    fun removeMembership(cohortMembership: CohortMembership?): Boolean = memberships.remove(cohortMembership)
    
    /**
     * @since 2.1.0
     * @param includeVoided boolean true/false to include/exclude voided memberships
     * @return Collection of cohort memberships
     */
    fun getMemberships(includeVoided: Boolean): Collection<CohortMembership> =
        if (includeVoided) {
            memberships
        } else {
            memberships.filter { it.voided == includeVoided }
        }
    
    /**
     * @since 2.1.0
     */
    fun getMemberships(): MutableCollection<CohortMembership> = memberships
    
    /**
     * @since 2.1.0
     * @param asOfDate date used to return active memberships
     * @return Collection of cohort memberships
     */
    fun getActiveMemberships(asOfDate: Date?): Collection<CohortMembership> =
        memberships.filter { it.isActive(asOfDate) }
    
    fun getActiveMemberships(): Collection<CohortMembership> = getActiveMemberships(Date())
    
    /**
     * @since 2.1.0
     */
    fun getActiveMembership(patient: Patient): CohortMembership =
        memberships.first { it.isActive() && it.patientId == patient.patientId }
    
    fun size(): Int = memberships.count { !it.voided }
    
    /**
     * @deprecated use [size]
     */
    @Deprecated("Use size() instead", ReplaceWith("size()"))
    fun getSize(): Int = size()
    
    fun isEmpty(): Boolean = size() == 0
    
    /**
     * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
     */
    @Deprecated("Use getMemberships() instead")
    val memberIds: Set<Int>
        get() = memberships.mapTo(TreeSet()) { it.patientId!! }
    
    /**
     * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
     * @param memberIds
     */
    @Deprecated("Use setMemberships() instead")
    fun setMemberIds(memberIds: Set<Int>) {
        if (memberships.isEmpty()) {
            memberIds.forEach { addMembership(CohortMembership(it)) }
        } else {
            throw IllegalArgumentException("since 2.1.0 cohorts are more complex than just a set of patient ids")
        }
    }
    
    fun setMemberships(members: MutableCollection<CohortMembership>) {
        this.memberships = members
    }
    
    override var id: Int?
        get() = cohortId
        set(value) {
            cohortId = value
        }
    
    /**
     * @since 2.3
     * 
     * This function checks if there exists any active CohortMembership for a given patientId
     * 
     * @param patientId is the patientid that should be checked for activity in cohort
     * @return true if cohort has active membership for the requested patient             
     */
    fun hasActiveMembership(patientId: Int): Boolean =
        memberships.any { it.patientId == patientId && it.isActive() }
    
    /**
     * 
     * @since  2.3
     * This method returns the number of active members in the cohort
     * 
     * @return  number of active memberships in the cohort
     */
    fun activeMembershipSize(): Int = getActiveMemberships().size
    
    /**
     *
     * @since  2.3
     * This method returns true if cohort has no active memberships
     *
     * @return true if no active cohort exists
     */
    fun hasNoActiveMemberships(): Boolean = getActiveMemberships().isEmpty()
    
    companion object {
        const val serialVersionUID = 0L
        
        /**
         * Returns the union of two cohorts
         *
         * @param a The first Cohort
         * @param b The second Cohort
         * @return Cohort
         */
        @JvmStatic
        fun union(a: Cohort?, b: Cohort?): Cohort {
            val ret = Cohort()
            a?.let { ret.memberships.addAll(it.memberships) }
            b?.let { ret.memberships.addAll(it.memberships) }
            if (a != null && b != null) {
                ret.name = "(${a.name} + ${b.name})"
            }
            return ret
        }
        
        /**
         * Returns the intersection of two cohorts, treating null as an empty cohort
         *
         * @param a The first Cohort
         * @param b The second Cohort
         * @return Cohort
         */
        @JvmStatic
        fun intersect(a: Cohort?, b: Cohort?): Cohort {
            val ret = Cohort()
            ret.name = "(${a?.name ?: "NULL"} * ${b?.name ?: "NULL"})"
            if (a != null && b != null) {
                ret.memberships.addAll(a.memberships)
                ret.memberships.retainAll(b.memberships.toSet())
            }
            return ret
        }
        
        /**
         * Subtracts a cohort from a cohort
         *
         * @param a the original Cohort
         * @param b the Cohort to subtract
         * @return Cohort
         */
        @JvmStatic
        fun subtract(a: Cohort?, b: Cohort?): Cohort {
            val ret = Cohort()
            a?.let {
                ret.memberships.addAll(it.memberships)
                b?.let { bCohort ->
                    ret.memberships.removeAll(bCohort.memberships.toSet())
                    ret.name = "(${it.name} - ${bCohort.name})"
                }
            }
            return ret
        }
    }
}
