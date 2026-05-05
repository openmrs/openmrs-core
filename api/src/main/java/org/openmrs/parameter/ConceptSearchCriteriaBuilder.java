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
 * A builder class for constructing instances of {@link ConceptSearchCriteria}. This builder allows
 * for the flexible creation of {@link ConceptSearchCriteria} objects by providing a simple
 * interface to set various fields.
 *
 * @since 2.8.0
 */
public class ConceptSearchCriteriaBuilder {

	private Collection<String> uuids;

	private Collection<Integer> conceptIds;

	private String mappingCode;

	private String mappingSourceName;

	private boolean includeRetired = false;

	/**
	 * Constructs a new {@link ConceptSearchCriteriaBuilder} instance.
	 */
	public ConceptSearchCriteriaBuilder() {
	}

	/**
	 * Sets the UUIDs to search for.
	 *
	 * @param uuids the collection of UUIDs to include.
	 * @return the current instance of {@link ConceptSearchCriteriaBuilder} for method chaining.
	 */
	public ConceptSearchCriteriaBuilder uuids(Collection<String> uuids) {
		this.uuids = uuids;
		return this;
	}

	/**
	 * Sets the concept integer IDs to search for.
	 *
	 * @param conceptIds the collection of concept IDs to include.
	 * @return the current instance of {@link ConceptSearchCriteriaBuilder} for method chaining.
	 */
	public ConceptSearchCriteriaBuilder conceptIds(Collection<Integer> conceptIds) {
		this.conceptIds = conceptIds;
		return this;
	}

	/**
	 * Sets the concept mapping code to search for. Must be used together with
	 * {@link #mappingSourceName(String)}.
	 *
	 * @param mappingCode the code associated with a concept within a given concept source.
	 * @return the current instance of {@link ConceptSearchCriteriaBuilder} for method chaining.
	 */
	public ConceptSearchCriteriaBuilder mappingCode(String mappingCode) {
		this.mappingCode = mappingCode;
		return this;
	}

	/**
	 * Sets the concept mapping source name to search for. Must be used together with
	 * {@link #mappingCode(String)}.
	 *
	 * @param mappingSourceName the name or hl7Code of the concept source.
	 * @return the current instance of {@link ConceptSearchCriteriaBuilder} for method chaining.
	 */
	public ConceptSearchCriteriaBuilder mappingSourceName(String mappingSourceName) {
		this.mappingSourceName = mappingSourceName;
		return this;
	}

	/**
	 * Sets whether retired concepts should be included in the search results.
	 *
	 * @param includeRetired true to include retired concepts, false otherwise.
	 * @return the current instance of {@link ConceptSearchCriteriaBuilder} for method chaining.
	 */
	public ConceptSearchCriteriaBuilder includeRetired(boolean includeRetired) {
		this.includeRetired = includeRetired;
		return this;
	}

	/**
	 * Builds and returns a {@link ConceptSearchCriteria} instance based on the current state of the
	 * builder.
	 *
	 * @return a new instance of {@link ConceptSearchCriteria}.
	 */
	public ConceptSearchCriteria build() {
		return new ConceptSearchCriteria(uuids, conceptIds, mappingCode, mappingSourceName, includeRetired);
	}
}
