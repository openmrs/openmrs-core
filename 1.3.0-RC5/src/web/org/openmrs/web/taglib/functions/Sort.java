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
package org.openmrs.web.taglib.functions;

import java.util.*;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Sort {
	
	public static final long serialVersionUID = 1333333L;
	private final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This method will sort a collection based on the natural order of it's elements
	 * @param c
	 * @return
	 */
	public static <T> List<T> sort(Collection<T> c) {
		return sort(c, null, false);
	}
	
	/**
	 * This method will sort a collection based on the natural order of it's elements
	 * @param c
	 * @param isDescending
	 * @return
	 */
	public static <T extends Comparable<T>> List<T> sort(Collection<T> c, Boolean reverseOrder) {
		List<T> l = new ArrayList<T>(c);
		Collections.sort(l);
		return l;
	}
	
	/**
	 * This method will sort a passed Collection
	 * @param c: The collection to sort
	 * @param sortProperty: The javabean property to sort the elements of the Collection by
	 * @param isDescending: Boolean indicating whether or not to reverse the order of the collection
	 * @return: A sorted List of the passed elements
	 */
	public static <T> List<T> sort(Collection<T> c, String sortProperty, Boolean reverseOrder) {
		if (sortProperty == null || sortProperty.equals("")) {
			throw new IllegalArgumentException("sortProperty = " + sortProperty);
		} 
		List<T> l = new ArrayList<T>(c);
		Comparator comp = new BeanComparator(sortProperty, new ComparableComparator());
		Collections.sort(l, comp);
		if (reverseOrder) {
			Collections.reverse(l);
		}
		return l;
	}
}
