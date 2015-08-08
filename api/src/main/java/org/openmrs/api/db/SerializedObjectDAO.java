/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.serialization.OpenmrsSerializer;

/**
 * The SerializedObjectDAO is meant to be used as a means for persisting objects for which a typical
 * relational table model is impractical. A typical example is for persisting multiple different
 * user-configurable implementations of a particular interface. Because it is impossible to know
 * what properties a given implementation will have and which will need to be persisted, it might be
 * more practical to use serialization for this. Each available method for managing Serialized
 * Objects on this class is available in two forms. The first form operates on OpenmrsObject
 * instances directly, and isolates the consumer completely from the mechanics of Serialization. You
 * pass in OpenmrsObjects and you get out OpenmrsObjects. For example:
 * 
 * <pre>
 * MyOpenmrsObject m = getObject(MyOpenmrsObject.class, 10);
 * </pre>
 * 
 * The second form operates on SerializedObject instances directly, and provides the consumer with
 * more control over how to handle these SerializedObjects. A typical reason why this might be
 * useful is to provide graceful failure in the event that a persisted Object has had an API change,
 * and thus would fail to deserialize properly. In this case, the consumer can use something like
 * the following:
 * 
 * <pre>
 * MyOpenmrsObject m = null;
 * SerializedObject s = getSerializedObject(10);
 * try {
 *     m = convertSerializedObject(MyOpenmrsObject.class, s);
 * }
 * catch (Exception e) {
 *     // Handle this exception however you need to for your use case.
 * }
 * </pre>
 * 
 * @since 1.5
 */
public interface SerializedObjectDAO {
	
	/**
	 * Retrieves the raw SerializedObject from the database by id
	 * 
	 * @param id the id to lookup
	 * @return the SerializedObject with the given id
	 * @throws DAOException
	 * @should return the saved serialized object
	 */
	public SerializedObject getSerializedObject(Integer id) throws DAOException;
	
	/**
	 * Retrieves the saved object of the passed type from the database by it's id
	 * 
	 * @param type The class of the object to retrieve
	 * @param id The primary key id of the object to retrieve
	 * @return the saved object
	 * @throws DAOException
	 * @should return the saved object
	 */
	public <T extends OpenmrsObject> T getObject(Class<T> type, Integer id) throws DAOException;
	
	/**
	 * Retrieves the raw Serialized Object from the database by uuid
	 * 
	 * @param uuid The UUID of the object to retrieve
	 * @return the SerializedObject with the given uuid
	 * @throws DAOException
	 * @should return the saved serialized object
	 */
	public SerializedObject getSerializedObjectByUuid(String uuid) throws DAOException;
	
	/**
	 * Retrieves the saved object of the passed type from the database by it's uuid
	 * 
	 * @param type The class of the object to retrieve
	 * @param uuid The UUID of the object to retrieve
	 * @return the saved object
	 * @throws DAOException
	 * @should return the saved object
	 */
	public <T extends OpenmrsObject> T getObjectByUuid(Class<T> type, String uuid) throws DAOException;
	
	/**
	 * Saves an object to the database in serialized form
	 * 
	 * @param object The object to save
	 * @return the saved object
	 * @throws DAOException
	 * @should save the passed object if supported
	 * @should throw an exception if object not supported
	 */
	public <T extends OpenmrsObject> T saveObject(T object) throws DAOException;
	
	/**
	 * Saves an object to the database, in serialized form, using the specified
	 * {@link OpenmrsSerializer}
	 * 
	 * @param object The object to save
	 * @param serializer The {@link OpenmrsSerializer} to use
	 * @return the saved object
	 * @throws DAOException
	 * @should save the passed object if supported
	 * @should throw an exception if object not supported
	 * @should set auditable fields before serializing
	 */
	public <T extends OpenmrsObject> T saveObject(T object, OpenmrsSerializer serializer) throws DAOException;
	
