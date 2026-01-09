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
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.openmrs.api.context.Context
import org.openmrs.util.LocaleUtility
import org.openmrs.util.OpenmrsConstants
import org.openmrs.util.OpenmrsUtil
import org.openmrs.util.RoleConstants
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.Date
import java.util.Locale

/**
 * Defines a User Account in the system. This account belongs to a [Person] in the system,
 * although that person may have other user accounts. Users have login credentials
 * (username/password) and can have special user properties. User properties are just simple
 * key-value pairs for either quick info or display specific info that needs to be persisted (like
 * locale preferences, search options, etc)
 */
@Entity
@Table(name = "users")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
class User() : BaseOpenmrsObject(), Serializable, Attributable<User>, Auditable, Retireable {

    companion object {
        const val serialVersionUID: Long = 2L
        private val log = LoggerFactory.getLogger(User::class.java)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_user_id_seq")
    @GenericGenerator(
        name = "users_user_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "users_user_id_seq")]
    )
    @Column(name = "user_id")
    var userId: Int? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Cascade(CascadeType.SAVE_UPDATE)
    var person: Person? = null

    @Column(name = "system_id", nullable = false, length = 50)
    var systemId: String? = null

    @Column(name = "username", length = 50)
    var username: String? = null

    /** @since 2.2 */
    @Column(name = "email", length = 255, unique = true)
    var email: String? = null

    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role")]
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DETACH)
    var roles: MutableSet<Role>? = null

    @ElementCollection
    @CollectionTable(name = "user_property", joinColumns = [JoinColumn(name = "user_id", nullable = false)])
    @MapKeyColumn(name = "property", length = 255)
    @Column(name = "property_value", length = Int.MAX_VALUE)
    @Cascade(CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DETACH)
    @NotAudited
    private var _userProperties: MutableMap<String, String>? = null

    @Transient
    private var proficientLocales: MutableList<Locale>? = null

    @Transient
    private var parsedProficientLocalesProperty: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", nullable = false)
    override var creator: User? = null

    @Column(name = "date_created", nullable = false, length = 19)
    override var dateCreated: Date? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    override var changedBy: User? = null

    @Column(name = "date_changed", length = 19)
    override var dateChanged: Date? = null

    @Column(name = "retired", nullable = false, length = 1)
    private var _retired: Boolean = false

    override var retired: Boolean?
        get() = _retired
        set(value) { _retired = value ?: false }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retired_by")
    override var retiredBy: User? = null

    @Column(name = "date_retired", length = 19)
    override var dateRetired: Date? = null

    @Column(name = "retire_reason", length = 255)
    override var retireReason: String? = null

    /** Constructor with id */
    constructor(userId: Int?) : this() {
        this.userId = userId
    }

    /** Constructor with person object */
    constructor(person: Person?) : this() {
        this.person = person
    }

    override var id: Integer?
        get() = userId?.let { Integer(it) }
        set(value) { userId = value?.toInt() }

    /**
     * Return true if this user has all privileges.
     */
    val isSuperUser: Boolean
        get() = containsRole(RoleConstants.SUPERUSER)

    /**
     * This method shouldn't be used directly. Use org.openmrs.api.context.Context#hasPrivilege so that
     * anonymous/authenticated/proxy privileges are all included.
     *
     * @param privilege the privilege to check
     * @return true if this user has the specified privilege
     */
    fun hasPrivilege(privilege: String?): Boolean {
        if (privilege.isNullOrEmpty()) return true
        if (isSuperUser) return true
        return allRoles.any { it.hasPrivilege(privilege) }
    }

    /**
     * Check if this user has the given String role.
     *
     * @param r String name of a role to check
     * @return true if this user has the specified role
     */
    fun hasRole(r: String): Boolean = hasRole(r, ignoreSuperUser = false)

    /**
     * Checks if this user has the given String role.
     *
     * @param r String name of a role to check
     * @param ignoreSuperUser If false, this method will always return true for a superuser.
     * @return true if the user has the given role, or if ignoreSuperUser is false and the user is a superUser
     */
    fun hasRole(r: String, ignoreSuperUser: Boolean): Boolean {
        if (!ignoreSuperUser && isSuperUser) return true
        if (roles == null) return false
        log.debug("User # {} has roles: {}", userId, allRoles)
        return containsRole(r)
    }

    /**
     * Checks if the user has a given role. Role name comparisons are not case sensitive.
     *
     * @param roleName the name of the role to check
     * @return true if the user has the given role
     */
    fun containsRole(roleName: String): Boolean =
        allRoles.any { it.role.equals(roleName, ignoreCase = true) }

