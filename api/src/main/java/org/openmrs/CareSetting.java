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

public class CareSetting implements java.io.Serializable {
	
	private static final String OUT_PATIENT = "OUTPATIENT";
	
	private static final String IN_PATIENT = "INPATIENT";
	
	public static CareSetting OUTPATIENT = new CareSetting(OUT_PATIENT);
	
	public static CareSetting INPATIENT = new CareSetting(IN_PATIENT);
	
	private String careSetting;
	
	public CareSetting() {
		this.careSetting = OUT_PATIENT;
	}
	
	public CareSetting(String careSetting) {
		this.careSetting = careSetting;
	}
	
	public String getCareSetting() {
		return careSetting;
	}
	
	public void setCareSetting(String careSetting) {
		this.careSetting = careSetting;
	}
	
	@Override
	public String toString() {
		return careSetting;
	}
}
