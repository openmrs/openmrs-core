/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.Serializable;
import java.util.Comparator;

import org.openmrs.ConceptMapType;

/**
 * A utility class which sorts a collection of ConceptMapType objects in the following order:
 * <ol>
 * <li>Regular</li>
 * <li>Retired</li>
 * <li>Hidden</li>
 * <li>Retired and Hidden</li>
 * </ol>
 */
public class ConceptMapTypeComparator implements Comparator<ConceptMapType>, Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ConceptMapType conceptMapType, ConceptMapType conceptMapType2) {
		int firstWeight = getConceptMapTypeSortWeight(conceptMapType);
		int secondWeight = getConceptMapTypeSortWeight(conceptMapType2);
		
		return Integer.compare(firstWeight, secondWeight);
	}
	
	/**
	 * <p>This method calculates a weight used to decide the object's order in a collection.</p>
	 * @param conceptMapType the ConceptMapType object the weight of which is to be calculated
	 * @return
	 * <ol>
	 * <li>Regular: 0</li>
	 * <li>Retired: 1</li>
	 * <li>Hidden: 2</li>
	 * <li>Retired and Hidden: 3</li>
	 * </ol>
	 */
	public static int getConceptMapTypeSortWeight(ConceptMapType conceptMapType) {
		return ((conceptMapType.getRetired() ? 1 : 0) + (conceptMapType.getIsHidden() ? 2 : 0));
	}
}
