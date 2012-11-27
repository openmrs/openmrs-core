package org.openmrs.api.db;

import java.util.List;

import org.openmrs.BaseOpenmrsMetadata;

public interface OpenmrsDAO {

	/**
	 * Obtains a metadata object matching a given identifier
	 * 
	 * @param clazz the persistent class
	 * @param id 	the metadata identifier
	 * @return the matching metadata object
	 */
	public <T extends BaseOpenmrsMetadata> T getMetadata(Class<T> clazz, Integer id) ;

	/**
	 * Obtains a metadata object matching a given UUID
	 * 
	 * @param clazz the persistent class
	 * @param uuid
	 * @return the matching metadata object
	 */
	public <T extends BaseOpenmrsMetadata> T getMetadataByUuid(Class<T> clazz, String uuid);

	/**
	 * Return a list of metadata objects optionally retired
	 * 
	 * @param clazz the persistent class
	 * @param includeRetired if true retired metadata is also returned
	 * @return a list of all metadata objects of the given class
	 */
	public <T extends BaseOpenmrsMetadata> List<T> getAllMetadata(Class<T> clazz, boolean includeRetired) ;

	/**
	 * Completely deletes a metadata object from the database
	 * 
	 * @param metadata The metadata object to delete
	 */
	public <T extends BaseOpenmrsMetadata> void deleteMetadata(T metadata) ;

	/**
	 * Save or update the given metadata object in the database
	 * 
	 * @param metadata The metadata object to save or update
	 * @return the metadata object that was saved or updated
	 */
	public <T extends BaseOpenmrsMetadata> T saveMetadata(T metadata) ;

}
