/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.parameter;

import java.util.Collection;

/**
 * The ConceptSearchCriteria class encapsulates the criteria used for searching concepts in the
 * system. It supports searching by UUIDs, integer IDs, and concept mapping (code + source name).
 * Multiple criteria are combined with OR semantics — a concept matching any one criterion is
 * returned. The {@code includeRetired} flag is applied as a global AND filter on top of those
 * results.
 * <p>
 * Instead of calling the constructor directly, use {@link ConceptSearchCriteriaBuilder}.
 *
 * @since 2.8.0
 */
public class ConceptSearchCriteria {

	private Collection<String> uuids;

	private Collection<Integer> conceptIds;

	private String mappingCode;

	private String mappingSourceName;

	private boolean includeRetired = false;

	/**
	 * Constructs a ConceptSearchCriteria with the specified parameters. Instead of calling this
	 * constructor directly, it is recommended to use {@link ConceptSearchCriteriaBuilder}.
	 *
	 * @param uuids collection of concept UUIDs to search for
	 * @param conceptIds collection of concept integer IDs to search for
	 * @param mappingCode the code associated with a concept within a given concept source
	 * @param mappingSourceName the name or hl7Code of the concept source to check
	 * @param includeRetired whether to include retired concepts in the results
	 */
	public ConceptSearchCriteria(Collection<String> uuids, Collection<Integer> conceptIds, String mappingCode,
	    String mappingSourceName, boolean includeRetired) {
		this.uuids = uuids;
		this.conceptIds = conceptIds;
		this.mappingCode = mappingCode;
		this.mappingSourceName = mappingSourceName;
		this.includeRetired = includeRetired;
	}

	/**
	 * @return the collection of UUIDs included in the search criteria.
	 */
	public Collection<String> getUuids() {
		return uuids;
	}

	/**
	 * Sets the collection of UUIDs to be included in the search criteria.
	 *
	 * @param uuids the collection of UUIDs to set.
	 */
	public void setUuids(Collection<String> uuids) {
		this.uuids = uuids;
	}

	/**
	 * @return the collection of concept integer IDs included in the search criteria.
	 */
	public Collection<Integer> getConceptIds() {
		return conceptIds;
	}

	/**
	 * Sets the collection of concept integer IDs to be included in the search criteria.
	 *
	 * @param conceptIds the collection of concept IDs to set.
	 */
	public void setConceptIds(Collection<Integer> conceptIds) {
		this.conceptIds = conceptIds;
	}

	/**
	 * @return the mapping code included in the search criteria.
	 */
	public String getMappingCode() {
		return mappingCode;
	}

	/**
	 * Sets the mapping code to be included in the search criteria.
	 *
	 * @param mappingCode the mapping code to set.
	 */
	public void setMappingCode(String mappingCode) {
		this.mappingCode = mappingCode;
	}

	/**
	 * @return the mapping source name included in the search criteria.
	 */
	public String getMappingSourceName() {
		return mappingSourceName;
	}

	/**
	 * Sets the mapping source name to be included in the search criteria.
	 *
	 * @param mappingSourceName the mapping source name to set.
	 */
	public void setMappingSourceName(String mappingSourceName) {
		this.mappingSourceName = mappingSourceName;
	}

	/**
	 * @return true if retired concepts are included in the search results, false otherwise.
	 */
	public boolean isIncludeRetired() {
		return includeRetired;
	}

	/**
	 * Sets whether retired concepts should be included in the search results.
	 *
	 * @param includeRetired true to include retired concepts, false otherwise.
	 */
	public void setIncludeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
	}
}
