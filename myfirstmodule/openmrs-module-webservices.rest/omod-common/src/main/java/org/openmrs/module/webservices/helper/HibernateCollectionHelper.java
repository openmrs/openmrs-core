/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import java.util.Collection;
import java.util.Comparator;

/**
 * Hibernate does not allow for replacing a collection in an attached object. The collection must be
 * updated instead. This class is used to manipulate an existing collection property when calling
 * set, by delegating to proper add and remove methods on the given instance and using a custom
 * comparator for elements.
 * 
 * @param <T>
 * @param <E>
 */
public abstract class HibernateCollectionHelper<T, E> implements Comparator<E> {
	
	protected T instance;
	
	public HibernateCollectionHelper(T instance) {
		this.instance = instance;
	}
	
	public abstract Collection<E> getAll();
	
	public abstract void add(E item);
	
	public abstract void remove(E item);
	
	public void set(Collection<E> items) {
		//delete objects which are absent in new list
		for (E oldItem : getAll()) {
			boolean found = false;
			
			for (E newItem : items) {
				if (compare(oldItem, newItem) == 0) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				remove(oldItem);
			}
		}
		
		//add objects which are absent in old list
		for (E newItem : items) {
			boolean found = false;
			for (E oldItem : getAll()) {
				if (compare(oldItem, newItem) == 0) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				add(newItem);
			}
		}
	}
}
