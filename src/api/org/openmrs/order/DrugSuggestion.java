/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
