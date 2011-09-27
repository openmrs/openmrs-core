package org.openmrs;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.AttributeType;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * The contents of this file are subject to the OpenMRS Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */

public class BaseCustomizableMetadataTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private ProviderService service;
	
	@Before
	public void before() throws Exception {
		service = Context.getProviderService();
		executeDataSet(PROVIDERS_INITIAL_XML);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
	}
	
	/**
	 * @verifies void the attribute if an attribute with same attribute type already exists and the
	 *           maxOccurs is set to 1
	 * @see org.openmrs.BaseCustomizableMetadata#setAttribute(org.openmrs.attribute.Attribute)
	 */
	@Test
	public void setAttribute_shouldVoidTheAttributeIfAnAttributeWithSameAttributeTypeAlreadyExistsAndTheMaxOccursIsSetTo1()
	        throws Exception {
		Provider provider = new Provider();
		provider.setIdentifier("test");
		provider.setName("test provider");
		AttributeType<Provider> providerAttributeType = service.getProviderAttributeType(3);
		provider.setAttribute(createProviderAttribute(providerAttributeType, "bangalore"));
		provider.setAttribute(createProviderAttribute(providerAttributeType, "chennai"));
		Assert.assertEquals(1, provider.getAttributes().size());
		service.saveProvider(provider);
		Assert.assertNotNull(provider.getId());
		provider.setAttribute(createProviderAttribute(providerAttributeType, "seattle"));
		Assert.assertEquals(2, provider.getAttributes().size());
		ProviderAttribute lastAttribute = (ProviderAttribute) provider.getAttributes().toArray()[0];
		Assert.assertTrue(lastAttribute.getVoided());
	}
	
	private ProviderAttribute createProviderAttribute(AttributeType<Provider> providerAttributeType, Object value)
	        throws Exception {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setAttributeType(providerAttributeType);
		providerAttribute.setSerializedValue(value.toString());
		return providerAttribute;
	}
}
