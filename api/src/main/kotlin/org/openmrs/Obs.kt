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

import org.apache.commons.lang3.StringUtils
import org.hibernate.envers.Audited
import org.openmrs.annotation.AllowDirectAccess
import org.openmrs.api.APIException
import org.openmrs.api.context.Context
import org.openmrs.api.db.hibernate.HibernateUtil
import org.openmrs.obs.ComplexData
import org.openmrs.util.Format
import org.openmrs.util.OpenmrsUtil
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.Locale

/**
 * An observation is a single unit of clinical information.
 *
 * Observations are collected and grouped together into one Encounter (one visit). Obs can be
 * grouped in a hierarchical fashion.
 *
 * The [obsGroup] property returns an optional parent. That parent object is also an Obs.
 * The parent Obs object knows about its child objects through the [groupMembers] property.
 *
 * (Multi-level hierarchies are achieved by an Obs parent object being a member of another Obs
 * (grand)parent object)
 *
 * @see Encounter
 */
@Audited
open class Obs() : BaseFormRecordableOpenmrsData() {

    companion object {
        const val serialVersionUID: Long = 112342333L

        private val log = LoggerFactory.getLogger(Obs::class.java)

        private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"
        private const val TIME_PATTERN = "HH:mm"
        private const val DATE_PATTERN = "yyyy-MM-dd"

        /**
         * This is an equivalent to a copy constructor. Creates a new copy of the given
         * obsToCopy with a null obs id.
         */
        @JvmStatic
        fun newInstance(obsToCopy: Obs): Obs = Obs(
            obsToCopy.person,
            obsToCopy.concept,
            obsToCopy.obsDatetime,
            obsToCopy.location
        ).apply {
            obsGroup = obsToCopy.obsGroup
            accessionNumber = obsToCopy.accessionNumber
            valueCoded = obsToCopy.valueCoded
            valueDrug = obsToCopy.valueDrug
            valueGroupId = obsToCopy.valueGroupId
            valueDatetime = obsToCopy.valueDatetime
            valueNumeric = obsToCopy.valueNumeric
            valueModifier = obsToCopy.valueModifier
            valueText = obsToCopy.valueText
            comment = obsToCopy.comment
            encounter = obsToCopy.encounter
            creator = obsToCopy.creator
            dateCreated = obsToCopy.dateCreated
            voided = obsToCopy.voided
            voidedBy = obsToCopy.voidedBy
            dateVoided = obsToCopy.dateVoided
            voidReason = obsToCopy.voidReason
            status = obsToCopy.status
            interpretation = obsToCopy.interpretation
            order = obsToCopy.order
            valueComplex = obsToCopy.valueComplex
            complexData = obsToCopy.complexData
            setFormField(obsToCopy.formFieldNamespace, obsToCopy.formFieldPath)

            // Copy list of all members, including voided
            if (obsToCopy.hasGroupMembers(true)) {
                for (member in obsToCopy.getGroupMembers(true) ?: emptySet()) {
                    if (member.obsId == null) {
                        addGroupMember(member)
                    } else {
                        val newMember = newInstance(member)
                        newMember.previousVersion = member
                        addGroupMember(newMember)
                    }
                }
            }
        }
    }

    /** @since 2.1.0 */
    enum class Interpretation {
        NORMAL, ABNORMAL, CRITICALLY_ABNORMAL, NEGATIVE, POSITIVE,
        CRITICALLY_LOW, LOW, HIGH, CRITICALLY_HIGH,
        VERY_SUSCEPTIBLE, SUSCEPTIBLE, INTERMEDIATE, RESISTANT,
        SIGNIFICANT_CHANGE_DOWN, SIGNIFICANT_CHANGE_UP,
        OFF_SCALE_LOW, OFF_SCALE_HIGH
    }

    /** @since 2.1.0 */
    enum class Status {
        PRELIMINARY, FINAL, AMENDED
    }

    var obsId: Int? = null

    private var _concept: Concept? = null
    var concept: Concept?
        get() = _concept
        set(value) {
            markAsDirty(_concept, value)
            _concept = value
        }

    private var _obsDatetime: Date? = null
    var obsDatetime: Date?
        get() = _obsDatetime
        set(value) {
            markAsDirty(_obsDatetime, value)
            _obsDatetime = value
        }

    private var _accessionNumber: String? = null
    var accessionNumber: String?
        get() = _accessionNumber
        set(value) {
            markAsDirty(_accessionNumber, value)
            _accessionNumber = value
        }

