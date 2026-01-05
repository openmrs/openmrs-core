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
import jakarta.persistence.Transient
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.util.OpenmrsUtil
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.TreeSet

/**
 * A Person in the system. This can be either a small person stub, or indicative of an actual
 * Patient in the system. This class holds the generic person things that both the stubs and
 * patients share. Things like birthdate, names, addresses, and attributes are all generified into
 * the person table (and hence this super class)
 *
 * @see org.openmrs.Patient
 */
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
open class Person() : BaseChangeableOpenmrsData() {

    companion object {
        const val serialVersionUID: Long = 2L
        private val log = LoggerFactory.getLogger(Person::class.java)
    }

    @DocumentId
    private var _personId: Int? = null

    open fun getPersonId(): Int? = _personId
    open fun setPersonId(personId: Int?) { _personId = personId }

    private var _addresses: MutableSet<PersonAddress>? = null
    private var _names: MutableSet<PersonName>? = null
    private var _attributes: MutableSet<PersonAttribute>? = null

    @GenericField
    var gender: String? = null

    @GenericField
    var birthdate: Date? = null

    var birthtime: Date? = null

    var birthdateEstimated: Boolean? = false

    var deathdateEstimated: Boolean? = false

    @GenericField
    var dead: Boolean? = false

    var deathDate: Date? = null
        set(value) {
            field = value
            if (value != null) dead = true
        }

    var causeOfDeath: Concept? = null

    /** @since 2.2.0 */
    var causeOfDeathNonCoded: String? = null

    var personCreator: User? = null
        set(value) {
            field = value
            creator = value
        }

    var personDateCreated: Date? = null
        set(value) {
            field = value
            dateCreated = value
        }

    var personChangedBy: User? = null
        set(value) {
            field = value
            changedBy = value
        }

    var personDateChanged: Date? = null
        set(value) {
            field = value
            dateChanged = value
        }

    var personVoided: Boolean? = false
        set(value) {
            field = value
            voided = value
        }

    var personVoidedBy: User? = null
        set(value) {
            field = value
            voidedBy = value
        }

    var personDateVoided: Date? = null
        set(value) {
            field = value
            dateVoided = value
        }

    var personVoidReason: String? = null
        set(value) {
            field = value
            voidReason = value
        }

    @GenericField
    @NotAudited
    @IndexingDependency(derivedFrom = [ObjectPath(PropertyValue(propertyName = "patient"))])
    @get:JvmName("getIsPatient")
    private var _isPatient: Boolean = false

    @Transient
    private var attributeMap: MutableMap<String, PersonAttribute>? = null

    @Transient
    private var allAttributeMap: MutableMap<String, PersonAttribute>? = null

    /**
     * Copy constructor - builds a new Person object from another person object.
     * NOTE: All child collection objects are copied as pointers, each individual element is not copied.
     */
    constructor(person: Person?) : this() {
        if (person == null) return

        _personId = person._personId
        uuid = person.uuid
        _addresses = person._addresses
        _names = person._names
        _attributes = person._attributes

        gender = person.gender
        birthdate = person.birthdate
        birthtime = person.birthtime
        birthdateEstimated = person.birthdateEstimated
        deathdateEstimated = person.deathdateEstimated
        dead = person.dead
        deathDate = person.deathDate
        causeOfDeath = person.causeOfDeath
        causeOfDeathNonCoded = person.causeOfDeathNonCoded

        personCreator = person.personCreator
        personDateCreated = person.personDateCreated
        personChangedBy = person.personChangedBy
        personDateChanged = person.personDateChanged
        personVoided = person.personVoided
        personVoidedBy = person.personVoidedBy
        personDateVoided = person.personDateVoided
        personVoidReason = person.personVoidReason

        setPatient(person._isPatient)
    }

    /** Constructor with personId */
    constructor(personId: Int?) : this() {
        this._personId = personId
    }

    /**
     * @return true if person's birthdate is estimated
     * @deprecated as of 2.0, use [getBirthdateEstimated]
     */
    @Deprecated("Use getBirthdateEstimated() instead", ReplaceWith("birthdateEstimated"))
    @JsonIgnore
    fun isBirthdateEstimated(): Boolean? = birthdateEstimated

