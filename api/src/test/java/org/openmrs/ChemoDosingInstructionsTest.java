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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class ChemoDosingInstructionsTest extends BaseContextSensitiveTest {

	private DrugOrder drugOrder;
	
	private ChemoDosingInstructions chemoDosingInstructions;
	private static final String DILUTION_INSTRUCTIONS_CONSTANT = "10% diluted";

	private static final Double ADJUSTMENT_PERCENTAGE_CONSTANT = 20.04;

	private static final String DELIVERED_TIMING_CONSTANT= "once in 2 hours";

	private static final String ADJUSTMENT_PERCENTAGE = "Dosage Adjustment Percentage : ";

	private static final String DELIVERED_TIMING = "Dosage Delivered Timing : ";

	private static final String DILUTION_INSTRUCTIONS = "Dosage Dilution Instructions : ";

	@BeforeEach
	void setUp() {
		drugOrder = new DrugOrder();
		chemoDosingInstructions = new ChemoDosingInstructions();
	}

	@Test
	public void getDosingInstructionsAsString_shouldInferCorrectly() {
		
		chemoDosingInstructions.setDosageAdjustmentPercentage(ADJUSTMENT_PERCENTAGE_CONSTANT);
		chemoDosingInstructions.setDosageDilutionInstructions(DILUTION_INSTRUCTIONS_CONSTANT);
		chemoDosingInstructions.setDosageDeliveredTiming(DELIVERED_TIMING_CONSTANT);

		// when getDosingInstructionsAsString(Locale) is called, results should be in this format
		String shouldBeResult = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DELIVERED_TIMING +
			DELIVERED_TIMING_CONSTANT + "\n" + DILUTION_INSTRUCTIONS + DILUTION_INSTRUCTIONS_CONSTANT + "\n";

		String dosingInstructions = chemoDosingInstructions.getDosingInstructionsAsString(Locale.getDefault());
		
		// assert that results returned by getDosingInstructionsAsString(Locale) are same as shouldBeResult
		Assertions.assertEquals(dosingInstructions, shouldBeResult);
	}

	@Test
	public void setDosingInstructions_shouldBeSetCorrectly() {
		
		chemoDosingInstructions.setDosageAdjustmentPercentage(ADJUSTMENT_PERCENTAGE_CONSTANT);
		chemoDosingInstructions.setDosageDilutionInstructions(DILUTION_INSTRUCTIONS_CONSTANT);
		
		// when getDosingInstructions() is called, results should be in this format
		String shouldBeResult = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DILUTION_INSTRUCTIONS +
			DILUTION_INSTRUCTIONS_CONSTANT + "\n";

		chemoDosingInstructions.setDosingInstructions(drugOrder);

		// assert that results returned by getDosingInstructions() are same as shouldBeResult if the dosingType of 
		// drugOrder is ChemoDosingInstructions
		Assertions.assertEquals(drugOrder.getDosingType(), ChemoDosingInstructions.class);
		Assertions.assertEquals(drugOrder.getDosingInstructions(), shouldBeResult);
	}

	@Test
	public void getDosingInstructions_shouldInferCorrectly() {

		drugOrder.setDosingType(ChemoDosingInstructions.class);
		String instStr = ADJUSTMENT_PERCENTAGE + ADJUSTMENT_PERCENTAGE_CONSTANT + "\n" + DELIVERED_TIMING +
			DELIVERED_TIMING_CONSTANT + "\n" + DILUTION_INSTRUCTIONS + DILUTION_INSTRUCTIONS_CONSTANT + "\n";
		drugOrder.setDosingInstructions(instStr);
		
		ChemoDosingInstructions instructions = new ChemoDosingInstructions();
		chemoDosingInstructions = (ChemoDosingInstructions)instructions.getDosingInstructions(drugOrder);

		Assertions.assertEquals(chemoDosingInstructions.getDosageAdjustmentPercentage(), ADJUSTMENT_PERCENTAGE_CONSTANT);
		Assertions.assertEquals(chemoDosingInstructions.getDosageDeliveredTiming(), DELIVERED_TIMING_CONSTANT);
		Assertions.assertEquals(chemoDosingInstructions.getDosageDilutionInstructions(), DILUTION_INSTRUCTIONS_CONSTANT);
	}
		
}
