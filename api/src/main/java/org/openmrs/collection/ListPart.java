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
import java.util.List;

/**
 * It is a wrapper for a partial list, which stores additional info about the current part and the
 * whole.
 * 
 * @since 1.11
 */
public class ListPart<E> extends CollectionPart<E> {
	
	private final List<E> list;
	
	/**
	 * @param list
	 * @param firstElement
	 * @param maxElements
	 * @param totalElements
	 */
	public ListPart(List<E> list, Long firstElement, Long maxElements, Long totalElements, Boolean totalElementsExact) {
		super(list, firstElement, maxElements, totalElements, totalElementsExact);
		this.list = (List<E>) list;
	}
	
	@Override
	public Collection<E> getCollection() {
		return list;
	}
	
	public List<E> getList() {
		return list;
	}
	
	public static <T> ListPart<T> newListPart(List<T> list, Long firstElement, Long maxElements, Long totalElements,
	        Boolean totalElementsExact) {
		return new ListPart<>(list, firstElement, maxElements, totalElements, totalElementsExact);
	}
	
}
