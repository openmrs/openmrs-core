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

import jakarta.persistence.Cacheable
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.openmrs.util.RoleConstants
import org.slf4j.LoggerFactory

/**
 * A Role is just an aggregater of [Privilege]s. [User]s contain a number of roles
 * (Users DO NOT contain any privileges directly) Roles can be grouped by inheriting other roles. If
 * a user is given Role A that inherits from Role B, the user has all rights/abilities for both Role
 * A's privileges and for Role B's privileges.
 *
 * @see Privilege
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
open class Role : BaseChangeableOpenmrsMetadata {
    
    open var role: String? = null
    
    open var privileges: MutableSet<Privilege>? = null
    
    open var inheritedRoles: MutableSet<Role>? = null
        get() = field ?: mutableSetOf<Role>().also { field = it }
    
    open var childRoles: MutableSet<Role>? = null
        get() = field ?: mutableSetOf<Role>().also { field = it }
    
    /** default constructor */
    constructor()
    
    /** constructor with id */
    constructor(role: String?) {
        this.role = role
    }
    
    /** constructor with all database required properties */
    constructor(role: String?, description: String?) {
        this.role = role
        this.description = description
    }
    
    override fun getName(): String? = role
    
    /**
     * Adds the given Privilege to the list of privileges
     *
     * @param privilege Privilege to add
     */
    fun addPrivilege(privilege: Privilege?) {
        if (privilege != null) {
            if (privileges == null) {
                privileges = mutableSetOf()
            }
            if (!containsPrivilege(privileges!!, privilege.privilege)) {
                privileges!!.add(privilege)
            }
        }
    }
    
    private fun containsPrivilege(privileges: Collection<Privilege>, privilegeName: String?): Boolean =
        privileges.any { it.privilege == privilegeName }
    
    /**
     * Removes the given Privilege from the list of privileges
     *
     * @param privilege Privilege to remove
     */
    fun removePrivilege(privilege: Privilege?) {
        privileges?.remove(privilege)
    }
    
    override fun toString(): String = role ?: ""
    
    /**
     * Looks for the given [privilegeName] privilege name in this roles privileges. This
     * method does not recurse through the inherited roles
     *
     * @param privilegeName String name of a privilege
     * @return true/false whether this role has the given privilege
     * <strong>Should</strong> return false if not found
     * <strong>Should</strong> return true if found
     * <strong>Should</strong> not fail given null parameter
     * <strong>Should</strong> return true for any privilegeName if super user
     */
    fun hasPrivilege(privilegeName: String?): Boolean {
        if (RoleConstants.SUPERUSER == role) {
            return true
        }
        
        return privileges?.any { 
            it.privilege.equals(privilegeName, ignoreCase = true) 
        } ?: false
    }
    
    /**
     * Convenience method to test whether or not this role extends/ inherits from any other roles
     *
     * @return true/false whether this role inherits from other roles
     */
    fun inheritsRoles(): Boolean = !inheritedRoles.isNullOrEmpty()
    
    /**
     * Recursive (if need be) method to return all parent roles of this role
     *
     * <strong>Should</strong> only return parent roles
     * @return Return this role's parents
     */
    fun getAllParentRoles(): Set<Role> {
        val parents = mutableSetOf<Role>()
        if (inheritsRoles()) {
            parents.addAll(recurseOverParents(parents))
        }
        return parents
    }
    
    /**
     * Returns the full set of roles be looping over inherited roles. Duplicate roles are dropped.
     *
     * @param total Roles already looped over
     * @return Set<Role> Current and inherited roles
     */
    fun recurseOverParents(total: Set<Role>): Set<Role> {
        if (!inheritsRoles()) {
            return total
        }
        
        val allRoles = total.toMutableSet()
        val myRoles = inheritedRoles!!.toMutableSet()
        myRoles.removeAll(total)
        // prevent an obvious looping problem
        myRoles.remove(this)
        allRoles.addAll(myRoles)
        
        for (r in myRoles) {
            if (r.inheritsRoles()) {
                allRoles.addAll(r.recurseOverParents(allRoles))
            }
        }
        
        log.debug("Total roles: {}", allRoles)
        
        return allRoles
    }
    
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
    
    /**
     * Convenience method to test whether or not this role is a parent of another role
     *
     * @return true/false whether this role is a parent of another role
     * @since 1.9
     */
    fun hasChildRoles(): Boolean = !childRoles.isNullOrEmpty()
    
    /**
     * Recursive (if need be) method to return all child roles of this role
     *
     * <strong>Should</strong> only return child roles
     * @return this role's children
     * @since 1.9
     */
    fun getAllChildRoles(): Set<Role> {
        val children = mutableSetOf<Role>()
        if (hasChildRoles()) {
            children.addAll(recurseOverChildren(children))
        }
        return children
    }
    
    /**
     * Returns the full set of child roles be looping over children. Duplicate roles are dropped.
     *
     * @param total Roles already looped over
     * @return Set<Role> Current and child roles
     * @since 1.9
     */
    fun recurseOverChildren(total: Set<Role>): Set<Role> {
        if (!hasChildRoles()) {
            return total
        }
        
        val allRoles = total.toMutableSet()
        val myRoles = childRoles!!.toMutableSet()
        myRoles.removeAll(total)
        // prevent an obvious looping problem
        myRoles.remove(this)
        allRoles.addAll(myRoles)
        
        for (r in myRoles) {
            if (r.hasChildRoles()) {
                allRoles.addAll(r.recurseOverChildren(allRoles))
            }
        }
        
        log.debug("Total roles: {}", allRoles)
        
        return allRoles
    }
    
    companion object {
        const val serialVersionUID = 1234233L
        
        private val log = LoggerFactory.getLogger(Role::class.java)
    }
}
