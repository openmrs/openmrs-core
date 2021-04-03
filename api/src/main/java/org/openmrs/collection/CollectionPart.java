/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.collection;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

/**
 * It is a wrapper for a partial collection, which stores additional info about the current part and
 * the whole.
 * 
 * @since 1.11
 */
public abstract class CollectionPart<E> {
	
	private final Long firstElement;
	
	private final Long maxElements;
	
	private final Long totalElements;
	
	private final Boolean totalElementsExact;
	
	public CollectionPart(Collection<E> collection, Long firstElement, Long maxElements, Long totalElements,
	    Boolean totalElementsExact) {
		Validate.notNull(collection);
		
		if (firstElement == null) {
			this.firstElement = 0L;
		} else {
			this.firstElement = firstElement;
		}
		
		if (maxElements == null) {
			this.maxElements = (long) collection.size();
		} else {
			this.maxElements = maxElements;
		}
		
		this.totalElements = totalElements;
		
		this.totalElementsExact = totalElementsExact;
	}
	
	public abstract Collection<E> getCollection();
	
	public Long getFirstElement() {
		return firstElement;
	}
	
	public Long getMaxElements() {
		return maxElements;
	}
	
	public Long getTotalElements() {
		return totalElements;
	}
	
	public boolean isTotalElementsExact() {
		return Boolean.TRUE.equals(totalElementsExact);
	}
}
