/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.StringUtils;

/**
 * Functions used within taglibs in a webapp jsp page. <br/>
 * <br/>
 * Example:
 *
 * <pre>
 * &lt;c:forEach items="${openmrs:sort(someListObject)}" var="o" end="0">
 *   ....
 *   ....
 * &lt;/c:forEach>
 * </pre>
 */
public class Sort {
	
	/**
	 * This method will sort a collection based on the natural order of it's elements
	 *
	 * @param c
	 * @return
	 */
	public static <T> List<T> sort(Collection<T> c) {
		return sort(c, null, false);
	}
	
	/**
	 * This method will sort a collection based on the natural order of it's elements
	 *
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
	 *
	 * @param c: The collection to sort
	 * @param sortProperty: The javabean property to sort the elements of the Collection by
	 * @param isDescending: Boolean indicating whether or not to reverse the order of the collection
	 * @return: A sorted List of the passed elements
	 */
	public static <T> List<T> sort(Collection<T> c, String sortProperty, Boolean reverseOrder) {
		if (StringUtils.isEmpty(sortProperty)) {
			throw new IllegalArgumentException("sortProperty = " + sortProperty);
		}
		// fail early if the passed collection is null
		if (c == null) {
			return null;
		}
		
		// fail early if the passed collection is empty
		if (c.size() == 0) {
			return Collections.emptyList();
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
