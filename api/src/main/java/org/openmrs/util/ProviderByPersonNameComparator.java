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

import java.util.Comparator;

import org.openmrs.Provider;

/**
 * Sorts providers by the primary person name associated with the underlying person
 *
 * Note that this ignores any values stored in the provider "name" property and sorts
 * solely on the underlying person name               l
 *
 * Utilizes the {@link PersonByNameComparator} comparator to do the underlying sort
 */
public class ProviderByPersonNameComparator implements Comparator<Provider> {
	
	@Override
	public int compare(Provider provider1, Provider provider2) {
		
		// test for null cases (sorting them to be last in a list)
		boolean provider1IsNull = (provider1 == null || provider1.getPerson() == null);
		boolean provider2IsNull = (provider2 == null || provider2.getPerson() == null);
		
		if (provider1IsNull && provider2IsNull) {
			return 0;
		} else if (provider1IsNull) {
			return 1;
		} else if (provider2IsNull) {
			return -1;
		}
		
		// delegate to the person by name comparator
		return new PersonByNameComparator().compare(provider1.getPerson(), provider2.getPerson());
	}
}
