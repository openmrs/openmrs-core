package org.openmrs.util;

import org.openmrs.Provider;

import java.util.Comparator;

/**
 * Sorts providers by the primary person name associated with the underlying person
 *
 * Note that this ignores any values stored in the provider "name" property and sorts
 * solely on the underlying person name
 *
 * Utilizes the PersonByName comparator to do the underlying sort, which sorts names based on the following
 * precedence: FamilyName, FamilyName2, GivenName, MiddleName, FamilyNamePrefix, FamilyNameSuffix
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
