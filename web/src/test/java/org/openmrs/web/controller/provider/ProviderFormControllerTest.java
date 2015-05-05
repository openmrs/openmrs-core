/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.provider;

import java.util.Arrays;

import junit.framework.Assert;

import org.hibernate.ObjectNotFoundException;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;

/**
 * Tests against the {@link ProviderFormController}
 */
public class ProviderFormControllerTest extends BaseWebContextSensitiveTest {
	
	protected static final String PROVIDERS_ATTRIBUTES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	protected static final String PROVIDERS_XML = "org/openmrs/api/include/ProviderServiceTest-initial.xml";
	
	/**
	 * @verifies not void or change attributeList if the attribute values are same
	 * @see org.openmrs.web.controller.provider.ProviderFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      String, String, String, String, org.openmrs.Provider,
	 *      org.springframework.validation.BindingResult, org.springframework.ui.ModelMap)
	 */
	@Test
	public void onSubmit_shouldNotVoidOrChangeAttributeListIfTheAttributeValuesAreSame() throws Exception {
		executeDataSet(PROVIDERS_ATTRIBUTES_XML);
		Provider provider = Context.getProviderService().getProvider(1);
		ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeType(1);
		providerAttributeType.setName("provider joined date");
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletRequest.setParameter("attribute." + providerAttributeType.getId(), "2011-04-25");
		BindException errors = new BindException(provider, "provider");
		ProviderFormController providerFormController = (ProviderFormController) applicationContext
		        .getBean("providerFormController");
		providerFormController.onSubmit(mockHttpServletRequest, "save", null, null, null, provider, errors,
		    createModelMap(providerAttributeType));
		Assert.assertFalse(((ProviderAttribute) (provider.getAttributes().toArray()[0])).getVoided());
		Assert.assertEquals(1, provider.getAttributes().size());
		
	}
	
	/**
	 * @verifies set attributes to void if the values is not set
	 * @see org.openmrs.web.controller.provider.ProviderFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      String, String, String, String, org.openmrs.Provider,
	 *      org.springframework.validation.BindingResult, org.springframework.ui.ModelMap)
	 */
	@Test
	public void onSubmit_shouldSetAttributesToVoidIfTheValueIsNotSet() throws Exception {
		executeDataSet(PROVIDERS_ATTRIBUTES_XML);
		Provider provider = Context.getProviderService().getProvider(1);
		ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeType(1);
		providerAttributeType.setName("provider type");
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		//If value is not set then void all the attributes.
		mockHttpServletRequest.setParameter("attribute." + providerAttributeType.getId() + ".existing[1]", "");
		BindException errors = new BindException(provider, "provider");
		ProviderFormController providerFormController = (ProviderFormController) applicationContext
		        .getBean("providerFormController");
		providerFormController.onSubmit(mockHttpServletRequest, "save", null, null, null, provider, errors,
		    createModelMap(providerAttributeType));
		Assert.assertEquals(1, provider.getAttributes().size());
		Assert.assertTrue(((ProviderAttribute) (provider.getAttributes().toArray()[0])).isVoided());
		
	}
	
	/**
	 * @verifies should purge the provider
	 * @see org.openmrs.web.controller.provider.ProviderFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      String, String, String, String, org.openmrs.Provider,
	 *      org.springframework.validation.BindingResult, org.springframework.ui.ModelMap)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void onSubmit_shouldPurgeTheProvider() throws Exception {
		executeDataSet(PROVIDERS_ATTRIBUTES_XML);
		executeDataSet(PROVIDERS_XML);
		Provider provider = Context.getProviderService().getProvider(2);
		ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeType(1);
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		BindException errors = new BindException(provider, "provider");
		ProviderFormController providerFormController = (ProviderFormController) applicationContext
		        .getBean("providerFormController");
		providerFormController.onSubmit(mockHttpServletRequest, null, null, null, "purge", provider, errors,
		    createModelMap(providerAttributeType));
		Context.flushSession();
		Assert.assertNull(Context.getProviderService().getProvider(2));
	}
	
	private ModelMap createModelMap(ProviderAttributeType providerAttributeType) {
		ModelMap modelMap = new ModelMap();
		modelMap.put("providerAttributeTypes", Arrays.asList(providerAttributeType));
		return modelMap;
	}
	
}
