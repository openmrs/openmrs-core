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
