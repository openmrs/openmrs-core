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
public interface OpenmrsMetadataDAO<T extends BaseOpenmrsMetadata> extends OpenmrsObjectDAO<T> {
	
	/**
	 * Return a list of metadata objects (optionally retired)
	 * 
	 * @param includeRetired if true retired metadata is also returned
	 * @return a list of all metadata objects of the given class
	 */
	List<T> getAll(boolean includeRetired);
	
	/**
	 * Returns total number of persistents (optionally retired)
	 * @param includeVoided
	 * @return
	 */
	int getAllCount(boolean includeRetired);
	
	/**
	 * Return a lists of metadata objects optionally retired, with paging
	 * @param includeVoided
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	List<T> getAll(boolean includeRetired, Integer firstResult, Integer maxResults);
	
}
