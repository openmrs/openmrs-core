/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRProviderServiceTest extends BaseWebContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private DWRProviderService service;
	
	@Before
	public void setup() throws Exception {
		service = new DWRProviderService();
		
		executeDataSet(PROVIDERS_INITIAL_XML);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
	}
	
	/**
	 * @see DWRProviderService#findProvider(String,boolean,Integer,Integer)
	 * @verifies return a message with no matches found when no providers are found
	 */
	@Test
	public void findProvider_shouldReturnAMessageWithNoMatchesFoundWhenNoProvidersAreFound() throws Exception {
		Vector<Object> providers = service.findProvider("noProvider", false, 0, 1);
		
		Assert.assertEquals("Provider.noMatchesFound", ((String) providers.get(0)));
	}
	
	/**
	 * @see DWRProviderService#findProvider(String,boolean,Integer,Integer)
	 * @verifies return the list of providers including retired providers for the matching search
	 *           name
	 */
	@Test
	public void findProvider_shouldReturnTheListOfProvidersIncludingRetiredProvidersForTheMatchingSearchName()
	        throws Exception {
		
		Vector<Object> providers = service.findProvider("provider", true, 0, 10);
		Assert.assertEquals(4, providers.size());
		
		Assert.assertTrue(CollectionUtils.exists(providers, new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				return ((ProviderListItem) object).getDisplayName().equals("Jimmy Manana Chemalit");
			}
		}));
	}
	
	/**
	 * @see DWRProviderService#findProvider(String,boolean,Integer,Integer)
	 * @verifies return the list of providers matching the search name
	 */
	@Test
	public void findProvider_shouldReturnTheListOfProvidersMatchingTheSearchName() throws Exception {
		
		Vector<Object> providers = service.findProvider("provider", false, 0, 10);
		Assert.assertEquals(2, providers.size());
		
		final ArrayList<String> providerNames = new ArrayList<String>();
		
		CollectionUtils.forAllDo(providers, new Closure() {
			
			@Override
			public void execute(Object input) {
				providerNames.add(((ProviderListItem) input).getDisplayName());
			}
		});
		
		Assert.assertTrue(providerNames.containsAll(Arrays.asList("Bruno Otterbourg", "Hippocrates of Cos")));
	}
	
	/**
	 * @see DWRProviderService#findProviderCountAndProvider(String,boolean,Integer,Integer)
	 * @verifies return the count of all providers matching the searched name along with provider
	 *           list
	 */
	@Test
	@Ignore("This test fails because we have the order by for person names mentioned in the person.hbm.xml for the names set. "
	        + "H2 is expecting a group by clause for all the columns mentioned in the order by which is not needed to execute a query in mysql."
	        + "Keeping the test case here because this might be a problem in other databases too")
	public void findProviderCountAndProvider_shouldReturnTheCountOfAllProvidersMatchingTheSearchedNameAlongWithProviderList()
	        throws Exception {
		Map<String, Object> countAndProviders = service.findProviderCountAndProvider("provider", true, 0, 2);
		Assert.assertEquals(3, countAndProviders.get("count"));
	}
}
