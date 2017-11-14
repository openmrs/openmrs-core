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

/**
 * Care Setting defines the scope of care for any piece of data within the medical record. Clinical
 * data (treatments, notes, etc.) apply within their associated care setting. Implementations
 * working within a single context (e.g., within a single clinic, a set of coordinated clinics, or a
 * single hospital) may use a single Care Setting for all of their data. Implementations providing
 * care across different care settings (e.g., within clinics and within hospital) may need to define
 * multiple care settings in order to, for example, keep orders within one care setting from
 * affecting orders in the other care setting. Active order lists are scoped to a single care
 * setting. In the simplest example, an implementation would define a care setting for each basic
 * type of setting (INPATIENT, OUTPATIENT, EMERGENCY) they use. If an implementation provides care
 * in two separate hospitals that function independently, then they would use two separate care
 * settings like "Hospital A" and "Hospital B".
 * 
 * @since 1.10
 */
public class CareSetting extends BaseChangeableOpenmrsMetadata {
	
	public enum CareSettingType {
		OUTPATIENT,
		INPATIENT
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
