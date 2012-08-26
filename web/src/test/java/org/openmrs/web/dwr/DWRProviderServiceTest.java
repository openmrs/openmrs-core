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
