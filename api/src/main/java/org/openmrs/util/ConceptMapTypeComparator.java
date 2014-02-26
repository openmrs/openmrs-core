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

package org.openmrs.util;

import org.openmrs.ConceptMapType;

import java.util.Comparator;

/**
 * A utility class which sorts a collection of ConceptMapType objects in the following order:
 * <ol>
 * <li>Regular</li>
 * <li>Retired</li>
 * <li>Hidden</li>
 * <li>Retired and Hidden</li>
 * </ol>
 */
public class ConceptMapTypeComparator implements Comparator<ConceptMapType> {
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ConceptMapType conceptMapType, ConceptMapType conceptMapType2) {
		int firstWeight = getConceptMapTypeSortWeight(conceptMapType);
		int secondWeight = getConceptMapTypeSortWeight(conceptMapType2);
		
		return (firstWeight < secondWeight) ? -1 : (firstWeight == secondWeight) ? 0 : 1;
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
		return ((conceptMapType.isRetired() ? 1 : 0) + (conceptMapType.isHidden() ? 2 : 0));
	}
}