    /**
     * The "parent" of this obs. It is the grouping that brings other obs together.
     * This will be non-null if this obs is a member of another groupedObs.
     */
    private var _obsGroup: Obs? = null
    var obsGroup: Obs?
        get() = _obsGroup
        set(value) {
            markAsDirty(_obsGroup, value)
            _obsGroup = value
        }

    /** The list of obs grouped under this obs. */
    @AllowDirectAccess
    var groupMembers: MutableSet<Obs>? = null

    private var _valueCoded: Concept? = null
    var valueCoded: Concept?
        get() = _valueCoded
        set(value) {
            markAsDirty(_valueCoded, value)
            _valueCoded = value
        }

    private var _valueCodedName: ConceptName? = null
    var valueCodedName: ConceptName?
        get() = _valueCodedName
        set(value) {
            markAsDirty(_valueCodedName, value)
            _valueCodedName = value
        }

    private var _valueDrug: Drug? = null
    var valueDrug: Drug?
        get() = _valueDrug
        set(value) {
            markAsDirty(_valueDrug, value)
            _valueDrug = value
        }

    private var _valueGroupId: Int? = null
    var valueGroupId: Int?
        get() = _valueGroupId
        set(value) {
            markAsDirty(_valueGroupId, value)
            _valueGroupId = value
        }

    private var _valueDatetime: Date? = null
    var valueDatetime: Date?
        get() = _valueDatetime
        set(value) {
            markAsDirty(_valueDatetime, value)
            _valueDatetime = value
        }

    private var _valueNumeric: Double? = null
    var valueNumeric: Double?
        get() = _valueNumeric
        set(value) {
            markAsDirty(_valueNumeric, value)
            _valueNumeric = value
        }

    private var _valueModifier: String? = null
    var valueModifier: String?
        get() = _valueModifier
        set(value) {
            markAsDirty(_valueModifier, value)
            _valueModifier = value
        }

    private var _valueText: String? = null
    var valueText: String?
        get() = _valueText
        set(value) {
            markAsDirty(_valueText, value)
            _valueText = value
        }

    private var _valueComplex: String? = null
    var valueComplex: String?
        get() = _valueComplex
        set(value) {
            markAsDirty(_valueComplex, value)
            _valueComplex = value
        }

    // ComplexData is not persisted in the database.
    @Transient
    private var _complexData: ComplexData? = null
    var complexData: ComplexData?
        get() = _complexData
        set(value) {
            markAsDirty(_complexData, value)
            _complexData = value
        }

    private var _comment: String? = null
    var comment: String?
        get() = _comment
        set(value) {
            markAsDirty(_comment, value)
            _comment = value
        }

    @Transient
    private var _personId: Int? = null

    fun getPersonId(): Int? = _personId

    protected fun setPersonId(personId: Int?) {
        markAsDirty(_personId, personId)
        _personId = personId
    }

    private var _person: Person? = null
    var person: Person?
        get() = _person
        set(value) {
            markAsDirty(_person, value)
            _person = value
            value?.let { setPersonId(it.getPersonId()) }
        }

    private var _order: Order? = null
    var order: Order?
        get() = _order
        set(value) {
            markAsDirty(_order, value)
            _order = value
        }

    private var _location: Location? = null
    var location: Location?
        get() = _location
        set(value) {
            markAsDirty(_location, value)
            _location = value
        }

    private var _encounter: Encounter? = null
    var encounter: Encounter?
        get() = _encounter
        set(value) {
            markAsDirty(_encounter, value)
            _encounter = value
        }

    private var _previousVersion: Obs? = null
    var previousVersion: Obs?
        get() = _previousVersion
        set(value) {
            markAsDirty(_previousVersion, value)
            _previousVersion = value
        }

    private var dirty: Boolean = false

    private var _interpretation: Interpretation? = null
    /** @since 2.1.0 */
    var interpretation: Interpretation?
        get() = _interpretation
        set(value) {
            markAsDirty(_interpretation, value)
            _interpretation = value
        }

    private var _status: Status? = Status.FINAL
    /** @since 2.1.0 */
    var status: Status?
        get() = _status
        set(value) {
            markAsDirty(_status, value)
            _status = value
        }

    /** @since 2.7.0 */
    var referenceRange: ObsReferenceRange? = null

    /** Constructor with required fields */
    constructor(person: Person?, question: Concept?, obsDatetime: Date?, location: Location?) : this() {
        this._person = person
        person?.let { this._personId = it.getPersonId() }
        this._concept = question
        this._obsDatetime = obsDatetime
        this._location = location
    }

    /** Constructor with id */
    constructor(obsId: Int?) : this() {
        this.obsId = obsId
    }

    /** Get the concept description tied to the concept name used when making this observation */
    val conceptDescription: ConceptDescription?
        get() = concept?.getDescription()

