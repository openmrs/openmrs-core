/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Address
import org.openmrs.Location
import org.openmrs.LocationAttribute
import org.openmrs.LocationAttributeType
import org.openmrs.LocationTag
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.LocationDAO
import org.openmrs.util.PrivilegeConstants

/**
 * API methods for managing Locations.
 *
 * Example Usage:
 * ```
 * val locations = Context.getLocationService().getAllLocations()
 * ```
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Location
 */
interface LocationService : OpenmrsService {

    /**
     * Set the data access object that the service will use to interact with the database. This is
     * set by spring in the applicationContext-service.xml file.
     *
     * @param dao the DAO to set
     */
    fun setLocationDAO(dao: LocationDAO)

    /**
     * Save location to database (create if new or update if changed).
     *
     * @param location is the location to be saved to the database
     * @return the saved location
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
    @Throws(APIException::class)
    fun saveLocation(location: Location): Location

    /**
     * Returns a location given that locations primary key locationId. A null value is
     * returned if no location exists with this location.
     *
     * @param locationId integer primary key of the location to find
     * @return Location object that has location.locationId = locationId passed in.
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocation(locationId: Int?): Location?

    /**
     * Returns a location given the location's exact name. A null value is returned if
     * there is no location with this name.
     *
     * @param name the exact name of the location to match on
     * @return Location matching the name to Location.name
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocation(name: String): Location?

    /**
     * Returns the default location for this implementation.
     *
     * @return The default location for this implementation.
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getDefaultLocation(): Location?

    /**
     * Returns a location by uuid.
     *
     * @param uuid is the uuid of the desired location
     * @return location with the given uuid
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationByUuid(uuid: String): Location?

    /**
     * Returns a location tag by uuid.
     *
     * @param uuid is the uuid of the desired location tag
     * @return location tag with the given uuid
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationTagByUuid(uuid: String): LocationTag?

    /**
     * Returns all locations, includes retired locations. This method delegates to the
     * [getAllLocations] method.
     *
     * @return locations that are in the database
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getAllLocations(): List<Location>

    /**
     * Returns all locations.
     *
     * @param includeRetired whether or not to include retired locations
     * @return all locations
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getAllLocations(includeRetired: Boolean): List<Location>

    /**
     * Returns locations that match the beginning of the given string. A null list will never be
     * returned. An empty list will be returned if there are no locations. Search is case
     * insensitive.
     *
     * @param nameFragment is the string used to search for locations
     * @return matching locations
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocations(nameFragment: String?): List<Location>

    /**
     * Gets the locations matching the specified arguments. A null list will never be returned. An empty list will be
     * returned if there are no locations. Search is case insensitive. If start and length are not specified, then all
     * matches are returned.
     *
     * @param nameFragment is the string used to search for locations
     * @param parent only return children of this parent
     * @param attributeValues the attribute values
     * @param includeRetired specifies if retired locations should also be returned
     * @param start the beginning index
     * @param length the number of matching locations to return
     * @return the list of locations
     * @throws APIException if retrieval fails
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocations(
        nameFragment: String?,
        parent: Location?,
        attributeValues: @JvmSuppressWildcards Map<LocationAttributeType, Any>?,
        includeRetired: Boolean,
        start: Int?,
        length: Int?
    ): List<Location>

    /**
     * Returns locations that contain the given tag.
     *
     * @param tag LocationTag criterion
     * @return matching locations
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationsByTag(tag: LocationTag): List<Location>

    /**
     * Returns locations that are mapped to all given tags.
     *
     * @param tags Set of LocationTag criteria
     * @return matching locations
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationsHavingAllTags(tags: @JvmSuppressWildcards List<LocationTag>): List<Location>

    /**
     * Returns locations that are mapped to any of the given tags.
     *
     * @param tags Set of LocationTag criteria
     * @return matching locations
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationsHavingAnyTag(tags: @JvmSuppressWildcards List<LocationTag>): List<Location>

    /**
     * Retires the given location. This effectively removes the location from circulation or use.
     *
     * @param location location to be retired
     * @param reason is the reason why the location is being retired
     * @return the retired location
     * @throws APIException if retirement fails
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
    @Throws(APIException::class)
    fun retireLocation(location: Location, reason: String): Location

    /**
     * Unretire the given location. This restores a previously retired location back into
     * circulation and use.
     *
     * @param location the location to unretire
     * @return the newly unretired location
     * @throws APIException if unretirement fails
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
    @Throws(APIException::class)
    fun unretireLocation(location: Location): Location

    /**
     * Completely remove a location from the database (not reversible). This method delegates to
     * [purgeLocation] method.
     *
     * @param location the Location to clean out of the database.
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_LOCATIONS)
    @Throws(APIException::class)
    fun purgeLocation(location: Location)

    /**
     * Save location tag to database (create if new or update if changed).
     *
     * @param tag is the tag to be saved to the database
     * @return the saved tag
     * @throws APIException if saving fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_TAGS)
    @Throws(APIException::class)
    fun saveLocationTag(tag: LocationTag): LocationTag

    /**
     * Returns a location tag given that locations primary key locationTagId. A null
     * value is returned if no tag exists with this ID.
     *
     * @param locationTagId integer primary key of the location tag to find
     * @return LocationTag object that has LocationTag.locationTagId = locationTagId passed in.
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationTag(locationTagId: Int?): LocationTag?

    /**
     * Returns a location tag given the location's exact name (tag). A null value is returned if
     * there is no tag with this name.
     *
     * @param tag the exact name of the tag to match on
     * @return LocationTag matching the name to LocationTag.tag
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationTagByName(tag: String): LocationTag?

    /**
     * Returns all location tags, includes retired location tags. This method delegates to the
     * [getAllLocationTags] method.
     *
     * @return location tags that are in the database
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getAllLocationTags(): List<LocationTag>

    /**
     * Returns all location tags.
     *
     * @param includeRetired whether or not to include retired location tags
     * @return all location tags
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getAllLocationTags(includeRetired: Boolean): List<LocationTag>

    /**
     * Returns location tags that match the beginning of the given string. A null list will never be
     * returned. An empty list will be returned if there are no tags. Search is case insensitive.
     *
     * @param search is the string used to search for tags
     * @return matching location tags
     * @throws APIException if retrieval fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getLocationTags(search: String?): List<LocationTag>

    /**
     * Retire the given location tag. This effectively removes the tag from circulation or use.
     *
     * @param tag location tag to be retired
     * @param reason is the reason why the location tag is being retired
     * @return the retired location tag
     * @throws APIException if retirement fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_TAGS)
    @Throws(APIException::class)
    fun retireLocationTag(tag: LocationTag, reason: String): LocationTag

    /**
     * Unretire the given location tag. This restores a previously retired tag back into circulation
     * and use.
     *
     * @param tag the location tag to unretire
     * @return the newly unretired location tag
     * @throws APIException if unretirement fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_TAGS)
    @Throws(APIException::class)
    fun unretireLocationTag(tag: LocationTag): LocationTag

    /**
     * Completely remove a location tag from the database (not reversible).
     *
     * @param tag the LocationTag to clean out of the database.
     * @throws APIException if purging fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.PURGE_LOCATION_TAGS)
    @Throws(APIException::class)
    fun purgeLocationTag(tag: LocationTag)

    /**
     * Return the number of all locations that start with the given name fragment, if the name
     * fragment is null or an empty string, then the number of all locations will be returned.
     *
     * @param nameFragment is the string used to search for locations
     * @param includeRetired Specifies if retired locations should be counted or ignored
     * @return the number of all locations starting with the given nameFragment
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    fun getCountOfLocations(nameFragment: String?, includeRetired: Boolean?): Integer

    /**
     * Returns all root locations (i.e. those who have no parentLocation), optionally including
     * retired ones.
     *
     * @param includeRetired whether to include retired locations
     * @return return all root locations depends on includeRetired
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    fun getRootLocations(includeRetired: Boolean): List<Location>

    /**
     * Given an Address object, returns all the possible values for the specified AddressField. This
     * method is not implemented in core, but is meant to overridden by implementing modules such as
     * the Address Hierarchy module.
     *
     * @param incomplete the incomplete address
     * @param fieldName the address field we are looking for possible values for
     * @return a list of possible address values for the specified field
     * @throws APIException if retrieval fails
     * @since 1.7.2
     */
    @Throws(APIException::class)
    fun getPossibleAddressValues(incomplete: Address, fieldName: String): List<String>?

