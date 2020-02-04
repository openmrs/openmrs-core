/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ChemoDosingInstructionsTest extends BaseContextSensitiveTest {

	private static final String DILUTION_INSTRUCTIONS_CONSTANT = "10% diluted";

	private static final Double ADJUSTMENT_PERCENTAGE_CONSTANT = 20.04;

	private static final String DELIVERED_TIMING_CONSTANT= "once in 2 hours";

	private static final String ADJUSTMENT_PERCENTAGE = "Dosage Adjustment Percentage : ";

	private static final String DELIVERED_TIMING = "Dosage Delivered Timing : ";

	private static final String DILUTION_INSTRUCTIONS = "Dosage Dilution Instructions : ";

	@Test
	public void getDosingInstructionsAsString_shouldInferCorrectly() {
		ChemoDosingInstructions chemoDosingInstructions = new ChemoDosingInstructions();
		chemoDosingInstructions.setDosageAdjustmentPercentage(ADJUSTMENT_PERCENTAGE_CONSTANT);
		chemoDosingInstructions.setDosageDilutionInstructions(DILUTION_INSTRUCTIONS_CONSTANT);
		chemoDosingInstructions.setDosageDeliveredTiming(DELIVERED_TIMING_CONSTANT);

		String shouldBeResult = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DELIVERED_TIMING +
			DELIVERED_TIMING_CONSTANT + "\n" + DILUTION_INSTRUCTIONS + DILUTION_INSTRUCTIONS_CONSTANT + "\n";

		String dosingInstructions = chemoDosingInstructions.getDosingInstructionsAsString(Locale.getDefault());
		Assert.assertEquals(dosingInstructions, shouldBeResult);
	}

	@Test
	public void setDosingInstructions_shouldBeSetCorrectly() {
		DrugOrder drugOrder = new DrugOrder();
		ChemoDosingInstructions chemoDosingInstructions = new ChemoDosingInstructions();
		chemoDosingInstructions.setDosageAdjustmentPercentage(ADJUSTMENT_PERCENTAGE_CONSTANT);
		chemoDosingInstructions.setDosageDilutionInstructions(DILUTION_INSTRUCTIONS_CONSTANT);

		String shouldBeResult = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DILUTION_INSTRUCTIONS +
			DILUTION_INSTRUCTIONS_CONSTANT + "\n";

		chemoDosingInstructions.setDosingInstructions(drugOrder);

		Assert.assertEquals(drugOrder.getDosingType(), ChemoDosingInstructions.class);
		Assert.assertEquals(drugOrder.getDosingInstructions(), shouldBeResult);
	}

	@Test
	public void getDosingInstructions_shouldInferCorrectly() {
		DrugOrder order = createValidDrugOrder();
		ChemoDosingInstructions instructions = new ChemoDosingInstructions();
		ChemoDosingInstructions dosingInstructions = (ChemoDosingInstructions)instructions.getDosingInstructions(order);

		Assert.assertEquals(dosingInstructions.getDosageAdjustmentPercentage(), ADJUSTMENT_PERCENTAGE_CONSTANT);
		Assert.assertEquals(dosingInstructions.getDosageDeliveredTiming(),DELIVERED_TIMING_CONSTANT);
		Assert.assertEquals(dosingInstructions.getDosageDilutionInstructions(),DILUTION_INSTRUCTIONS_CONSTANT);
	}

	private DrugOrder createValidDrugOrder() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDosingType(ChemoDosingInstructions.class);
		String instructions = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DELIVERED_TIMING +
			DELIVERED_TIMING_CONSTANT + "\n" + DILUTION_INSTRUCTIONS + DILUTION_INSTRUCTIONS_CONSTANT + "\n";
		drugOrder.setDosingInstructions(instructions);
		return drugOrder;
	}
}
