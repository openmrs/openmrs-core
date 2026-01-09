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
import org.apache.commons.lang3.StringUtils.defaultString
import org.apache.commons.lang3.builder.EqualsBuilder
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.api.APIException
import org.openmrs.api.db.hibernate.search.SearchAnalysis
import org.openmrs.layout.name.NameSupport
import org.openmrs.layout.name.NameTemplate
import org.openmrs.util.OpenmrsConstants
import org.openmrs.util.OpenmrsUtil
import org.openmrs.util.compareWithNullAsLatest
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import java.io.Serializable
import java.util.*

/**
 * A Person can have zero to n PersonName(s).
 */
@Indexed
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class PersonName() : BaseChangeableOpenmrsData(), Serializable, Cloneable, Comparable<PersonName> {

    @DocumentId
    var personNameId: Int? = null

    @IndexedEmbedded(includeEmbeddedObjectId = true)
    @AssociationInverseSide(inversePath = [ObjectPath([PropertyValue(propertyName = "names")])])
    var person: Person? = null

    var preferred: Boolean? = false

    @FullTextField(name = "givenNameExact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "givenNameStart", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "givenNameAnywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "givenNameSoundex", analyzer = SearchAnalysis.SOUNDEX_ANALYZER)
    private var _givenName: String? = null

    var givenName: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) {
            OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME
        } else _givenName
        set(value) { _givenName = value }

    private var _prefix: String? = null
    var prefix: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) null else _prefix
        set(value) { _prefix = value }

    @FullTextField(name = "middleNameExact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "middleNameStart", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "middleNameAnywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "middleNameSoundex", analyzer = SearchAnalysis.SOUNDEX_ANALYZER)
    private var _middleName: String? = null

    var middleName: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) {
            OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME
        } else _middleName
        set(value) { _middleName = value }

    private var _familyNamePrefix: String? = null
    var familyNamePrefix: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) null else _familyNamePrefix
        set(value) { _familyNamePrefix = value }

    @FullTextField(name = "familyNameExact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyNameStart", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyNameAnywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyNameSoundex", analyzer = SearchAnalysis.SOUNDEX_ANALYZER)
    private var _familyName: String? = null

    var familyName: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) {
            OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME
        } else _familyName
        set(value) { _familyName = value }

    @FullTextField(name = "familyName2Exact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyName2Start", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyName2Anywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "familyName2Soundex", analyzer = SearchAnalysis.SOUNDEX_ANALYZER)
    private var _familyName2: String? = null

    var familyName2: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) null else _familyName2
        set(value) { _familyName2 = value }

    private var _familyNameSuffix: String? = null
    var familyNameSuffix: String?
        get() = if (OpenmrsConstants.OBSCURE_PATIENTS) null else _familyNameSuffix
        set(value) { _familyNameSuffix = value }

    var degree: String? = null

    constructor(personNameId: Int?) : this() {
        this.personNameId = personNameId
    }

    constructor(givenName: String?, middleName: String?, familyName: String?) : this() {
        this._givenName = givenName
        this._middleName = middleName
        this._familyName = familyName
    }

    /**
     * Compares this PersonName object to the given otherName. This method differs from
     * [equals] in that this method compares the inner fields of each name for
     * equality. Note: Null/empty fields on `otherName` /will not/ cause a false value to
     * be returned
     *
     * @param otherName PersonName with which to compare
     * @return boolean true/false whether or not they are the same names
     * Should return true if given middle and family name are equal
     */
    fun equalsContent(otherName: PersonName): Boolean {
        return EqualsBuilder()
            .append(defaultString(otherName.prefix), defaultString(prefix))
            .append(defaultString(otherName.givenName), defaultString(givenName))
            .append(defaultString(otherName.middleName), defaultString(middleName))
            .append(defaultString(otherName.familyNamePrefix), defaultString(familyNamePrefix))
            .append(defaultString(otherName.degree), defaultString(degree))
            .append(defaultString(otherName.familyName), defaultString(familyName))
            .append(defaultString(otherName.familyName2), defaultString(familyName2))
            .append(defaultString(otherName.familyNameSuffix), defaultString(familyNameSuffix))
            .isEquals
    }

    fun getPreferred(): Boolean = preferred ?: false

    fun setPreferred(preferred: Boolean?) {
        this.preferred = preferred
    }

    @Deprecated("as of 2.0, use getPreferred()")
    @JsonIgnore
    fun isPreferred(): Boolean = getPreferred()

    /**
     * Convenience method to get all the names of this PersonName and concatenating them together
     * with spaces in between. If any part of [getPrefix], [getGivenName],
     * [getMiddleName], etc are null, they are not included in the returned name
     *
     * @return all of the parts of this [PersonName] joined with spaces
     * Should not put spaces around an empty middle name
     */
    val fullName: String
        get() {
            val nameTemplate: NameTemplate? = try {
                NameSupport.getInstance().defaultLayoutTemplate
            } catch (ex: APIException) {
                log.warn("No name layout format set")
                null
            }

            if (nameTemplate != null) {
                return nameTemplate.format(this)
            }

            val temp = mutableListOf<String>()
            prefix?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
            givenName?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
            middleName?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }

            if (OpenmrsConstants.PERSON_NAME_FORMAT_LONG == format) {
                familyNamePrefix?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
                familyName?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
                familyName2?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
                familyNameSuffix?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
                degree?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
            } else {
                familyName?.takeIf { StringUtils.hasText(it) }?.let { temp.add(it) }
            }

            return StringUtils.collectionToDelimitedString(temp, " ").trim()
        }

    override fun toString(): String = fullName

    override fun getId(): Int? = personNameId

    override fun setId(id: Int?) {
        personNameId = id
    }

    /**
     * Note: this comparator imposes orderings that are inconsistent with equals.
     *
     * Should return negative if other name is voided
     * Should return negative if this name is preferred
     * Should return negative if other familyName is greater
     * Should return negative if other familyName2 is greater
     * Should return negative if other givenName is greater
     * Should return negative if other middleName is greater
     * Should return negative if other familynamePrefix is greater
     * Should return negative if other familyNameSuffix is greater
     * Should return negative if other dateCreated is greater
     */
    override fun compareTo(other: PersonName): Int {
        return DefaultComparator().compare(this, other)
    }

    /**
     * Provides a default comparator.
     * @since 1.12
     */
    class DefaultComparator : Comparator<PersonName>, Serializable {
        override fun compare(pn1: PersonName, pn2: PersonName): Int {
            var ret = pn1.voided!!.compareTo(pn2.voided!!)
            if (ret == 0) {
                ret = pn2.getPreferred().compareTo(pn1.getPreferred())
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.familyName, pn2.familyName)
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.familyName2, pn2.familyName2)
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.givenName, pn2.givenName)
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.middleName, pn2.middleName)
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.familyNamePrefix, pn2.familyNamePrefix)
            }
            if (ret == 0) {
                ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.familyNameSuffix, pn2.familyNameSuffix)
            }
            if (ret == 0 && pn1.dateCreated != null) {
                ret = pn1.dateCreated.compareWithNullAsLatest(pn2.dateCreated)
            }

            // if we've gotten this far, just check all name values. If they are
            // equal, leave the objects at 0. If not, arbitrarily pick retValue=1
            // and return that (they are not equal).
            if (ret == 0 && !pn1.equalsContent(pn2)) {
                ret = 1
            }

            return ret
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 4353L
        private val log = LoggerFactory.getLogger(PersonName::class.java)

        private var format: String = OpenmrsConstants.PERSON_NAME_FORMAT_SHORT

        @JvmStatic
        fun setFormat(format: String?) {
            this.format = if (StringUtils.isEmpty(format)) {
                OpenmrsConstants.PERSON_NAME_FORMAT_SHORT
            } else {
                format!!
            }
        }

        @JvmStatic
        fun getFormat(): String = format

        /**
         * Bitwise copy of the personName object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
         * PersonName.person object in this object AND the cloned object will point at the same person
         *
         * @return New PersonName object
         * Should copy every property of given personName
         */
        @JvmStatic
        fun newInstance(pn: PersonName?): PersonName {
            requireNotNull(pn) { "PersonName cannot be null" }

            return PersonName(pn.personNameId).apply {
                pn.givenName?.let { _givenName = it }
                pn.middleName?.let { _middleName = it }
                pn.familyName?.let { _familyName = it }
                pn.familyName2?.let { _familyName2 = it }
                pn.familyNamePrefix?.let { _familyNamePrefix = it }
                pn.familyNameSuffix?.let { _familyNameSuffix = it }
                pn.prefix?.let { _prefix = it }
                pn.degree?.let { degree = it }
                pn.voidReason?.let { voidReason = it }

                pn.dateChanged?.let { dateChanged = it.clone() as Date }
                pn.dateCreated?.let { dateCreated = it.clone() as Date }
                pn.dateVoided?.let { dateVoided = it.clone() as Date }

                pn.preferred?.let { preferred = it }
                pn.voided?.let { voided = it }

                person = pn.person
                voidedBy = pn.voidedBy
                changedBy = pn.changedBy
                creator = pn.creator
            }
        }
    }
}