    /**
     * Returns the xml of default address template.
     *
     * @return a string value of the default address template. If the GP is empty, the default
     *         template is returned
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    @Throws(APIException::class)
    fun getAddressTemplate(): String

    /**
     * Saved default address template to global properties.
     *
     * @param xml is a string to be saved as address template
     * @throws APIException if saving fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_ADDRESS_TEMPLATES)
    @Throws(APIException::class)
    fun saveAddressTemplate(xml: String)

    /**
     * Gets all location attribute types.
     *
     * @return all LocationAttributeTypes
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES)
    fun getAllLocationAttributeTypes(): List<LocationAttributeType>

    /**
     * Gets a location attribute type by id.
     *
     * @param id the location attribute type id
     * @return the LocationAttributeType with the given internal id
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES)
    fun getLocationAttributeType(id: Int?): LocationAttributeType?

    /**
     * Gets a location attribute type by uuid.
     *
     * @param uuid the location attribute type uuid
     * @return the LocationAttributeType with the given uuid
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES)
    fun getLocationAttributeTypeByUuid(uuid: String): LocationAttributeType?

    /**
     * Creates or updates the given location attribute type in the database.
     *
     * @param locationAttributeType the location attribute type to save
     * @return the LocationAttributeType created/saved
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_ATTRIBUTE_TYPES)
    fun saveLocationAttributeType(locationAttributeType: LocationAttributeType): LocationAttributeType

    /**
     * Retires the given location attribute type in the database.
     *
     * @param locationAttributeType the location attribute type to retire
     * @param reason the reason for retirement
     * @return the locationAttribute retired
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_ATTRIBUTE_TYPES)
    fun retireLocationAttributeType(locationAttributeType: LocationAttributeType, reason: String): LocationAttributeType

    /**
     * Restores a location attribute type that was previous retired in the database.
     *
     * @param locationAttributeType the location attribute type to unretire
     * @return the LocationAttributeType unretired
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_LOCATION_ATTRIBUTE_TYPES)
    fun unretireLocationAttributeType(locationAttributeType: LocationAttributeType): LocationAttributeType

    /**
     * Completely removes a location attribute type from the database.
     *
     * @param locationAttributeType the location attribute type to purge
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.PURGE_LOCATION_ATTRIBUTE_TYPES)
    fun purgeLocationAttributeType(locationAttributeType: LocationAttributeType)

    /**
     * Gets a location attribute by uuid.
     *
     * @param uuid the location attribute uuid
     * @return the LocationAttribute with the given uuid
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    fun getLocationAttributeByUuid(uuid: String): LocationAttribute?

    /**
     * Retrieves a LocationAttributeType object based on the name provided.
     *
     * @param locationAttributeTypeName the location attribute type name
     * @return the LocationAttributeType with the specified name
     * @since 1.10.0
     */
    @Authorized(PrivilegeConstants.GET_LOCATION_ATTRIBUTE_TYPES)
    fun getLocationAttributeTypeByName(locationAttributeTypeName: String): LocationAttributeType?
}
