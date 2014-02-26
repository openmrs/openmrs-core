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
