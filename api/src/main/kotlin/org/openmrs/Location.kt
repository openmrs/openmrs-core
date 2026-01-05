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

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Cacheable
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.openmrs.annotation.Independent
import org.openmrs.api.APIException
import org.openmrs.api.context.Context
import java.io.Serializable

/**
 * A Location is a physical place, such as a hospital, a room, a clinic, or a district. Locations
 * support a single hierarchy, such that each location may have one parent location. A
 * non-geographical grouping of locations, such as "All Community Health Centers" is not a location,
 * and should be modeled using [LocationTag]s.
 *
 * Note: Prior to version 1.9 this class extended BaseMetadata
 */
@Entity
@Table(name = "location")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AttributeOverride(name = "attributes", column = Column(name = "location_id"))
@Audited
class Location() : BaseCustomizableMetadata<LocationAttribute>(), Serializable, Attributable<Location>, Address {

    companion object {
        const val serialVersionUID: Long = 455634L
        const val LOCATION_UNKNOWN: Int = 1

        /**
         * Checks whether 'location' is a member of the tree starting at 'root'.
         *
         * @param location The location to be tested.
         * @param root Location node from which to start the testing (down in the hierarchy).
         * @return true if location is in the hierarchy starting at root
         * @since 1.5
         */
        @JvmStatic
        fun isInHierarchy(location: Location?, root: Location?): Boolean {
            if (root == null) return false
            var current = location
            while (current != null) {
                if (root == current) return true
                current = current.parentLocation
            }
            return false
        }
    }

    @Id
    @Column(name = "location_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var locationId: Int? = null

    @ManyToOne
    @JoinColumn(name = "location_type_concept_id")
    var type: Concept? = null

    @Column(name = "address1")
    override var address1: String? = null

    @Column(name = "address2")
    override var address2: String? = null

    @Column(name = "city_village")
    override var cityVillage: String? = null

    @Column(name = "state_province")
    override var stateProvince: String? = null

    @Column(name = "country", length = 50)
    override var country: String? = null

    @Column(name = "postal_code", length = 50)
    override var postalCode: String? = null

    @Column(name = "latitude", length = 50)
    override var latitude: String? = null

    @Column(name = "longitude", length = 50)
    override var longitude: String? = null

    @Column(name = "county_district")
    override var countyDistrict: String? = null

    @Column(name = "address3")
    override var address3: String? = null

    @Column(name = "address4")
    override var address4: String? = null

    @Column(name = "address5")
    override var address5: String? = null

    @Column(name = "address6")
    override var address6: String? = null

    @Column(name = "address7")
    override var address7: String? = null

    @Column(name = "address8")
    override var address8: String? = null

    @Column(name = "address9")
    override var address9: String? = null

    @Column(name = "address10")
    override var address10: String? = null

    @Column(name = "address11")
    override var address11: String? = null

    @Column(name = "address12")
    override var address12: String? = null

    @Column(name = "address13")
    override var address13: String? = null

    @Column(name = "address14")
    override var address14: String? = null

    @Column(name = "address15")
    override var address15: String? = null

    @ManyToOne
    @JoinColumn(name = "parent_location")
    var parentLocation: Location? = null

    @OneToMany(mappedBy = "parentLocation", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 100)
    @OrderBy("name")
    var childLocations: MutableSet<Location>? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "location_tag_map",
        joinColumns = [JoinColumn(name = "location_id")],
        inverseJoinColumns = [JoinColumn(name = "location_tag_id")]
    )
    @Independent
    var tags: MutableSet<LocationTag>? = null

    /** Constructor with id */
    constructor(locationId: Int?) : this() {
        this.locationId = locationId
    }

    override var id: Integer?
        get() = locationId?.let { Integer(it) }
        set(value) { locationId = value?.toInt() }

    override fun toString(): String = name ?: id?.toString() ?: ""

    /**
     * Returns all childLocations where child.locationId = this.locationId.
     *
     * @param includeRetired specifies whether or not to include voided childLocations
     * @return a Set of all the childLocations
     * @since 1.5
     */
    fun getChildLocations(includeRetired: Boolean): Set<Location> =
        if (includeRetired) {
            childLocations ?: emptySet()
        } else {
            childLocations?.filterNot { it.retired == true }?.toSet() ?: emptySet()
        }

    /**
     * Returns the descendant locations.
     *
     * @param includeRetired specifies whether or not to include voided childLocations
     * @return a Set of the descendant locations
     * @since 1.10
     */
    fun getDescendantLocations(includeRetired: Boolean): Set<Location> {
        val result = mutableSetOf<Location>()
        childLocations?.forEach { child ->
            if (child.retired != true || includeRetired) {
                result.add(child)
                result.addAll(child.getDescendantLocations(includeRetired))
            }
        }
        return result
    }

    /**
     * Adds a child location.
     *
     * @param child The child location to add.
     * @throws APIException if the child is the same as this location or already in the hierarchy
     * @since 1.5
     */
    fun addChildLocation(child: Location?) {
        child ?: return

        if (childLocations == null) {
            childLocations = mutableSetOf()
        }

        if (child == this) {
            throw APIException("Location.cannot.be.its.own.child", null as Array<Any>?)
        }

        // Traverse all the way up to the root, then check whether the child is already in the tree
        var root: Location = this
        while (root.parentLocation != null) {
            root = root.parentLocation!!
        }

        if (isInHierarchy(child, root)) {
            throw APIException("Location.hierarchy.loop", arrayOf(child, this))
        }

        child.parentLocation = this
        childLocations!!.add(child)
    }

    /**
     * Removes a child location.
     *
     * @param child The child location to remove.
     * @since 1.5
     */
    fun removeChildLocation(child: Location) {
        childLocations?.remove(child)
    }

    /**
     * Attaches a tag to the Location.
     *
     * @param tag The tag to add.
     * @since 1.5
     */
    fun addTag(tag: LocationTag?) {
        if (tag == null) return
        if (tags == null) {
            tags = mutableSetOf()
        }
        if (tag !in tags!!) {
            tags!!.add(tag)
        }
    }

    /**
     * Removes a tag from the Location.
     *
     * @param tag The tag to remove.
     * @since 1.5
     */
    fun removeTag(tag: LocationTag) {
        tags?.remove(tag)
    }

    /**
     * Checks whether the Location has a particular tag.
     *
     * @param tagToFind the string of the tag for which to check
     * @return true if the tags include the specified tag, false otherwise
     * @since 1.5
     */
    fun hasTag(tagToFind: String?): Boolean =
        tagToFind != null && tags?.any { it.name == tagToFind } == true

    // Attributable implementation

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun findPossibleValues(searchText: String): List<Location> =
        runCatching { Context.getLocationService().getLocations(searchText) }
            .getOrDefault(emptyList())

    @Deprecated("Data provided by this method can be better achieved from appropriate service at point of use")
    override fun getPossibleValues(): List<Location> =
        runCatching { Context.getLocationService().getAllLocations() }
            .getOrDefault(emptyList())

    override fun hydrate(s: String): Location =
        runCatching { Context.getLocationService().getLocation(s.toInt()) ?: Location() }
            .getOrDefault(Location())

    override fun serialize(): String = locationId?.toString() ?: ""

    override val displayString: String?
        get() = name
}
