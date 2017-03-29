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

import java.io.IOException;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.databasechange.Database1_9_7UpgradeIT;

public class UpgradeUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @throws IOException
	 * @see org.openmrs.util.UpgradeUtil#getConceptIdForUnits(String)
	 */
	@Test
	public void getConceptIdForUnits_shouldReturnConcept_idForDrug_order_quantity_units() throws IOException {
		Database1_9_7UpgradeIT.createOrderEntryUpgradeFileWithTestData("mg=5401" + "\n" + "drug_order_quantity_units=5403"
		        + "\n" + "ounces=5402");
		
		Integer conceptId = UpgradeUtil.getConceptIdForUnits("drug_order_quantity_units");
		
		Assert.assertThat(conceptId, Is.is(5403));
	}
	
	/**
	 * @throws IOException
	 * @see org.openmrs.util.UpgradeUtil#getConceptIdForUnits(String)
	 */
	@Test(expected = APIException.class)
	public void getConceptIdForUnits_shouldFailIfUnitsIsNotSpecified() throws IOException {
		Database1_9_7UpgradeIT.createOrderEntryUpgradeFileWithTestData("mg=540" + "\n" + "ounces=5402");
		
		UpgradeUtil.getConceptIdForUnits("drug_order_quantity_units");
	}
}
