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
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.api.context.Context
import org.openmrs.api.db.hibernate.search.SearchAnalysis
import org.openmrs.util.OpenmrsClassLoader
import org.openmrs.util.OpenmrsUtil
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.*

/**
 * A PersonAttribute is meant as way for implementations to add arbitrary information about a
 * user/patient to their database. PersonAttributes are essentially just key-value pairs. However,
 * the PersonAttributeType can be defined in such a way that the value portion of this
 * PersonAttribute is a foreign key to another database table (like to the location table, or
 * concept table). This gives a PersonAttribute the ability to link to any other part of the
 * database A Person can have zero to n PersonAttribute(s).
 *
 * @see org.openmrs.PersonAttributeType
 * @see org.openmrs.Attributable
 */
@Indexed
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class PersonAttribute() : BaseChangeableOpenmrsData(), Serializable, Comparable<PersonAttribute> {

    @DocumentId
    var personAttributeId: Int? = null

    @IndexedEmbedded(includeEmbeddedObjectId = true)
    @AssociationInverseSide(inversePath = [ObjectPath([PropertyValue(propertyName = "attributes")])])
    var person: Person? = null

    @IndexedEmbedded
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    var attributeType: PersonAttributeType? = null

    @FullTextField(name = "valuePhrase", analyzer = SearchAnalysis.PHRASE_ANALYZER)
    @FullTextField(name = "valueExact", analyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "valueStart", analyzer = SearchAnalysis.START_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    @FullTextField(name = "valueAnywhere", analyzer = SearchAnalysis.ANYWHERE_ANALYZER, searchAnalyzer = SearchAnalysis.EXACT_ANALYZER)
    var value: String? = null

    constructor(personAttributeId: Int?) : this() {
        this.personAttributeId = personAttributeId
    }

    /**
     * Constructor for creating a basic attribute
     *
     * @param type PersonAttributeType
     * @param value String
     */
    constructor(type: PersonAttributeType?, value: String?) : this() {
        this.attributeType = type
        this.value = value
    }

    /**
     * Shallow copy of this PersonAttribute. Does NOT copy personAttributeId
     *
     * @return a shallows copy of this
     */
    fun copy(): PersonAttribute = copyHelper(PersonAttribute())

    /**
     * The purpose of this method is to allow subclasses of PersonAttribute to delegate a portion of
     * their copy() method back to the superclass, in case the base class implementation changes.
     *
     * @param target a PersonAttribute that will have the state of this copied into it
     * @return Returns the PersonAttribute that was passed in, with state copied into it
     */
    protected fun copyHelper(target: PersonAttribute): PersonAttribute {
        target.person = person
        target.attributeType = attributeType
        target.value = value
        target.creator = creator
        target.dateCreated = dateCreated
        target.changedBy = changedBy
        target.dateChanged = dateChanged
        target.voidedBy = voidedBy
        target.voided = voided
        target.dateVoided = dateVoided
        target.voidReason = voidReason
        return target
    }

    /**
     * Compares this PersonAttribute object to the given otherAttribute. This method differs from
     * [equals] in that this method compares the inner fields of each attribute for
     * equality. Note: Null/empty fields on `otherAttribute` /will not/ cause a false value to be returned
     *
     * @param otherAttribute PersonAttribute with which to compare
     * @return boolean true/false whether or not they are the same attributes
     * Should return true if attributeType value and void status are the same
     */
    fun equalsContent(otherAttribute: PersonAttribute): Boolean {
        var returnValue = true

        // these are the methods to compare.
        val methods = arrayOf("getAttributeType", "getValue", "getVoided")

        val attributeClass = this.javaClass

        // loop over all of the selected methods and compare this and other
        for (methodAttribute in methods) {
            try {
                val method = attributeClass.getMethod(methodAttribute)

                val thisValue = method.invoke(this)
                val otherValue = method.invoke(otherAttribute)

                if (otherValue != null) {
                    returnValue = returnValue && otherValue == thisValue
                }

            } catch (e: NoSuchMethodException) {
                log.warn("No such method for comparison $methodAttribute", e)
            } catch (e: Exception) {
                log.error("Error while comparing attributes", e)
            }
        }

        return returnValue
    }

    /**
     * Will try to create an object of class 'PersonAttributeType.format'. If that implements
     * Attributable, hydrate(value) is called. Defaults to just returning getValue()
     *
     * @return hydrated object or getValue()
     * Should load class in format property
     * Should still load class in format property if not Attributable
     */
    fun getHydratedObject(): Any? {
        if (value == null) {
            return null
        }

        try {
            val c = OpenmrsClassLoader.getInstance().loadClass(attributeType?.format)
            try {
                val o = c.getDeclaredConstructor().newInstance()
                if (o is Attributable<*>) {
                    return o.hydrate(value)
                }
            } catch (e: InstantiationException) {
                // try to hydrate the object with the String constructor
                log.trace("Unable to call no-arg constructor for class: ${c.name}")
                return c.getConstructor(String::class.java).newInstance(value)
            }
        } catch (e: Exception) {
            // No need to warn if the input was blank
            if (StringUtils.isBlank(value)) {
                return null
            }

            log.warn("Unable to hydrate value: $value for type: $attributeType", e)
        }

        log.debug("Returning value: '$value'")
        return value
    }

    /**
     * Convenience method for voiding this attribute
     *
     * @param reason
     * Should set voided bit to true
     */
    fun voidAttribute(reason: String?) {
        voided = true
        voidedBy = Context.getAuthenticatedUser()
        voidReason = reason
        dateVoided = Date()
    }

    override fun toString(): String {
        val o = getHydratedObject()
        return when (o) {
            is Attributable<*> -> o.displayString
            null -> value ?: ""
            else -> o.toString()
        }
    }

    override fun getId(): Int? = personAttributeId

    override fun setId(id: Int?) {
        personAttributeId = id
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * Should return negative if other attribute is voided
     * Should return negative if other attribute has earlier date created
     * Should return negative if this attribute has lower attribute type than argument
     * Should return negative if other attribute has lower value
     * Should return negative if this attribute has lower attribute id than argument
     * Should not throw exception if attribute type is null
     * Note: this comparator imposes orderings that are inconsistent with equals
     */
    override fun compareTo(other: PersonAttribute): Int {
        return DefaultComparator().compare(this, other)
    }

    /**
     * Provides a default comparator.
     * @since 1.12
     */
    class DefaultComparator : Comparator<PersonAttribute>, Serializable {
        override fun compare(pa1: PersonAttribute, pa2: PersonAttribute): Int {
            var retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.attributeType, pa2.attributeType)
            if (retValue != 0) {
                return retValue
            }

            retValue = pa1.voided!!.compareTo(pa2.voided!!)
            if (retValue != 0) {
                return retValue
            }

            retValue = OpenmrsUtil.compareWithNullAsLatest(pa1.dateCreated, pa2.dateCreated)
            if (retValue != 0) {
                return retValue
            }

            retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.value, pa2.value)
            if (retValue != 0) {
                return retValue
            }

            return OpenmrsUtil.compareWithNullAsGreatest(pa1.personAttributeId, pa2.personAttributeId)
        }

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        private const val serialVersionUID = 11231211232111L
        private val log = LoggerFactory.getLogger(PersonAttribute::class.java)
    }
}
