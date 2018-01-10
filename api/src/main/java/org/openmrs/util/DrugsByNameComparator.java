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

import org.openmrs.Drug;

/**
 * The Class DrugsByNameComparator. An Util class which sorts drug names while ignoring any
 * numericals or other characters contained in the string. It will ignore all except letters a-z and
 * A-Z.
 */
public class DrugsByNameComparator implements Comparator<Drug>, Serializable {

	private static final long serialVersionUID = 1L;

	/* (non-Jsdoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
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
	 * @should return negative if name for drug1 comes before that of drug2
	 * @should return zero if name for drug1 comes before that of drug2
	 * @should return positive if name for drug1 comes before that of drug2 ignoring dashes
	 * @should return positive if name for drug1 comes before that of drug2 ignoring numerics
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
		return drugName.replaceAll("[^a-zA-Z]", "");
	}
}
