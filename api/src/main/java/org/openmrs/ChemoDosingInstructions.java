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

import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * @since 2.7.0
 */
public class ChemoDosingInstructions implements DosingInstructions{

	private Double dosageAdjustmentPercentage;

	private String dosageDeliveredTiming;

	private String dosageDilutionInstructions;

	private static final String ADJUSTMENT_PERCENTAGE = "Dosage Adjustment Percentage";

	private static final String DELIVERED_TIMING = "Dosage Delivered Timing";

	private static final String DILUTION_INSTRUCTIONS = "Dosage Dilution Instructions";


	/**
	 * Serializing adjustment percentage, timing and dilution instructions to compose dosing instructions.
	 * 
	 * @see DosingInstructions#getDosingInstructionsAsString(java.util.Locale)
	 */
	@Override
	public String getDosingInstructionsAsString(Locale locale) {
		StringBuilder dosingInstructions = new StringBuilder();
		if (this.dosageAdjustmentPercentage != null) {
			dosingInstructions.append(this.formatDosingInformation(ADJUSTMENT_PERCENTAGE, this.dosageAdjustmentPercentage.toString()));
		}
		if (this.dosageDeliveredTiming != null) {
			dosingInstructions.append(this.formatDosingInformation(DELIVERED_TIMING, this.dosageDeliveredTiming));
		}
		if (this.dosageDilutionInstructions != null) {
			dosingInstructions.append(this.formatDosingInformation(DILUTION_INSTRUCTIONS, this.dosageDilutionInstructions));
		}
		return dosingInstructions.toString();
	}

	/**
	 * Setting <b>DosingType</b> of the <b>DrugOrder</b> object as "ChemoDosingInstructions" and serialize dosing 
	 * instructions as <tt>String</tt> into the <b>DrugOrder</b> object.
	 * 
	 * @param order DrugOrder to set dosing instructions
	 * 
	 * @see DosingInstructions#setDosingInstructions(DrugOrder)
	 */
	@Override
	public void setDosingInstructions(DrugOrder order) {
		order.setDosingType(this.getClass());
		order.setDosingInstructions(this.getDosingInstructionsAsString(Locale.getDefault()));
	}

	/**
	 * Get human-readable version of <b>ChemoDosingInstructions</b> from <b>DrugOrder</b> object
	 * 
	 * @param order DrugOrder to get dosing instructions
	 * @throws APIException if dosing type of passing order is not matched with dosing type of <tt>"ChemoDosingInstructions"</tt>.
	 * 
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public DosingInstructions getDosingInstructions(DrugOrder order) {
		if (!order.getDosingType().equals(this.getClass())) {
			throw new APIException("DrugOrder.error.dosingTypeIsMismatched", new Object[] { this.getClass().getName(),
				order.getDosingType() });
		}
		
		ChemoDosingInstructions chemoDosingInstructions = new ChemoDosingInstructions();
		StringTokenizer tokenizeDosingInstructions = new StringTokenizer(order.getDosingInstructions(), "\n");
		
		while (tokenizeDosingInstructions.hasMoreTokens()) {
			String instructionLine = tokenizeDosingInstructions.nextToken();
			int indexOfColon = instructionLine.indexOf(':');
			String inLineType = instructionLine.substring(0, indexOfColon).trim();
			String inLineValue = instructionLine.substring(indexOfColon + 1).trim();
			if (inLineType.equals(ADJUSTMENT_PERCENTAGE)) {
				chemoDosingInstructions.dosageAdjustmentPercentage = Double.parseDouble(inLineValue);
			} else if (inLineType.equals(DELIVERED_TIMING)) {
				chemoDosingInstructions.dosageDeliveredTiming = inLineValue;
			} else {
				chemoDosingInstructions.dosageDilutionInstructions = inLineValue;
			}
		}
		return chemoDosingInstructions;
	}

	/**
	 * @see DosingInstructions#validate(DrugOrder, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(DrugOrder order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "dosingInstructions",
			"DrugOrder.error.dosingInstructionsIsNullForDosingTypeChemo");
	}

	/**
	 * @see DosingInstructions#getAutoExpireDate(DrugOrder)
	 */
	@Override
	public Date getAutoExpireDate(DrugOrder order) {
		return null;
	}

	/**
	 * Returns the formatted string to be appended to dosing instructions
	 * @param instructionType part to be formatted
	 * @param value part to be formatted
	 *
	 * @return String
	 */
	private String formatDosingInformation(String instructionType,String value){
		return instructionType + " : " + value + "\n";
	}

	/**
	 * Returns the dosage adjustment percentage
	 *
	 * @return Integer
	 */
	public Double getDosageAdjustmentPercentage() {
		return this.dosageAdjustmentPercentage;
	}

	/**
	 * Sets the dosage adjustment percentage
	 *
	 * @param dosageAdjustmentPercentage to set
	 */
	public void setDosageAdjustmentPercentage(Double dosageAdjustmentPercentage) {
		this.dosageAdjustmentPercentage = dosageAdjustmentPercentage;
	}

	/**
	 * Returns the dosage delivered timing
	 *
	 * @return String
	 */
	public String getDosageDeliveredTiming() {
		return this.dosageDeliveredTiming;
	}

	/**
	 * Sets the dosage delivered timing
	 *
	 * @param dosageDeliveredTiming to set
	 */
	public void setDosageDeliveredTiming(String dosageDeliveredTiming) {
		this.dosageDeliveredTiming = dosageDeliveredTiming;
	}

	/**
	 * Returns the dosage dilution instructions
	 *
	 * @return String
	 */
	public String getDosageDilutionInstructions() {
		return this.dosageDilutionInstructions;
	}

	/**
	 * Sets the dosage dilution instructions
	 *
	 * @param dosageDilutionInstructions to set
	 */
	public void setDosageDilutionInstructions(String dosageDilutionInstructions) {
		this.dosageDilutionInstructions = dosageDilutionInstructions;
	}
}
