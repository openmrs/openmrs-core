package org.openmrs.util;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.databasechange.Database1_9To1_10UpgradeTest;

public class UpgradeUtilTest extends BaseContextSensitiveTest {

	/**
	 * @verifies return concept_id for drug_order_quantity_units
	 * @see org.openmrs.util.UpgradeUtil#getConceptIdForUnits(String)
	 */
	@Test
	public void getConceptIdForUnits_shouldReturnConcept_idForDrug_order_quantity_units() throws Exception {
		Database1_9To1_10UpgradeTest.createOrderEntryUpgradeFileWithTestData("mg=5401" + "\n" + "drug order quantity units=5403" + "\n"
				+ "ounces=5402");

		Integer conceptId = UpgradeUtil.getConceptIdForUnits("drug order quantity units");

		Assert.assertThat(conceptId, Is.is(5403));
	}

	/**
	 * @verifies fail if units is not specified
	 * @see org.openmrs.util.UpgradeUtil#getConceptIdForUnits(String)
	 */
	@Test(expected = APIException.class)
	public void getConceptIdForUnits_shouldFailIfUnitsIsNotSpecified() throws Exception {
		Database1_9To1_10UpgradeTest.createOrderEntryUpgradeFileWithTestData("mg=540" + "\n" + "ounces=5402");

		UpgradeUtil.getConceptIdForUnits("drug_order_quantity_units");
	}
}