/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.controller.provider;

import java.util.Arrays;

import junit.framework.Assert;

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
	
	/**
	 * @verifies not void or change attributeList if the attribute values are same
	 * @see org.openmrs.web.controller.provider.ProviderFormController#onSubmit(org.springframework.web.context.request.WebRequest,
	 *      String, String, String, org.openmrs.Provider,
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
		providerFormController.onSubmit(mockHttpServletRequest, "save", null, null, true, provider, errors,
		    createModelMap(providerAttributeType));
		Assert.assertFalse(((ProviderAttribute) (provider.getAttributes().toArray()[0])).getVoided());
		Assert.assertEquals(1, provider.getAttributes().size());
		
	}
	
	/**
	 * @verifies set attributes to void if the values is not set
	 * @see org.openmrs.web.controller.provider.ProviderFormController#onSubmit(org.springframework.web.context.request.WebRequest,
	 *      String, String, String, org.openmrs.Provider,
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
		ProviderFormController visitFormController = (ProviderFormController) applicationContext
		        .getBean("providerFormController");
		visitFormController.onSubmit(mockHttpServletRequest, "save", null, null, true, provider, errors,
		    createModelMap(providerAttributeType));
		Assert.assertEquals(1, provider.getAttributes().size());
		Assert.assertTrue(((ProviderAttribute) (provider.getAttributes().toArray()[0])).isVoided());
		
	}
	
	private ModelMap createModelMap(ProviderAttributeType providerAttributeType) {
		ModelMap modelMap = new ModelMap();
		modelMap.put("providerAttributeTypes", Arrays.asList(providerAttributeType));
		return modelMap;
	}
	
}
