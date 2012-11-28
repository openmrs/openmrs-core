package org.openmrs.api.db;

import java.util.List;

import org.openmrs.BaseOpenmrsData;

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides an interface for
 * standard DAO operations on data.
 * 
 * @since 1.10
 */
public interface OpenmrsDataDAO<T extends BaseOpenmrsData> {
	
	/**
	 * Obtains a persistent matching a given UUID
	 * 
	 * @param uuid
	 * @return the matching persistent
	 */
	public T getByUuid(String uuid);

	/**
	 * Return a list of persistents optionally voided
	 * 
	 * @param includeVoided if true voided persistents are also returned
	 * @return a list of persistents of the given class
	 */
	public List<T> getAll(boolean includeVoided) ;

	/**
	 * Completely deletes a persistent from the database
	 * 
	 * @param persistent The persistent to delete
	 */
	public void delete(T persistent) ;

	/**
	 * Save or update the given persistent in the database
	 * 
	 * @param persistent The persistent to save or update
	 * @return the persistent that was saved or updated
	 */
	public T saveOrUpdate(T persistent) ;

}

