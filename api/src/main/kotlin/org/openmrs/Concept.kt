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
import org.apache.commons.lang3.StringUtils
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.hibernate.search.engine.backend.types.ObjectStructure
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.annotation.AllowDirectAccess
import org.openmrs.api.APIException
import org.openmrs.api.ConceptNameType
import org.openmrs.api.context.Context
import org.openmrs.api.db.hibernate.search.bridge.OpenmrsObjectValueBridge
import org.openmrs.customdatatype.CustomValueDescriptor
import org.openmrs.customdatatype.Customizable
import org.openmrs.util.LocaleUtility
import org.openmrs.util.OpenmrsUtil
import org.slf4j.LoggerFactory
import org.springframework.util.ObjectUtils
import java.io.Serializable
import java.util.Collections
import java.util.Date
import java.util.LinkedHashSet
import java.util.Locale
import java.util.TreeSet

/**
 * A Concept object can represent either a question or an answer to a data point. That data point is
 * usually an [Obs].
 *
 * A Concept can have multiple names and multiple descriptions within one locale and across multiple
 * locales.
 *
 * @see ConceptName
 * @see ConceptDescription
 * @see ConceptAnswer
 * @see ConceptSet
 * @see ConceptMap
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
open class Concept() : BaseOpenmrsObject(), Auditable, Retireable, Serializable, Attributable<Concept>, Customizable<ConceptAttribute> {

    companion object {
        const val serialVersionUID: Long = 57332L
        private val log = LoggerFactory.getLogger(Concept::class.java)
        private const val CONCEPT_NAME_LOCALE_NULL = "Concept.name.locale.null"
    }

    @DocumentId
    private var _conceptId: Int? = null

    fun getConceptId(): Int? = _conceptId
    fun setConceptId(conceptId: Int?) { _conceptId = conceptId }

    @GenericField
    private var _retired: Boolean = false

    override var retired: Boolean?
        get() = _retired
        set(value) { _retired = value ?: false }

    override var retiredBy: User? = null
    override var dateRetired: Date? = null
    override var retireReason: String? = null

    @KeywordField(valueBridge = ValueBridgeRef(type = OpenmrsObjectValueBridge::class))
    var datatype: ConceptDatatype? = null

    @KeywordField(valueBridge = ValueBridgeRef(type = OpenmrsObjectValueBridge::class))
    var conceptClass: ConceptClass? = null

    private var _set: Boolean = false

    var set: Boolean?
        get() = _set
        set(value) { _set = value ?: false }

    var version: String? = null

    override var creator: User? = null
    override var dateCreated: Date? = null
    override var changedBy: User? = null
    override var dateChanged: Date? = null

    @AllowDirectAccess
    @AssociationInverseSide(inversePath = ObjectPath(PropertyValue(propertyName = "concept")))
    private var _names: MutableCollection<ConceptName>? = null

    @AllowDirectAccess
    private var _answers: MutableCollection<ConceptAnswer>? = null

    private var _conceptSets: MutableCollection<ConceptSet>? = null
    private var _descriptions: MutableCollection<ConceptDescription>? = null

    @IndexedEmbedded
    @AssociationInverseSide(inversePath = ObjectPath(PropertyValue(propertyName = "concept")))
    private var _conceptMappings: MutableCollection<ConceptMap>? = null

    private var compatibleCache: MutableMap<Locale, MutableList<ConceptName>>? = null
    private var _attributes: MutableSet<ConceptAttribute> = LinkedHashSet()

    /** Constructor with conceptId */
    constructor(conceptId: Int?) : this() {
        this._conceptId = conceptId
    }

    init {
        _names = HashSet()
        _answers = HashSet()
        _conceptSets = TreeSet()
        _descriptions = HashSet()
        _conceptMappings = HashSet()
    }

    // Answers

    var answers: MutableCollection<ConceptAnswer>
        get() {
            if (_answers == null) _answers = HashSet()
            return _answers!!
        }
        set(value) { _answers = value }

    fun getAnswers(includeRetired: Boolean): Collection<ConceptAnswer> =
        if (includeRetired) answers
        else answers.filterNot { it.answerConcept?.retired == true }.toSet()

    fun addAnswer(conceptAnswer: ConceptAnswer?) {
        conceptAnswer ?: return
        if (conceptAnswer !in answers) {
            conceptAnswer.concept = this
            answers.add(conceptAnswer)
        }

        if (conceptAnswer.sortWeight == null || conceptAnswer.sortWeight!! <= 0) {
            val maxAnswer = answers.maxOrNull()
            val sortWeight = maxAnswer?.sortWeight?.let { it + 1.0 } ?: 1.0
            conceptAnswer.sortWeight = sortWeight
        }
    }

    fun removeAnswer(conceptAnswer: ConceptAnswer): Boolean = answers.remove(conceptAnswer)

    // Names

    var names: MutableCollection<ConceptName>
        get() = getNames(false)
        set(value) { _names = value }

    fun getNames(includeVoided: Boolean): MutableCollection<ConceptName> {
        if (_names == null) _names = HashSet()
        return if (includeVoided) _names!!
        else _names!!.filterNot { it.voided == true }.toMutableSet()
    }

    fun getNames(locale: Locale): Collection<ConceptName> =
        names.filter { it.locale == locale }.toSet()

    fun getName(): ConceptName? {
        if (names.isEmpty()) {
            log.debug("there are no names defined for: {}", _conceptId)
            return null
        }

        for (currentLocale in LocaleUtility.getLocalesInOrder()) {
            getPreferredName(currentLocale)?.let { return it }
            getFullySpecifiedName(currentLocale)?.let { return it }

            if (currentLocale.country.isNotBlank() || currentLocale.variant.isNotBlank()) {
                val broaderLocale = Locale(currentLocale.language)
                getPreferredName(broaderLocale)?.let { return it }
                getFullySpecifiedName(broaderLocale)?.let { return it }
            }
        }

        names.firstOrNull { it.isFullySpecifiedName }?.let { return it }
        synonyms.firstOrNull()?.let { return it }

        return null
    }

    fun getName(locale: Locale): ConceptName? = getName(locale, false)

    fun getName(locale: Locale, exact: Boolean): ConceptName? {
        if (names.isEmpty()) {
            log.debug("there are no names defined for: {}", _conceptId)
            return null
        }

        log.debug("Getting conceptName for locale: {}", locale)

        getNameInLocale(locale)?.let { return it }

        if (!exact) {
            val broaderLocale = Locale(locale.language)
            getNameInLocale(broaderLocale)?.let { return it }
            return getName()
        }
        return null
    }

    fun getName(locale: Locale, ofType: ConceptNameType?, havingTag: ConceptNameTag?): ConceptName? {
        val namesInLocale = getNames(locale)
        if (namesInLocale.isNotEmpty()) {
            val matches = namesInLocale.filter { cn ->
                (ofType == null || ofType == cn.conceptNameType) &&
                (havingTag == null || cn.hasTag(havingTag))
            }

            if (matches.size == 1) return matches[0]
            if (matches.size > 1) {
                matches.firstOrNull { it.localePreferred == true }?.let { return it }
                return matches[0]
            }
        }

        val parent = Locale(locale.language)
        return if (parent != locale) getName(parent, ofType, havingTag) else null
    }

    private fun getNameInLocale(locale: Locale): ConceptName? {
        getPreferredName(locale)?.let { return it }
        getFullySpecifiedName(locale)?.let { return it }
        getSynonyms(locale).firstOrNull()?.let { return it }
        return null
    }

    fun getPreferredName(forLocale: Locale): ConceptName? = getPreferredName(forLocale, false)

    fun getPreferredName(forLocale: Locale?, exact: Boolean): ConceptName? {
        if (log.isDebugEnabled) {
            log.debug("Getting preferred conceptName for locale: $forLocale")
        }

        if (forLocale == null) {
            log.warn("Locale cannot be null")
            return null
        }

        getNames(forLocale).firstOrNull { ObjectUtils.nullSafeEquals(it.localePreferred, true) }?.let { return it }

        if (exact) return null

        var bestMatch: ConceptName? = null
        for (nameInLocale in getPartiallyCompatibleNames(forLocale)) {
            if (ObjectUtils.nullSafeEquals(nameInLocale.localePreferred, true)) {
                val nameLocale = nameInLocale.locale
                if (forLocale.language == nameLocale?.language) {
                    return nameInLocale
                } else {
                    bestMatch = nameInLocale
                }
            }
        }

        return bestMatch ?: getFullySpecifiedName(forLocale)
    }

    fun getFullySpecifiedName(locale: Locale?): ConceptName? {
        if (locale == null || getNames(locale).isEmpty()) return null

        getNames(locale).firstOrNull { ObjectUtils.nullSafeEquals(it.isFullySpecifiedName, true) }?.let { return it }

        var bestMatch: ConceptName? = null
        for (conceptName in getPartiallyCompatibleNames(locale)) {
            if (ObjectUtils.nullSafeEquals(conceptName.isFullySpecifiedName, true)) {
                if (locale.language == conceptName.locale?.language) {
                    return conceptName
                }
                bestMatch = conceptName
            }
        }
        return bestMatch
    }

    private fun getPartiallyCompatibleNames(locale: Locale): Collection<ConceptName> {
        val language = locale.language
        val country = locale.country
        return names.filter { n ->
            language == n.locale?.language ||
            (country.isNotBlank() && country == n.locale?.country)
        }.toSet()
    }

    fun getCompatibleNames(desiredLocale: Locale): List<ConceptName> {
        if (compatibleCache == null) {
            compatibleCache = mutableMapOf()
        }

        compatibleCache!![desiredLocale]?.let { return it }

        val compatibleNames = names.filter { LocaleUtility.areCompatible(it.locale, desiredLocale) }.toMutableList()
        compatibleCache!![desiredLocale] = compatibleNames
        return compatibleNames
    }

    fun hasName(name: String?, locale: Locale?): Boolean {
        if (name == null) return false
        val currentNames = if (locale == null) names else getNames(locale)
        return currentNames.any { name.equals(it.name, ignoreCase = true) }
    }

    fun setPreferredName(preferredName: ConceptName?) {
        if (preferredName == null || preferredName.voided == true || preferredName.isIndexTerm) {
            throw APIException("Concept.error.preferredName.null", null as Array<Any>?)
        }
        if (preferredName.locale == null) {
            throw APIException(CONCEPT_NAME_LOCALE_NULL, null as Array<Any>?)
        }

        getPreferredName(preferredName.locale!!, true)?.localePreferred = false
        preferredName.localePreferred = true

        if (preferredName.conceptNameId == null || preferredName !in names) {
            addName(preferredName)
        }
    }

    fun setFullySpecifiedName(fullySpecifiedName: ConceptName?) {
        if (fullySpecifiedName?.locale == null) {
            throw APIException(CONCEPT_NAME_LOCALE_NULL, null as Array<Any>?)
        }
        if (fullySpecifiedName.voided == true) {
            throw APIException("Concept.error.fullySpecifiedName.null", null as Array<Any>?)
        }

        getFullySpecifiedName(fullySpecifiedName.locale)?.conceptNameType = null
        fullySpecifiedName.conceptNameType = ConceptNameType.FULLY_SPECIFIED

        if (fullySpecifiedName.conceptNameId == null || fullySpecifiedName !in names) {
            addName(fullySpecifiedName)
        }
    }

    fun setShortName(shortName: ConceptName?) {
        if (shortName == null) {
            throw APIException("Concept.error.shortName.null", null as Array<Any>?)
        }
        if (shortName.locale == null) {
            throw APIException(CONCEPT_NAME_LOCALE_NULL, null as Array<Any>?)
        }

        getShortNameInLocale(shortName.locale!!)?.conceptNameType = null
        shortName.conceptNameType = ConceptNameType.SHORT

        if (StringUtils.isNotBlank(shortName.name) &&
            (shortName.conceptNameId == null || shortName !in names)) {
            addName(shortName)
        }
    }

    fun getShortNameInLocale(locale: Locale): ConceptName? {
        var bestMatch: ConceptName? = null
        if (shortNames.isNotEmpty()) {
            for (shortName in shortNames) {
                val nameLocale = shortName.locale
                if (nameLocale == locale) return shortName
                if (OpenmrsUtil.nullSafeEquals(locale.language, nameLocale?.language)) {
                    bestMatch = shortName
                } else if (bestMatch == null && locale.country.isNotBlank() &&
                    locale.country == nameLocale?.country) {
                    bestMatch = shortName
                }
            }
        }
        return bestMatch
    }

    val shortNames: Collection<ConceptName>
        get() = if (names.isEmpty()) {
            log.debug("The Concept with id: {} has no names", _conceptId)
            emptyList()
        } else {
            names.filter { it.isShort }
        }

    fun getShortestName(locale: Locale?, exact: Boolean): ConceptName? {
        log.debug("Getting shortest conceptName for locale: {}", locale)

        locale?.let { getShortNameInLocale(it) }?.let { return it }

        var shortestNameForLocale: ConceptName? = null
        var shortestNameForConcept: ConceptName? = null

        if (locale != null) {
            for (possibleName in names) {
                if (possibleName.locale == locale &&
                    (shortestNameForLocale == null ||
                     possibleName.name!!.length < shortestNameForLocale.name!!.length)) {
                    shortestNameForLocale = possibleName
                }
                if (shortestNameForConcept == null ||
                    possibleName.name!!.length < shortestNameForConcept.name!!.length) {
                    shortestNameForConcept = possibleName
                }
            }
        }

        if (exact) {
            if (shortestNameForLocale == null) {
                log.warn("No short concept name found for concept id {} for locale {}",
                    _conceptId, locale?.displayName)
            }
            return shortestNameForLocale
        }

        return shortestNameForConcept
    }

    fun addName(conceptName: ConceptName?) {
        conceptName ?: return
        conceptName.concept = this
        if (_names == null) _names = HashSet()

        if (conceptName !in _names!!) {
            if (names.isEmpty() && conceptName.conceptNameType != ConceptNameType.FULLY_SPECIFIED) {
                conceptName.conceptNameType = ConceptNameType.FULLY_SPECIFIED
            } else {
                if (conceptName.isPreferred && !conceptName.isIndexTerm && conceptName.locale != null) {
                    getPreferredName(conceptName.locale!!, true)?.localePreferred = false
                }
                if (conceptName.isFullySpecifiedName && conceptName.locale != null) {
                    getFullySpecifiedName(conceptName.locale)?.conceptNameType = null
                } else if (conceptName.isShort && conceptName.locale != null) {
                    getShortNameInLocale(conceptName.locale!!)?.conceptNameType = null
                }
            }
            _names!!.add(conceptName)
            compatibleCache?.clear()
        }
    }

    fun removeName(conceptName: ConceptName): Boolean = _names?.remove(conceptName) ?: false

    fun findNameTaggedWith(conceptNameTag: ConceptNameTag): ConceptName? =
        names.firstOrNull { it.hasTag(conceptNameTag) }

    fun isNamed(name: String): Boolean = names.any { name == it.name }

    val indexTerms: Collection<ConceptName>
        get() = names.filter { it.isIndexTerm }.toSet()

    fun getIndexTermsForLocale(locale: Locale): Collection<ConceptName> =
        indexTerms.filter { it.locale == locale }

    val synonyms: Collection<ConceptName>
        get() = names.filter { it.isSynonym }.toSet()

    fun getSynonyms(locale: Locale): Collection<ConceptName> {
        var preferredConceptName: ConceptName? = null
        val syns = mutableListOf<ConceptName>()

        for (possibleSynonymInLoc in synonyms) {
            if (locale == possibleSynonymInLoc.locale) {
                if (possibleSynonymInLoc.isPreferred) {
                    preferredConceptName = possibleSynonymInLoc
                } else {
                    syns.add(possibleSynonymInLoc)
                }
            }
        }

        preferredConceptName?.let { syns.add(0, it) }
        log.debug("returning: {}", syns)
        return syns
    }

    val allConceptNameLocales: Set<Locale>?
        get() {
            if (names.isEmpty()) {
                log.debug("The Concept with id: {} has no names", _conceptId)
                return null
            }
            return names.mapNotNull { it.locale }.toSet()
        }

    // Descriptions

    var descriptions: MutableCollection<ConceptDescription>
        get() {
            if (_descriptions == null) _descriptions = HashSet()
            return _descriptions!!
        }
        set(value) { _descriptions = value }

    fun getDescription(): ConceptDescription? = getDescription(Context.getLocale())

    fun getDescription(locale: Locale?): ConceptDescription? = getDescription(locale, false)

    fun getDescription(locale: Locale?, exact: Boolean): ConceptDescription? {
        log.debug("Getting ConceptDescription for locale: {}", locale)

        val desiredLocale = locale ?: LocaleUtility.getDefaultLocale()
        var foundDescription: ConceptDescription? = null
        var defaultDescription: ConceptDescription? = null

        for (availableDescription in descriptions) {
            val availableLocale = availableDescription.locale
            if (availableLocale == desiredLocale) {
                foundDescription = availableDescription
                break
            }
            if (!exact && LocaleUtility.areCompatible(availableLocale, desiredLocale)) {
                foundDescription = availableDescription
            }
            if (availableLocale == LocaleUtility.getDefaultLocale()) {
                defaultDescription = availableDescription
            }
        }

        if (foundDescription == null) {
            if (exact) {
                log.debug("No concept description found for concept id {} for locale {}",
                    _conceptId, desiredLocale)
            } else {
                if (defaultDescription == null) {
                    log.debug("No concept description found for default locale for concept id {}", _conceptId)
                } else {
                    foundDescription = defaultDescription
                }
            }
        }
        return foundDescription
    }

    fun addDescription(description: ConceptDescription?) {
        if (description != null && StringUtils.isNotBlank(description.description) &&
            description !in descriptions) {
            description.concept = this
            descriptions.add(description)
        }
    }

    fun removeDescription(description: ConceptDescription): Boolean = descriptions.remove(description)

    // ConceptSets

    var conceptSets: MutableCollection<ConceptSet>?
        get() = _conceptSets
        set(value) { _conceptSets = value }

    val setMembers: List<Concept>
        get() = Collections.unmodifiableList(sortedConceptSets.map { it.concept }.toList())

    fun getSetMembers(includeRetired: Boolean): List<Concept> =
        if (includeRetired) setMembers
        else setMembers.filterNot { it.retired == true }

    private val sortedConceptSets: List<ConceptSet>
        get() = _conceptSets?.sorted() ?: emptyList()

    fun addSetMember(setMember: Concept) = addSetMember(setMember, -1)

    fun addSetMember(setMember: Concept, index: Int) {
        val sortedSets = sortedConceptSets.toMutableList()
        val setsSize = sortedSets.size

        var weight = 990.0
        for (conceptSet in sortedSets) {
            weight += 10.0
            conceptSet.sortWeight = weight
        }

        weight = when {
            sortedSets.isEmpty() -> 1000.0
            index == -1 || index >= setsSize -> sortedSets[setsSize - 1].sortWeight!! + 10.0
            index == 0 -> sortedSets[0].sortWeight!! - 10.0
            else -> (sortedSets[index - 1].sortWeight!! + sortedSets[index].sortWeight!!) / 2
        }

        val conceptSet = ConceptSet(setMember, weight)
        conceptSet.conceptSet = this
        _conceptSets?.add(conceptSet)
    }

    // ConceptMappings

    var conceptMappings: MutableCollection<ConceptMap>
        get() {
            if (_conceptMappings == null) _conceptMappings = HashSet()
            return _conceptMappings!!
        }
        set(value) { _conceptMappings = value }

    fun addConceptMapping(newConceptMap: ConceptMap?) {
        newConceptMap?.concept = this
        if (newConceptMap != null && newConceptMap !in conceptMappings) {
            if (newConceptMap.conceptMapType == null) {
                newConceptMap.conceptMapType = Context.getConceptService().defaultConceptMapType
            }
            conceptMappings.add(newConceptMap)
        }
    }

    fun removeConceptMapping(conceptMap: ConceptMap): Boolean = conceptMappings.remove(conceptMap)

    // Attributes

    override fun getAttributes(): MutableSet<ConceptAttribute> {
        if (_attributes == null) _attributes = LinkedHashSet()
        return _attributes
    }

    fun setAttributes(attributes: MutableSet<ConceptAttribute>) {
        _attributes = attributes
    }

    override fun getActiveAttributes(): Collection<ConceptAttribute> =
        attributes.filterNot { it.voided == true }

    override fun getActiveAttributes(ofType: CustomValueDescriptor): List<ConceptAttribute> =
        attributes.filter { it.attributeType == ofType && it.voided != true }

    override fun addAttribute(attribute: ConceptAttribute) {
        attributes.add(attribute)
        attribute.owner = this
    }

    // Deprecated methods

    /** @deprecated as of 2.0, use [set] */
    @Deprecated("Use set property", ReplaceWith("set"))
    @JsonIgnore
    fun isSet(): Boolean? = set

    /** Whether this concept is numeric. ConceptNumeric.isNumeric() will return true. */
    open fun isNumeric(): Boolean = false

    /** Child Class ConceptComplex overrides this method. */
    open fun isComplex(): Boolean = false

    override fun toString(): String = "Concept #$_conceptId"

    // Attributable implementation

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun findPossibleValues(searchText: String): List<Concept> =
        runCatching {
            Context.getConceptService()
                .getConcepts(searchText, listOf(Context.getLocale()), false, null, null, null, null, null, null, null)
                .map { it.concept }
        }.getOrDefault(emptyList())

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun getPossibleValues(): List<Concept> =
        runCatching { Context.getConceptService().getConceptsByName("") }
            .getOrDefault(emptyList())

    override fun hydrate(s: String): Concept =
        runCatching { Context.getConceptService().getConceptByReference(s) }
            .getOrDefault(Concept())

    override fun serialize(): String = _conceptId?.toString() ?: ""

    override val displayString: String?
        get() = getName()?.name ?: toString()

    override var id: Integer?
        get() = _conceptId?.let { Integer(it) }
        set(value) { _conceptId = value?.toInt() }
}
