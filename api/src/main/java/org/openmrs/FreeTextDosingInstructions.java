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
package org.openmrs;

import java.util.Locale;

/**
 * @since 1.10
 */
public class FreeTextDosingInstructions implements DosingInstructions {
	
	private String type = DrugOrder.DOSING_TYPE_FREE_TEXT;
	
	private String instructions;
	
	/**
	 * @see DosingInstructions#getType()
	 */
	@Override
	public String getType() {
		return this.type;
	}
	
	/**
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public String getDosingInstructionsAsString(Locale locale) {
		return this.instructions;
	}
	
	/**
	 * @see DosingInstructions#setDosingInstructions(DrugOrder)
	 */
	@Override
	public void setDosingInstructions(DrugOrder order) {
		order.setDosingType(this.getType());
		order.setDosingInstructions(this.getInstructions());
	}
	
	/**
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public DosingInstructions getDosingInstructions(DrugOrder order) throws Exception {
		if (this.getType() != order.getDosingType()) {
			throw new Exception("Dosing type of drug order is mismatched. Expected:" + this.getType() + " but received:"
			        + order.getDosingType());
		}
		FreeTextDosingInstructions ftdi = new FreeTextDosingInstructions();
		ftdi.setInstructions(order.getDosingInstructions());
		return ftdi;
	}
	
	public String getInstructions() {
		return instructions;
	}
	
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
}
