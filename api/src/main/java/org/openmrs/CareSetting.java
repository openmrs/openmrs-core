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

/**
 *  @since 1.10
 */
public class CareSetting extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public enum CareSettingType {
		OUTPATIENT, INPATIENT
	}
	
	private Integer careSettingId;
	
	private CareSettingType careSettingType;
	
	public CareSetting() {
	}
	
	public CareSetting(String name, String description, CareSettingType careSettingType) {
		setName(name);
		setDescription(description);
		setCareSettingType(careSettingType);
	}
	
	public Integer getCareSettingId() {
		return careSettingId;
	}
	
	public void setCareSettingId(Integer careSettingId) {
		this.careSettingId = careSettingId;
	}
	
	public CareSettingType getCareSettingType() {
		return careSettingType;
	}
	
	public void setCareSettingType(CareSettingType careSettingType) {
		this.careSettingType = careSettingType;
	}
	
	@Override
	public Integer getId() {
		return getCareSettingId();
	}
	
	@Override
	public void setId(Integer id) {
		setCareSettingId(id);
	}
}