    /**
     * Convenience method that checks for if this obs has 1 or more group members (either voided or non-voided).
     * Note this method differs from hasGroupMembers(), as that method excludes voided obs.
     */
    val isObsGrouping: Boolean
        get() = hasGroupMembers(true)

    /**
     * A convenience method to check for nullity and length to determine if this obs has group members.
     * By default, this ignores voided objects.
     */
    fun hasGroupMembers(): Boolean = hasGroupMembers(false)

    /**
     * Convenience method that checks for nullity and length to determine if this obs has group members.
     */
    fun hasGroupMembers(includeVoided: Boolean): Boolean =
        !getGroupMembers(includeVoided).isNullOrEmpty()

    /**
     * Get the group members of this obs group.
     */
    fun getGroupMembers(includeVoided: Boolean = false): Set<Obs>? {
        if (includeVoided) return groupMembers
        return groupMembers?.filterNot { it.voided == true }?.toCollection(LinkedHashSet())
    }

    /** Convenience method to add the given obs to this grouping. */
    fun addGroupMember(member: Obs?) {
        member ?: return
        if (groupMembers == null) groupMembers = HashSet()

        if (member == this) {
            throw APIException("Obs.error.groupCannotHaveItselfAsAMentor", arrayOf(this, member))
        }

        member.obsGroup = this
        groupMembers!!.add(member)
    }

    /** Convenience method to remove an Obs from this grouping. */
    fun removeGroupMember(member: Obs?) {
        if (member == null || groupMembers == null) return

        if (groupMembers!!.remove(member)) {
            member.obsGroup = null
        }
    }

    /**
     * Convenience method that returns related Obs.
     */
    val relatedObservations: Set<Obs>
        get() {
            val ret = HashSet<Obs>()
            if (isObsGrouping) {
                ret.addAll(getGroupMembers() ?: emptySet())
                var parentObs: Obs? = this
                while (parentObs?.obsGroup != null) {
                    for (obsSibling in parentObs.obsGroup!!.getGroupMembers() ?: emptySet()) {
                        if (!obsSibling.isObsGrouping) {
                            ret.add(obsSibling)
                        }
                    }
                    parentObs = parentObs.obsGroup
                }
            } else if (obsGroup != null) {
                for (obsSibling in obsGroup!!.getGroupMembers() ?: emptySet()) {
                    if (!obsSibling.isObsGrouping) {
                        ret.add(obsSibling)
                    }
                }
            }
            return ret
        }

    // Boolean value handling

    /** Sets the value of this obs to the specified valueBoolean if this obs has a boolean concept. */
    fun setValueBoolean(valueBoolean: Boolean?) {
        val datatype = concept?.datatype
        if (datatype?.isBoolean == true) {
            valueCoded = when (valueBoolean) {
                true -> Context.getConceptService().getTrueConcept()
                false -> Context.getConceptService().getFalseConcept()
                null -> null
            }
        }
    }

    /** Coerces a value to a Boolean representation. */
    val valueAsBoolean: Boolean?
        get() {
            valueCoded?.let { coded ->
                return when (coded) {
                    Context.getConceptService().getTrueConcept() -> true
                    Context.getConceptService().getFalseConcept() -> false
                    else -> null
                }
            }
            valueNumeric?.let { numeric ->
                return when (numeric) {
                    1.0 -> true
                    0.0 -> false
                    else -> null
                }
            }
            return null
        }

    /** Returns the boolean value if the concept of this obs is of boolean datatype. */
    val valueBoolean: Boolean?
        get() {
            if (concept?.datatype?.isBoolean == true && valueCoded != null) {
                val trueConcept = Context.getConceptService().getTrueConcept()
                return trueConcept != null && valueCoded?.id == trueConcept.id
            }
            return null
        }

    /** Alias for valueDatetime for date-only values. */
    var valueDate: Date?
        get() = valueDatetime
        set(value) { valueDatetime = value }

    /** Alias for valueDatetime for time-only values. */
    var valueTime: Date?
        get() = valueDatetime
        set(value) { valueDatetime = value }

    /** Returns true if this Obs is complex. */
    val isComplex: Boolean
        get() = concept?.isComplex() == true

