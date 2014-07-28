/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
			this.maxElements = Long.valueOf(collection.size());
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
