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
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField
import org.openmrs.api.ConceptNameType
import org.openmrs.api.db.hibernate.search.SearchAnalysis
import org.openmrs.api.db.hibernate.search.bridge.LocaleValueBridge
import java.io.Serializable
import java.util.Date
import java.util.HashSet
import java.util.Locale

/**
 * ConceptName is the real world term used to express a Concept within the idiom of a particular
 * locale.
 */
@Indexed
@Audited
class ConceptName() : BaseOpenmrsObject(), Auditable, Voidable, Serializable {

    companion object {
        const val serialVersionUID: Long = 2L
    }

    @DocumentId
    var conceptNameId: Int? = null

    @IndexedEmbedded(includeEmbeddedObjectId = true)
    var concept: Concept? = null

    @FullTextField(analyzer = SearchAnalysis.NAME_ANALYZER)
    private var _name: String? = null

    @KeywordField(valueBridge = ValueBridgeRef(type = LocaleValueBridge::class))
    var locale: Locale? = null

    override var creator: User? = null

    override var dateCreated: Date? = null

    @GenericField
    override var voided: Boolean? = false

    override var voidedBy: User? = null

    override var dateVoided: Date? = null

    override var voidReason: String? = null

    private var _tags: MutableCollection<ConceptNameTag>? = null

    @GenericField
    var conceptNameType: ConceptNameType? = null

    @GenericField
    var localePreferred: Boolean = false

    override var changedBy: User? = null

    override var dateChanged: Date? = null

    constructor(conceptNameId: Int?) : this() {
        this.conceptNameId = conceptNameId
    }

    constructor(name: String?, locale: Locale?) : this() {
        this.name = name
        this.locale = locale
    }

    var name: String?
        get() = _name
        set(value) {
            if (value != null && StringUtils.isBlank(value) && StringUtils.isNotBlank(_name)
                && conceptNameType == ConceptNameType.SHORT
            ) {
                voided = true
            } else {
                _name = value
            }
        }

    var tags: MutableCollection<ConceptNameTag>?
        get() = _tags
        set(value) { _tags = value }

    override var id: Integer?
        get() = conceptNameId?.let { Integer(it) }
        set(value) { conceptNameId = value?.toInt() }

    /**
     * Returns whether the ConceptName has been voided.
     *
     * @return true if the ConceptName has been voided, false otherwise.
     *
     * @deprecated as of 2.0, use [voided]
     */
    @Deprecated("Use voided property", ReplaceWith("voided"))
    @JsonIgnore
    fun isVoided(): Boolean = voided

    /**
     * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
     * ConceptNameTag will be created with a blank description.
     *
     * @see Concept.setPreferredName
     * @see Concept.setFullySpecifiedName
     * @see Concept.setShortName
     * @param tag human-readable text string for the tag
     */
    fun addTag(tag: String) {
        addTag(tag, "")
    }

    /**
     * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
     * ConceptNameTag will be created with the given description.
     *
     * @see Concept.setPreferredName
     * @see Concept.setFullySpecifiedName
     * @see Concept.setShortName
     * @param tag human-readable text string for the tag
     * @param description description of the tag's purpose
     */
    fun addTag(tag: String, description: String) {
        val nameTag = ConceptNameTag(tag, description)
        addTag(nameTag)
    }

    /**
     * Attaches a tag to the concept name.
     *
     * @see Concept.setPreferredName
     * @see Concept.setFullySpecifiedName
     * @see Concept.setShortName
     * @param tag the tag to add
     */
    fun addTag(tag: ConceptNameTag) {
        if (_tags == null) {
            _tags = HashSet()
        }
        if (!_tags!!.contains(tag)) {
            _tags!!.add(tag)
        }
    }

    /**
     * Removes a tag from the concept name.
     *
     * @see Concept.setPreferredName
     * @see Concept.setFullySpecifiedName
     * @see Concept.setShortName
     * @param tag the tag to remove
     */
    fun removeTag(tag: ConceptNameTag) {
        _tags?.remove(tag)
    }

    /**
     * Checks whether the name has a particular tag.
     *
     * @see isPreferred
     * @see isFullySpecifiedName
     * @see isIndexTerm
     * @see isSynonym
     * @see isShort
     * @param tagToFind the tag for which to check
     * @return true if the tags include the specified tag, false otherwise
     */
    fun hasTag(tagToFind: ConceptNameTag): Boolean = hasTag(tagToFind.tag)

