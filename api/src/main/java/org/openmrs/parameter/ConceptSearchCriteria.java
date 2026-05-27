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
 * system. It supports searching by UUIDs, integer IDs, concept mappings (in the form "source:term")
 * and concept names.
 * <p>
 * Instead of calling the constructor directly, use {@link ConceptSearchCriteriaBuilder}.
 *
 * @since 2.8.7
 */
public class ConceptSearchCriteria {

	private Collection<String> uuids;

	private Collection<Integer> conceptIds;

	private Collection<String> mappings;

	private Collection<String> names;

	private boolean includeRetired = false;

	/**
	 * Constructs a ConceptSearchCriteria with the specified parameters. Instead of calling this
	 * constructor directly, it is recommended to use {@link ConceptSearchCriteriaBuilder}.
	 *
	 * @param uuids collection of concept UUIDs to search for
	 * @param conceptIds collection of concept integer IDs to search for
	 * @param mappings collection of concept mappings in {@code "source:term"} format to search for
	 * @param names collection of concept names to search for
	 * @param includeRetired whether to include retired concepts in the results
	 */
	public ConceptSearchCriteria(Collection<String> uuids, Collection<Integer> conceptIds, Collection<String> mappings,
	    Collection<String> names, boolean includeRetired) {
		this.uuids = uuids;
		this.conceptIds = conceptIds;
		this.mappings = mappings;
		this.names = names;
		this.includeRetired = includeRetired;
	}

	/**
	 * @return the collection of UUIDs included in the search criteria.
	 */
	public Collection<String> getUuids() {
		return uuids;
	}

	/**
	 * @return the collection of concept integer IDs included in the search criteria.
	 */
	public Collection<Integer> getConceptIds() {
		return conceptIds;
	}

	/**
	 * @return the collection of mappings in {@code "source:term"} format included in the search
	 *         criteria.
	 */
	public Collection<String> getMappings() {
		return mappings;
	}

	/**
	 * @return the collection of concept names included in the search criteria.
	 */
	public Collection<String> getNames() {
		return names;
	}

	/**
	 * @return true if retired concepts are included in the search results, false otherwise.
	 */
	public boolean getIncludeRetired() {
		return includeRetired;
	}
}