	/**
	 * Retrieves all raw Serialized Object from the database that match the passed type and
	 * includeRetired flag
	 * 
	 * @param type The class of the object to retrieve
	 * @param includeRetired if true includes retired/voided objects, otherwise does not
	 * @return &lt;List&gt; T A list of all the saved objects that match the passed type
	 * @throws DAOException
	 * @should return all objects of the passed type
	 */
	public List<SerializedObject> getAllSerializedObjects(Class<?> type, boolean includeRetired) throws DAOException;
	
	/**
	 * Retrieves all non-retired objects of the passed type from the database that have been saved
	 * through serialization
	 * 
	 * @param type The class of the object to retrieve
	 * @return &lt;List&gt; T A list of all the saved objects that match the passed type
	 * @throws DAOException
	 * @should return all non-retired objects of the passed type
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type) throws DAOException;
	
	/**
	 * Retrieves all objects from the database that match the passed type that have been saved
	 * through serialization Returns voided / retired Objects only if includeRetired parameter is
	 * true
	 * 
	 * @param type The class of the object to retrieve
	 * @param includeRetired includeRetired If true, returns voided/retired objects as well
	 * @return &lt;List&gt; T A list of all the saved objects that match the passed type
	 * @throws DAOException
	 * @should return all saved objects of the passed type if includeRetired
	 * @should return only non-retired objects of the passed type if not includeRetired
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type, boolean includeRetired) throws DAOException;
	
	/**
	 * Retrieves all raw Serialized Objects from the database that match the passed type and name
	 * 
	 * @param type The class of the object to retrieve
	 * @param name the name of the item to retrieve
	 * @param exactMatchOnly if true will only return exact matches
	 * @return &lt;List&gt; T A list of all the saved objects that match the passed type and name
	 * @throws DAOException
	 * @should return all saved objects with the given type and exact name
	 * @should return all saved objects with the given type and partial name
	 */
	public List<SerializedObject> getAllSerializedObjectsByName(Class<?> type, String name, boolean exactMatchOnly)
	        throws DAOException;
	
	/**
	 * Retrieves all objects from the database that match the passed type and name that have been
	 * saved through serialization
	 * 
	 * @param type The class of the object to retrieve
	 * @param name the name of the item to retrieve
	 * @param exactMatchOnly if true will only return exact matches
	 * @return &lt;List&gt; T A list of all the saved objects that match the passed type and name
	 * @throws DAOException
	 * @should return all saved objects with the given type and exact name
	 * @should return all saved objects with the given type and partial name
	 */
	public <T extends OpenmrsMetadata> List<T> getAllObjectsByName(Class<T> type, String name, boolean exactMatchOnly)
	        throws DAOException;
	
	/**
	 * Converts a raw SerializedObject to an OpenmrsObject, using the appropriate Serializer
	 * 
	 * @param clazz the OpenmrsObject class to retrieve
	 * @param serializedObject the raw SerializedObject to deserialize into an OpenmrsObject
	 * @return an OpenmrsObject of the passed clazz from the passed SerializedObject
	 * @throws DAOException
	 */
	public <T extends OpenmrsObject> T convertSerializedObject(Class<T> clazz, SerializedObject serializedObject)
	        throws DAOException;
	
	/**
	 * Deletes the item from the database with the given primary key id
	 * 
	 * @param id The id of the item to delete from the database
	 * @throws DAOException
	 * @should delete the object with the passed id
	 */
	public void purgeObject(Integer id) throws DAOException;
	
	/**
	 * Returns the registered class for the passed object, or null if none found For example, if the
	 * supportedTypes property contains the CohortDefinition.class interface, and a particular
	 * implementation of that interface is passed in, then this method would return
	 * CohortDefinition.class.
	 * 
	 * @param object The object to check for the registered type
	 * @return The registered type for the passed object, or null if not found
	 */
	public Class<? extends OpenmrsObject> getRegisteredTypeForObject(OpenmrsObject object);
	
	/**
	 * @return all supported types that this class can manage
	 */
	public List<Class<? extends OpenmrsObject>> getSupportedTypes();
	
	/**
	 * Registers a class as one that should be supported
	 * 
	 * @param clazz The class to register
	 */
	public void registerSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException;
	
	/**
	 * Removes this class as one that should be supported
	 * 
	 * @param clazz The class to un-register
	 */
	public void unregisterSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException;
}
