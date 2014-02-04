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
public interface OpenmrsDataDAO<T extends BaseOpenmrsData> extends OpenmrsObjectDAO<T> {
	
	/**
	 * Return a list of persistents (optionally voided)
	 * 
	 * @param includeVoided if true voided persistents are also returned
	 * @return a list of persistents of the given class
	 */
	List<T> getAll(boolean includeVoided);
	
	/**
	 * Returns total number of persistents (optionally voided)
	 * @param includeVoided
	 * @return
	 */
	int getAllCount(boolean includeVoided);
	
	/**
	 * Return a lists of persistents optionally voided, with paging
	 * @param includeVoided
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	List<T> getAll(boolean includeVoided, Integer firstResult, Integer maxResults);
	
}
