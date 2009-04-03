/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods for retrieving registered Serializer instances,
 * and for persisting/retrieving/deleting objects using serialization
 */
@Transactional
public interface SerializedObjectDAO {
	
	/**
	 * Retrieves a Serialized Object from the database
	 * @param type The class of the object to retrieve
	 * @param id The primary key id of the object to retrieve
	 * @return the saved object
	 * @throws DAOException
	 * @should return the saved object
	 */
	public <T extends OpenmrsObject> T getObject(Class<T> type, Integer id) throws DAOException;
	
	/**
	 * Saves a Serialized Object to the database
	 * @param object The object to save
	 * @return the saved object
	 * @throws DAOException
	 * @should save the passed object if supported
	 * @should throw an exception if object not supported
	 */
	public <T extends OpenmrsObject> T saveObject(T object) throws DAOException;
	
	/**
	 * Retrieves all non-retired/voided Serialized Objects from the database that match the passed Class<T> type
	 * @param type The class of the object to retrieve
	 * @return <List> T A list of all the saved objects that match the passed type
	 * @throws DAOException
	 * @should return all non-retired objects of the passed type
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type) throws DAOException;
	
	/**
	 * Retrieves all Serialized Objects from the database that match the passed Class<T> type
	 * Returns voided / retired Objects only if includeRetired parameter is true
	 * @param type The class of the object to retrieve
	 * @param includeRetired includeRetired If true, returns voided/retired objects as well
	 * @return <List> T A list of all the saved objects that match the passed type
	 * @throws DAOException
	 * @should return all saved objects of the passed type if includeRetired
	 * @should return only non-retired objects of the passed type if not includeRetired
	 */
	public <T extends OpenmrsObject> List<T> getAllObjects(Class<T> type, boolean includeRetired) throws DAOException;
	
	/**
	 * Retrieves all Serialized Objects from the database that match the passed Class<T> type and name
	 * @param type The class of the object to retrieve
	 * @param name the name of the item to retrieve
	 * @return <List> T A list of all the saved objects that match the passed type and name
	 * @throws DAOException
	 * @should return all saved objects with the given type and name
	 */
	public <T extends OpenmrsMetadata> List<T> getAllObjectsByName(Class<T> type, String name) throws DAOException;
	
	/**
	 * Deletes the item from the database with the given primary key id
	 * @param id The id of the item to delete from the database
	 * @throws DAOException
	 * @should delete the object with the passed id
	 */
	public void purgeObject(Integer id) throws DAOException;
	
	/**
	 * Returns the registered class for the passed object, or null if none found
	 * For example, if the supportedTypes property contains the CohortDefinition.class interface,
	 * and a particular implementation of that interface is passed in, then this method would 
	 * return CohortDefinition.class.
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
	 * @param clazz The class to register
	 */
	public void registerSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException;
	
	/**
	 * Removes this class as one that should be supported
	 * @param clazz The class to un-register
	 */
	public void unregisterSupportedType(Class<? extends OpenmrsObject> clazz) throws DAOException;
}
