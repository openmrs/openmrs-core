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
		return new ListPart<T>(list, firstElement, maxElements, totalElements, totalElementsExact);
	}
	
}
