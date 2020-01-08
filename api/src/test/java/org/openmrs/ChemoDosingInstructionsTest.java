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

	@Test
	public void getDosingInstructionsAsString_shouldInferCorrectly() {
		ChemoDosingInstructions chemoDosingInstructions = new ChemoDosingInstructions();
		chemoDosingInstructions.setDosageAdjustmentPercentage(20.04);
		chemoDosingInstructions.setDosageDilutionInstructions("10% diluted");
		chemoDosingInstructions.setDosageDeliveredTiming("once in 2 hours");

		String shouldBeResult = "Dosage Adjustment Percentage : 20.04\nDosage Delivered Timing : once in 2 hours"
			+ "\nDosage Dilution Instructions : 10% diluted\n";

		String dosingInstructions = chemoDosingInstructions.getDosingInstructionsAsString(Locale.getDefault());
		Assert.assertEquals(dosingInstructions, shouldBeResult);
	}

	@Test
	public void setDosingInstructions_shouldBeSetCorrectly() {
		DrugOrder drugOrder = new DrugOrder();
		ChemoDosingInstructions chemoDosingInstructions = new ChemoDosingInstructions();
		chemoDosingInstructions.setDosageAdjustmentPercentage(20.04);
		chemoDosingInstructions.setDosageDilutionInstructions("10% diluted");

		String shouldBeResult = "Dosage Adjustment Percentage : 20.04\nDosage Dilution Instructions : 10% diluted\n";

		chemoDosingInstructions.setDosingInstructions(drugOrder);

		Assert.assertEquals(drugOrder.getDosingType(), ChemoDosingInstructions.class);
		Assert.assertEquals(drugOrder.getDosingInstructions(), shouldBeResult);
	}

	@Test
	public void getDosingInstructions_shouldInferCorrectly() {
		DrugOrder order = createValidDrugOrder();
		ChemoDosingInstructions instructions = new ChemoDosingInstructions();
		ChemoDosingInstructions dosingInstructions = (ChemoDosingInstructions)instructions.getDosingInstructions(order);
		
		Assert.assertEquals(dosingInstructions.getDosageAdjustmentPercentage(), new Double(20.04));
		Assert.assertEquals(dosingInstructions.getDosageDeliveredTiming(),"once in 2 hours");
		Assert.assertEquals(dosingInstructions.getDosageDilutionInstructions(),"10% diluted");
	}
	
	private DrugOrder createValidDrugOrder() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDosingType(ChemoDosingInstructions.class);
		String instructions = "Dosage Adjustment Percentage : 20.04\nDosage Delivered Timing : once in 2 hours"
			+ "\nDosage Dilution Instructions : 10% diluted\n";
		drugOrder.setDosingInstructions(instructions);
		return drugOrder;
	}
}
