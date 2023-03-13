/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.order;

public class DrugSuggestion {
	
	private String drugId;
	
	private String dose;
	
	private String units;
	
	private String frequency;
	
	private String instructions;
	
	/**
	 * @return Returns the dose.
	 */
	public String getDose() {
		return dose;
	}
	
	/**
	 * @param dose The dose to set.
	 */
	public void setDose(String dose) {
		this.dose = dose;
	}
	
	/**
	 * @return Returns the drugId.
	 */
	public String getDrugId() {
		return drugId;
	}
	
	/**
	 * @param drugId The drugId to set.
	 */
	public void setDrugId(String drugId) {
		this.drugId = drugId;
	}
	
	/**
	 * @return Returns the frequency.
	 */
	public String getFrequency() {
		return frequency;
	}
	
	/**
	 * @param frequency The frequency to set.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * @return Returns the instructions.
	 */
	public String getInstructions() {
		return instructions;
	}
	
	/**
	 * @param instructions The instructions to set.
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
	/**
	 * @return Returns the units.
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * @param units The units to set.
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
}
