package org.openmrs.api.db;

import java.util.List;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides an interface for
 * standard DAO operations on metadata.
 * 
 * @since 1.10
 */
public interface OpenmrsMetadataDAO<T extends BaseOpenmrsMetadata> {
	
	/**
	 * Obtains a metadata object matching a given identifier
	 * 
	 * @param id the metadata identifier
	 * @return the matching metadata object
	 */
	public T getById(Integer id) ;

	/**
	 * Obtains a metadata object matching a given UUID
	 * 
	 * @param uuid
	 * @return the matching metadata object
	 */
	public T getByUuid(String uuid);

	/**
	 * Return a list of metadata objects optionally retired
	 * 
	 * @param includeRetired if true retired metadata is also returned
	 * @return a list of all metadata objects of the given class
	 */
	public List<T> getAll(boolean includeRetired) ;

	/**
	 * Completely deletes a metadata object from the database
	 * 
	 * @param metadata The metadata object to delete
	 */
	public void delete(T metadata) ;

	/**
	 * Save or update the given metadata object in the database
	 * 
	 * @param metadata The metadata object to save or update
	 * @return the metadata object that was saved or updated
	 */
	public T saveOrUpdate(T metadata) ;

}