    /**
     * Checks whether the name has a particular tag.
     *
     * @see isPreferred
     * @see isFullySpecifiedName
     * @see isIndexTerm
     * @see isSynonym
     * @see isShort
     * @param tagToFind the string of the tag for which to check
     * @return true if the tags include the specified tag, false otherwise
     */
    fun hasTag(tagToFind: String?): Boolean {
        var foundTag = false
        if (_tags != null) {
            for (nameTag in _tags!!) {
                if (nameTag.tag == tagToFind) {
                    foundTag = true
                    break
                }
            }
        }
        return foundTag
    }

    /**
     * Checks whether the name is explicitly marked as preferred in a locale with a matching
     * language. E.g 'en_US' and 'en_UK' for language en
     *
     * @see isPreferredForLocale
     * @param language ISO 639 2-letter code for a language
     * @return true if the name is preferred in a locale with a matching language code, otherwise
     *         false
     */
    fun isPreferredInLanguage(language: String?): Boolean =
        !StringUtils.isBlank(language) && locale != null && isPreferred()
                && locale!!.language == language

    /**
     * Checks whether the name is explicitly marked as preferred in a locale with a matching country
     * code E.g 'fr_RW' and 'en_RW' for country RW
     *
     * @see isPreferredForLocale
     * @param country ISO 3166 2-letter code for a country
     * @return true if the name is preferred in a locale with a matching country code, otherwise
     *         false
     */
    fun isPreferredInCountry(country: String?): Boolean =
        !StringUtils.isBlank(country) && locale != null && isPreferred()
                && locale!!.country == country

    /**
     * Checks whether the name is explicitly marked as preferred for any locale. Note that this
     * method is different from [isPreferredForLocale] in that it checks if the given
     * name is marked as preferred irrespective of the locale in which it is preferred.
     *
     * @see isPreferredForLocale
     */
    fun isPreferred(): Boolean = localePreferred

    /**
     * Checks whether the name is explicitly marked as preferred for the given locale
     *
     * @param locale the locale in which the name is preferred
     * @return true if the name is marked as preferred for the given locale otherwise false.
     */
    fun isPreferredForLocale(locale: Locale?): Boolean = localePreferred && this.locale == locale

    /**
     * Checks whether the concept name is explicitly marked as fully specified
     *
     * @return true if the name is marked as 'fully specified' otherwise false
     * @since Version 1.7
     */
    fun isFullySpecifiedName(): Boolean = ConceptNameType.FULLY_SPECIFIED == conceptNameType

    /**
     * Convenience method for determining whether this is a short name.
     *
     * @return true if the name is marked as a short name, otherwise false
     */
    fun isShort(): Boolean = ConceptNameType.SHORT == conceptNameType

    /**
     * Convenience method for checking whether this is an index Term.
     *
     * @return true if the name is marked as an index term, otherwise false
     * @since Version 1.7
     */
    fun isIndexTerm(): Boolean = ConceptNameType.INDEX_TERM == conceptNameType

    /**
     * Convenience method for determining whether this is an index Term for a given locale.
     *
     * @param locale The locale in which this concept name should belong as an index term
     * @return true if the name is marked as an index term, otherwise false
     */
    fun isIndexTermInLocale(locale: Locale?): Boolean =
        conceptNameType != null && conceptNameType == ConceptNameType.INDEX_TERM
                && locale == this.locale

    /**
     * Convenience method for determining whether this is a synonym in a given locale.
     *
     * @param locale The locale in which this synonym should belong
     * @return true if the concept name is marked as a synonym in the given locale, otherwise false
     */
    fun isSynonymInLocale(locale: Locale?): Boolean = conceptNameType == null && locale == this.locale

    /**
     * Convenience method for checking whether this is a a synonym.
     *
     * @return true if the name is tagged as a synonym, false otherwise
     * @since Version 1.7
     */
    fun isSynonym(): Boolean = conceptNameType == null

    /**
     * Getter for localePreferred
     *
     * @return localPreferred
     *
     * @deprecated as of 2.0, use [localePreferred]
     */
    @Deprecated("Use localePreferred property", ReplaceWith("localePreferred"))
    @JsonIgnore
    fun isLocalePreferred(): Boolean = localePreferred

    override fun toString(): String =
        if (_name == null) "ConceptNameId: $conceptNameId" else _name!!
}
