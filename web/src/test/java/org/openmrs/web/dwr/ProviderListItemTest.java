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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;

import java.util.HashSet;

public class ProviderListItemTest {
	
	private Provider provider;
	
	@SuppressWarnings("serial")
	@Before
	public void setup() {
		provider = new Provider() {
			
			{
				setPerson(new Person() {
					
					{
						setNames(new HashSet<PersonName>() {
							
							{
								add(new PersonName() {
									
									{
										setGivenName("givenName");
										setFamilyName("familyName");
										setMiddleName("middleName");
										
									}
								});
							}
						});
					}
				});
			}
		};
	}
	
	/**
	 * @see ProviderListItem#getDisplayName()
	 * @verifies return a display name based on whether provider has a person associated
	 */
	@Test
	public void getDisplayName_shouldReturnADisplayNameBasedOnWhetherProviderHasAPersonAssociated() throws Exception {
		
		ProviderListItem providerListItem = new ProviderListItem(provider);
		Assert.assertEquals("givenName middleName familyName", providerListItem.getDisplayName());
	}
	
	/**
	 * @see ProviderListItem#getIdentifier()
	 * @verifies return the identifier that is mentioned for the provider when a person is not
	 *           specified
	 */
	@Test
	public void getIdentifier_shouldReturnTheIdentifierThatIsMentionedForTheProviderWhenAPersonIsNotSpecified()
	        throws Exception {
		Provider provider = new Provider();
		provider.setIdentifier("identifier");
		ProviderListItem providerListItem = new ProviderListItem(provider);
		Assert.assertEquals("identifier", providerListItem.getIdentifier());
	}
	
	/**
	 * @see ProviderListItem#getProviderId()
	 * @verifies return the provider id
	 */
	@Test
	public void getProviderId_shouldReturnTheProviderId() throws Exception {
		provider.setProviderId(2);
		
		ProviderListItem providerListItem = new ProviderListItem(provider);
		Assert.assertEquals(Integer.valueOf(2), providerListItem.getProviderId());
	}
	
}