    /**
     * Convenience method for obtaining the observation's value as a string.
     */
    fun getValueAsString(locale: Locale): String {
        val nf = NumberFormat.getNumberInstance(locale) as DecimalFormat
        nf.applyPattern("#0.0#####")

        val concept = this.concept
        if (concept != null) {
            when (val abbrev = concept.datatype?.hl7Abbreviation) {
                "BIT" -> return valueAsBoolean?.toString() ?: ""
                "CWE" -> {
                    if (valueCoded == null) return ""
                    valueDrug?.let { return it.getFullName(locale) }
                    return valueCodedName?.let { valueCoded?.getName(locale, false)?.name }
                        ?: valueCoded?.getName()?.name
                        ?: ""
                }
                "NM", "SN" -> {
                    if (valueNumeric == null) return ""
                    val deproxiedConcept = HibernateUtil.getRealObjectFromProxy(concept)
                    if (deproxiedConcept is ConceptNumeric) {
                        if (!deproxiedConcept.allowDecimal) {
                            return valueNumeric!!.toInt().toString()
                        }
                    }
                    return nf.format(valueNumeric)
                }
                "DT" -> {
                    return valueDatetime?.let { SimpleDateFormat(DATE_PATTERN).format(it) } ?: ""
                }
                "TM" -> {
                    return valueDatetime?.let { Format.format(it, locale, Format.FORMAT_TYPE.TIME) } ?: ""
                }
                "TS" -> {
                    return valueDatetime?.let { Format.format(it, locale, Format.FORMAT_TYPE.TIMESTAMP) } ?: ""
                }
                "ST" -> return valueText ?: ""
                "ED" -> {
                    valueComplex?.split("\\|".toRegex())?.forEach { value ->
                        if (StringUtils.isNotEmpty(value)) {
                            return value.trim()
                        }
                    }
                }
            }
        }

        // if the datatype is 'unknown', default to just returning what is not null
        valueNumeric?.let { return nf.format(it) }

        valueCoded?.let {
            valueDrug?.let { drug -> return drug.getFullName(locale) }
            return valueCodedName?.name ?: ""
        }

        valueDatetime?.let { return Format.format(it, locale, Format.FORMAT_TYPE.DATE) }

        valueText?.let { return it }

        if (hasGroupMembers()) {
            return getGroupMembers()?.joinToString(", ") { it.getValueAsString(locale) } ?: ""
        }

        // returns the title portion of the valueComplex
        valueComplex?.split("\\|".toRegex())?.forEach { value ->
            if (StringUtils.isNotEmpty(value)) {
                return value.trim()
            }
        }

        return ""
    }

    /**
     * Sets the value for the obs from a string depending on the datatype of the question concept.
     */
    @Throws(ParseException::class)
    fun setValueAsString(s: String?) {
        log.debug("getConcept() == {}", concept)

        if (concept != null && s != null) {
            val abbrev = concept!!.datatype?.hl7Abbreviation
            when {
                abbrev == "ST" -> {
                    if (s.isNotEmpty()) {
                        valueText = s
                    } else {
                        throw RuntimeException("Cannot set value to a empty string for concept: ${concept?.displayString}")
                    }
                }
                s.isNotBlank() -> {
                    val trimmed = s.trim()
                    when (abbrev) {
                        "BIT" -> setValueBoolean(trimmed.toBoolean())
                        "CWE" -> throw RuntimeException("Not Yet Implemented")
                        "NM", "SN" -> valueNumeric = trimmed.toDouble()
                        "DT" -> valueDatetime = SimpleDateFormat(DATE_PATTERN).parse(trimmed)
                        "TM" -> valueDatetime = SimpleDateFormat(TIME_PATTERN).parse(trimmed)
                        "TS" -> valueDatetime = SimpleDateFormat(DATE_TIME_PATTERN).parse(trimmed)
                        else -> throw RuntimeException("Don't know how to handle $abbrev for concept: ${concept?.displayString}")
                    }
                }
                else -> throw RuntimeException("Cannot set value to a blank string for concept: ${concept?.displayString}")
            }
        } else {
            if (s == null) {
                throw RuntimeException("cannot set value to null via setValueAsString()")
            } else {
                throw RuntimeException("concept is null for $this")
            }
        }
    }

    override fun setFormField(namespace: String?, formFieldPath: String?) {
        val oldValue = formNamespaceAndPath
        super.setFormField(namespace, formFieldPath)
        markAsDirty(oldValue, formNamespaceAndPath)
    }

    /**
     * Returns true if any change has been made to an Obs instance.
     * @since 2.0
     */
    fun isDirty(): Boolean = dirty

    protected fun markAsDirty(oldValue: Any?, newValue: Any?) {
        if (!isDirty() && obsId != null && !OpenmrsUtil.nullSafeEquals(oldValue, newValue)) {
            dirty = true
        }
    }

    fun hasPreviousVersion(): Boolean = previousVersion != null

    override fun toString(): String =
        obsId?.let { "Obs #$it" } ?: "obs id is null"

    override var id: Integer?
        get() = obsId?.let { Integer(it) }
        set(value) { obsId = value?.toInt() }
}
