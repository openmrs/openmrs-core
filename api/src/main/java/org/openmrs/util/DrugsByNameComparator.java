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

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;

/**
 * The Class DrugsByNameComparator. An Util class which sorts drug names while ignoring any
 * numericals or other characters contained in the string. It will ignore all except letters a-z and
 * A-Z.
 */
public class DrugsByNameComparator implements Comparator<Drug> {
	
	/** The Constant log. */
	private final static Log log = LogFactory.getLog(DrugsByNameComparator.class);
	
	/* (non-Jsdoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Drug d1, Drug d2) {
		return compareDrugNamesIgnoringNumericals(d1, d2);
	}
	
	/**
	 * Compare drug names ignoring numericals and other characters. Using compareToIgnoreCase()
	 * method to prevent a capital letter getting precedence over a simple letter which comes before
	 * it in the alphabet.
	 * 
	 * @param d1 the first Drug to be compared
	 * @param d2 the second Drug to be compared
	 * @return the int
	 */
	private int compareDrugNamesIgnoringNumericals(Drug d1, Drug d2) {
		
		String firstDrugName = remove(d1.getName());
		String secondDrugName = remove(d2.getName());
		
		return firstDrugName.compareToIgnoreCase(secondDrugName);
	}
	
	/**
	 * Private method which will remove all characters expect a-z and A to Z from text strings
	 * 
	 * @param drugName the drug name
	 * @return the string
	 */
	private String remove(String drugName) {
		String cleanText = drugName.replaceAll("[^a-zA-Z]", "");
		return cleanText;
	}
}
