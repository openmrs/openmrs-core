/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.web.address;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

public class AddressSupportTest extends BaseContextSensitiveTest {
	
	private static final String PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH = "org/openmrs/include/personAddressValidatorTestDataset.xml";
	
	/**
	 * As described in TRUNK-3849, when AddressSupport was copied from package
	 * org.openmrs.layout.web.address to org.openmrs.layout.address, and the AddressTemplate in the
	 * database was updated, the web AddressSupport class stopped working with ClassCastExceptions.
	 * A fix was made for backward-compatibility; this test ensures that the fix is working.
	 */
	@Test
	@Verifies(value = "should succeed even if db AddressTemplate class has changed", method = "getAddressTemplate()")
	public void getAddressTemplate_shouldSucceedEvenIfDBAddressTemplateClassHasChanged() throws Exception {
		
		executeDataSet(PERSON_ADDRESS_VALIDATOR_DATASET_PACKAGE_PATH);
		
		//first make sure the test setup is correct even if the dataset changes -- the AddressTemplate class used by this AddressSupport class
		//(in the 'web' package differs from the updated classname in the DB
		String newAddressTemplateClass = "org.openmrs.layout.address.AddressTemplate";
		String xml = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE);
		Assert.assertTrue(StringUtils.contains(xml, newAddressTemplateClass));
		Assert.assertNotEquals(newAddressTemplateClass, AddressTemplate.class.getName());
		
		AddressSupport addressSupport = AddressSupport.getInstance();
		List<AddressTemplate> addressTemplates = addressSupport.getAddressTemplate();
		Assert.assertNotNull(addressTemplates.get(0));
	}
	
}