    /**
     * Get all privileges this user has. This delves into all of the roles that a person has,
     * appending unique privileges.
     */
    val privileges: Collection<Privilege>
        get() = allRoles.flatMap { it.privileges ?: emptySet() }.toSet()

    /**
     * Returns all roles attributed to this user by expanding the role list to include the parents of
     * the assigned roles.
     */
    val allRoles: Set<Role>
        get() {
            val baseRoles = roles?.toMutableSet() ?: mutableSetOf()
            val totalRoles = baseRoles.toMutableSet()

            log.debug("User's base roles: {}", baseRoles)

            try {
                baseRoles.forEach { role ->
                    totalRoles.addAll(role.allParentRoles)
                }
            } catch (e: ClassCastException) {
                log.error("Error converting roles for user: $this")
                log.error("baseRoles.class: ${baseRoles.javaClass.name}")
                log.error("baseRoles: $baseRoles")
                baseRoles.forEach { log.error("baseRole: '$it'") }
            }
            return totalRoles
        }

    /**
     * Add the given Role to the list of roles for this User.
     *
     * @param role the role to add
     * @return this user with the given role attached
     */
    fun addRole(role: Role?): User {
        if (role == null) return this
        if (roles == null) roles = mutableSetOf()
        if (role !in roles!!) roles!!.add(role)
        return this
    }

    /**
     * Remove the given Role from the list of roles for this User.
     *
     * @param role the role to remove
     * @return this user with the given role removed
     */
    fun removeRole(role: Role): User {
        roles?.remove(role)
        return this
    }

    // Attributable implementation

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun findPossibleValues(searchText: String): List<User> =
        runCatching { Context.getUserService().getUsersByName(searchText, "", false) }
            .getOrDefault(emptyList())

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun getPossibleValues(): List<User> =
        runCatching { Context.getUserService().getAllUsers() }
            .getOrDefault(emptyList())

    override fun hydrate(s: String): User =
        runCatching { Context.getUserService().getUser(s.toInt()) ?: User() }
            .getOrDefault(User())

    override fun serialize(): String = userId?.toString() ?: ""

    override val displayString: String
        get() = buildString {
            personName?.fullName?.let { append("$it ") }
            append("($username)")
        }

    override fun toString(): String =
        if (username.isNullOrBlank()) systemId ?: "" else username!!

    // User properties

    var userProperties: MutableMap<String, String>
        get() {
            if (_userProperties == null) _userProperties = mutableMapOf()
            return _userProperties!!
        }
        set(value) { _userProperties = value }

    fun setUserProperty(prop: String, value: String) {
        userProperties[prop] = value
    }

    fun removeUserProperty(prop: String) {
        _userProperties?.remove(prop)
    }

    fun getUserProperty(prop: String): String = userProperties[prop] ?: ""

    fun getUserProperty(prop: String, defaultValue: String): String =
        userProperties[prop] ?: defaultValue

    // Person delegation

    private val personMaybeCreate: Person
        get() {
            if (person == null) person = Person()
            return person!!
        }

    fun addName(name: PersonName) {
        personMaybeCreate.addName(name)
    }

    val personName: PersonName?
        get() = person?.personName

    val givenName: String?
        get() = person?.givenName

    val familyName: String?
        get() = person?.familyName

    val names: Set<PersonName>
        get() = person?.names ?: emptySet()

    /**
     * Returns a list of Locales for which the User is considered proficient.
     */
    fun getProficientLocales(): List<Locale> {
        val proficientLocalesProperty = getUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES)

        if (proficientLocales == null ||
            !OpenmrsUtil.nullSafeEquals(parsedProficientLocalesProperty, proficientLocalesProperty)) {

            parsedProficientLocalesProperty = proficientLocalesProperty
            proficientLocales = mutableListOf()

            proficientLocalesProperty.split(",")
                .filter { it.isNotEmpty() }
                .forEach { localeSpec ->
                    val proficientLocale = LocaleUtility.fromSpecification(localeSpec)
                    if (proficientLocale !in proficientLocales!!) {
                        proficientLocales!!.add(proficientLocale)
                        if (proficientLocale.country.isNotEmpty()) {
                            val languageOnlyLocale = LocaleUtility.fromSpecification(proficientLocale.language)
                            if (languageOnlyLocale !in proficientLocales!!) {
                                proficientLocales!!.add(languageOnlyLocale)
                            }
                        }
                    }
                }
        }
        return proficientLocales!!.toList()
    }
}