    /**
     * @return person's time of birth with the date portion set to the date from person's birthdate
     */
    val birthDateTime: Date?
        get() {
            if (birthdate != null && birthtime != null) {
                val birthDateString = SimpleDateFormat("yyyy-MM-dd").format(birthdate)
                val birthTimeString = SimpleDateFormat("HH:mm:ss").format(birthtime)
                return try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("$birthDateString $birthTimeString")
                } catch (e: Exception) {
                    log.error("Failed to parse birth date string", e)
                    null
                }
            }
            return null
        }

    /**
     * @return Returns the death status.
     * @deprecated as of 2.0, use [getDead]
     */
    @Deprecated("Use getDead() instead", ReplaceWith("dead"))
    @JsonIgnore
    fun isDead(): Boolean? = dead

    // Collection accessors

    var addresses: MutableSet<PersonAddress>
        get() {
            if (_addresses == null) _addresses = TreeSet()
            return _addresses!!
        }
        set(value) { _addresses = value }

    var names: MutableSet<PersonName>
        get() {
            if (_names == null) _names = TreeSet()
            return _names!!
        }
        set(value) { _names = value }

    var attributes: MutableSet<PersonAttribute>
        get() {
            if (_attributes == null) _attributes = TreeSet()
            return _attributes!!
        }
        set(value) {
            _attributes = value
            attributeMap = null
            allAttributeMap = null
        }

    /** Returns only the non-voided attributes for this person */
    val activeAttributes: List<PersonAttribute>
        get() = attributes.filterNot { it.voided == true }

    // Convenience methods for attributes

    /**
     * Adds an attribute to this person. Voids any current attribute with the same type.
     * NOTE: This effectively limits persons to only one attribute of any given type.
     */
    fun addAttribute(newAttribute: PersonAttribute) {
        newAttribute.person = this
        val newIsNull = !StringUtils.hasText(newAttribute.value)

        for (currentAttribute in activeAttributes) {
            if (currentAttribute == newAttribute) {
                return
            } else if (currentAttribute.attributeType == newAttribute.attributeType) {
                if (currentAttribute.value != null && currentAttribute.value == newAttribute.value) {
                    return
                }

                if (!newAttribute.voided!! || newIsNull) {
                    if (currentAttribute.creator != null) {
                        currentAttribute.voidAttribute("New value: ${newAttribute.value}")
                    } else {
                        removeAttribute(currentAttribute)
                    }
                }
            }
        }

        attributeMap = null
        allAttributeMap = null
        if (!OpenmrsUtil.collectionContains(attributes, newAttribute) && !newIsNull) {
            attributes.add(newAttribute)
        }
    }

    fun removeAttribute(attribute: PersonAttribute) {
        if (_attributes?.remove(attribute) == true) {
            attributeMap = null
            allAttributeMap = null
        }
    }

    /** Returns the first non-voided attribute matching the given type */
    fun getAttribute(pat: PersonAttributeType?): PersonAttribute? =
        pat?.let { type ->
            attributes.firstOrNull { it.attributeType == type && it.voided != true }
        }

    /** Returns the first non-voided attribute matching the given type name */
    fun getAttribute(attributeName: String?): PersonAttribute? =
        attributeName?.let { name ->
            attributes.firstOrNull { it.attributeType?.name == name && it.voided != true }
        }

    /** Returns the first non-voided attribute matching the given type id */
    fun getAttribute(attributeTypeId: Int): PersonAttribute? =
        activeAttributes.firstOrNull { it.attributeType?.personAttributeTypeId == attributeTypeId }

    /** Returns all non-voided attributes matching the given type name */
    fun getAttributes(attributeName: String): List<PersonAttribute> =
        activeAttributes.filter { it.attributeType?.name == attributeName }

    /** Returns all non-voided attributes matching the given type id */
    fun getAttributes(attributeTypeId: Int): List<PersonAttribute> =
        activeAttributes.filter { it.attributeType?.personAttributeTypeId == attributeTypeId }

    /** Returns all non-voided attributes matching the given type */
    fun getAttributes(personAttributeType: PersonAttributeType): List<PersonAttribute> =
        attributes.filter { it.attributeType == personAttributeType && it.voided != true }

    /** Returns this person's active attributes in map form */
    fun getAttributeMap(): Map<String, PersonAttribute> {
        if (attributeMap != null) return attributeMap!!

        log.debug("Current Person Attributes: \n{}", printAttributes())

        attributeMap = mutableMapOf()
        activeAttributes.forEach { attr ->
            attr.attributeType?.name?.let { name ->
                attributeMap!![name] = attr
            }
        }
        return attributeMap!!
    }

    /** Returns all attributes (including voided) in map form */
    fun getAllAttributeMap(): Map<String, PersonAttribute> {
        if (allAttributeMap != null) return allAttributeMap!!

        log.debug("Current Person Attributes: \n{}", printAttributes())

        allAttributeMap = mutableMapOf()
        attributes.forEach { attr ->
            attr.attributeType?.name?.let { name ->
                allAttributeMap!![name] = attr
            }
        }
        return allAttributeMap!!
    }

    /** Convenience method for viewing all of the person's current attributes */
    fun printAttributes(): String = buildString {
        attributes.forEach { attr ->
            append("${attr.attributeType} : ${attr.value} : voided? ${attr.voided}\n")
        }
    }

    // Convenience methods for names

    fun addName(name: PersonName?) {
        name?.apply {
            person = this@Person
            if (_names == null) _names = TreeSet()
            if (!OpenmrsUtil.collectionContains(_names, this)) {
                _names!!.add(this)
            }
        }
    }

    fun removeName(name: PersonName) {
        _names?.remove(name)
    }

    /**
     * Returns the preferred PersonName, or first non-voided name.
     * Returns null if no non-voided names exist (unless person is voided).
     */
    val personName: PersonName?
        get() {
            if (names.isEmpty()) return null
            return names.firstOrNull { it.preferred == true && it.voided != true }
                ?: names.firstOrNull { it.voided != true }
                ?: if (voided == true) names.first() else null
        }

    val givenName: String?
        get() = personName?.givenName ?: ""

    val middleName: String?
        get() = personName?.middleName ?: ""

    val familyName: String?
        get() = personName?.familyName ?: ""

    // Convenience methods for addresses

    fun addAddress(address: PersonAddress?) {
        address?.apply {
            person = this@Person
            if (_addresses == null) _addresses = TreeSet()
            if (!OpenmrsUtil.collectionContains(_addresses, this) && !isBlank) {
                _addresses!!.add(this)
            }
        }
    }

    fun removeAddress(address: PersonAddress) {
        _addresses?.remove(address)
    }

    /**
     * Returns the preferred PersonAddress, or first non-voided address.
     * Returns null if no non-voided addresses exist (unless person is voided).
     */
    val personAddress: PersonAddress?
        get() {
            if (addresses.isEmpty()) return null
            return addresses.firstOrNull { it.preferred == true && it.voided != true }
                ?: addresses.firstOrNull { it.voided != true }
                ?: if (voided == true) addresses.first() else null
        }

    // Age calculations

    /** Calculates this person's age based on birthdate */
    val age: Int?
        get() = getAge(null)

    /** Calculates this person's age on a given date */
    fun getAge(onDate: Date?): Int? {
        if (birthdate == null) return null

        val today = Calendar.getInstance().apply {
            if (onDate != null) time = onDate
            if (deathDate != null && time.after(deathDate)) time = deathDate
        }

        val bday = Calendar.getInstance().apply { time = birthdate }

        var age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR)

        val todaysMonth = today.get(Calendar.MONTH)
        val bdayMonth = bday.get(Calendar.MONTH)
        val todaysDay = today.get(Calendar.DAY_OF_MONTH)
        val bdayDay = bday.get(Calendar.DAY_OF_MONTH)

        if (todaysMonth < bdayMonth || (todaysMonth == bdayMonth && todaysDay < bdayDay)) {
            age--
        }

        return age
    }

    /** @since 2.7.0 */
    val ageInMonths: Int?
        get() = getAgeInChronoUnit(ChronoUnit.MONTHS)

    /** @since 2.7.0 */
    val ageInWeeks: Int?
        get() = getAgeInChronoUnit(ChronoUnit.WEEKS)

    /** @since 2.7.0 */
    val ageInDays: Int?
        get() = getAgeInChronoUnit(ChronoUnit.DAYS)

    private fun getAgeInChronoUnit(chronoUnit: ChronoUnit): Int? {
        if (birthdate == null) return null

        val birthDate = java.sql.Date(birthdate!!.time).toLocalDate()
        var endDate = LocalDate.now()

        deathDate?.let { death ->
            val deathLocalDate = java.sql.Date(death.time).toLocalDate()
            if (endDate.isAfter(deathLocalDate)) {
                endDate = deathLocalDate
            }
        }

        return when (chronoUnit) {
            ChronoUnit.DAYS -> ChronoUnit.DAYS.between(birthDate, endDate).toInt()
            ChronoUnit.WEEKS -> ChronoUnit.WEEKS.between(birthDate, endDate).toInt()
            ChronoUnit.MONTHS -> ChronoUnit.MONTHS.between(birthDate, endDate).toInt()
            else -> throw IllegalArgumentException("Unsupported ChronoUnit: $chronoUnit")
        }
    }

    /** Sets birthdate from age, setting it to January 1 of the calculated year */
    fun setBirthdateFromAge(age: Int, ageOnDate: Date?) {
        val c = Calendar.getInstance().apply {
            time = ageOnDate ?: Date()
            set(Calendar.DATE, 1)
            set(Calendar.MONTH, Calendar.JANUARY)
            add(Calendar.YEAR, -age)
        }
        birthdate = c.time
        birthdateEstimated = true
    }

    /**
     * @deprecated as of 2.0, use [personVoided]
     */
    @Deprecated("Use personVoided instead", ReplaceWith("personVoided"))
    @JsonIgnore
    fun isPersonVoided(): Boolean? = personVoided

    /**
     * @deprecated as of 2.0, use [getIsPatient]
     */
    @Deprecated("Use getIsPatient() instead", ReplaceWith("getIsPatient()"))
    @JsonIgnore
    @NotAudited
    fun isPatient(): Boolean = _isPatient

    @NotAudited
    fun getIsPatient(): Boolean = _isPatient

    /** This should only be set by the database layer */
    protected fun setPatient(isPatient: Boolean) {
        this._isPatient = isPatient
    }

    override fun toString(): String = "Person(personId=$_personId)"

    override var id: Integer?
        get() = _personId?.let { Integer(it) }
        set(value) { _personId = value?.toInt() }
}
