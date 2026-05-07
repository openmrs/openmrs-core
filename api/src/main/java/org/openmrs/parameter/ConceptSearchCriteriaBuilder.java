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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

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

	private Collection<String> mappings;

	private boolean includeRetired = false;

	/**
	 * Constructs a new {@link ConceptSearchCriteriaBuilder} instance.
	 */
	public ConceptSearchCriteriaBuilder() {
		uuids = new ArrayList<>();
		conceptIds = new ArrayList<>();
		mappings = new ArrayList<>();
	}

	/**
	 * Adds a concept UUID to the search criteria.
	 *
	 * @param uuid the UUID of the concept to include.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addUuid(String uuid) {
		this.uuids.add(uuid);
		return this;
	}

	/**
	 * Adds multiple concept UUIDs to the search criteria.
	 *
	 * @param uuids the collection of UUIDs to include.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addUuids(Collection<String> uuids) {
		this.uuids.addAll(uuids);
		return this;
	}

	/**
	 * Adds a concept integer ID to the search criteria.
	 *
	 * @param conceptId the integer ID of the concept to include.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addConceptId(Integer conceptId) {
		this.conceptIds.add(conceptId);
		return this;
	}

	/**
	 * Adds multiple concept integer IDs to the search criteria.
	 *
	 * @param conceptIds the collection of concept IDs to include.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addConceptIds(Collection<Integer> conceptIds) {
		this.conceptIds.addAll(conceptIds);
		return this;
	}

	/**
	 * Adds a concept mapping to the search criteria. The mapping must be in {@code "source:term"}
	 * format, where {@code source} is the concept source name or HL7 code and {@code term} is the
	 * concept code.
	 *
	 * @param mapping the mapping string in {@code "source:term"} format.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addMapping(String mapping) {
		this.mappings.add(mapping);
		return this;
	}

	/**
	 * Adds multiple concept mappings to the search criteria. Each mapping must be in
	 * {@code "source:term"} format, where {@code source} is the concept source name or HL7 code and
	 * {@code term} is the concept code.
	 *
	 * @param mappings the collection of mapping strings in {@code "source:term"} format.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addMappings(Collection<String> mappings) {
		this.mappings.addAll(mappings);
		return this;
	}

	/**
	 * Adds a concept reference to the search criteria, automatically classifying it into the
	 * appropriate bucket. The reference is resolved in this order:
	 * <ol>
	 * <li>UUID — if the value is a valid UUID format</li>
	 * <li>Mapping — if the value contains {@code ":"} (i.e. {@code "source:term"})</li>
	 * <li>Concept ID — if the value is a non-negative integer</li>
	 * <li>Static constant — if the value is a fully qualified Java constant name (e.g.
	 * {@code "org.openmrs.module.emrapi.EmrApiConstants.CONCEPT_SOURCE_NAME"}), its value is resolved
	 * via reflection and the result is classified recursively</li>
	 * </ol>
	 * Blank or unrecognised references are silently ignored.
	 *
	 * @param ref the concept reference string.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addConceptReference(String ref) {
		if (StringUtils.isBlank(ref)) {
			return this;
		}
		if (isValidUuidFormat(ref)) {
			return addUuid(ref);
		}
		int idx = ref.indexOf(":");
		if (idx >= 0 && idx < ref.length() - 1) {
			return addMapping(ref);
		}
		int conceptId = NumberUtils.toInt(ref, -1);
		if (conceptId >= 0) {
			return addConceptId(conceptId);
		}
		if (ref.contains(".")) {
			try {
				String resolved = evaluateStaticConstant(ref);
				if (resolved != null) {
					return addConceptReference(resolved);
				}
			} catch (APIException e) {
				// unresolvable constant — skip silently
			}
		}
		return this;
	}

	/**
	 * Adds multiple concept references to the search criteria. Each reference is classified
	 * individually by {@link #addConceptReference(String)}.
	 *
	 * @param refs the collection of concept reference strings.
	 * @return the current builder instance for method chaining.
	 */
	public ConceptSearchCriteriaBuilder addConceptReferences(Collection<String> refs) {
		for (String ref : refs) {
			addConceptReference(ref);
		}
		return this;
	}

	private static boolean isValidUuidFormat(String uuid) {
		return uuid.length() >= 36 && uuid.length() <= 38 && !uuid.contains(" ") && !uuid.contains(".");
	}

	private static String evaluateStaticConstant(String fqn) {
		int lastPeriod = fqn.lastIndexOf(".");
		String clazzName = fqn.substring(0, lastPeriod);
		String constantName = fqn.substring(lastPeriod + 1);
		try {
			Class<?> clazz = Context.loadClass(clazzName);
			Field constantField = clazz.getDeclaredField(constantName);
			constantField.setAccessible(true);
			Object val = constantField.get(null);
			return val != null ? String.valueOf(val) : null;
		} catch (Exception ex) {
			throw new APIException("Error while evaluating " + fqn + " as a constant", ex);
		}
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
		return new ConceptSearchCriteria(uuids, conceptIds, mappings, includeRetired);
	}
}
