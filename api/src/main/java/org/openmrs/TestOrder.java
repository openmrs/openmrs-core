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
 * This is a type of order that adds tests specific attributes like: specimen source, laterality,
 * clinical history, etc.
 * 
 * @since 1.9
 */
public class TestOrder extends Order {
	
	public static final long serialVersionUID = 1L;
	
	private Integer testOrderId;
	
	private Integer specimenSource;
	
	private Integer laterality;
	
	private String clinicalHistory;
	
	public TestOrder(Integer testOrderId, Integer orderId) {
		setTestOrderId(testOrderId);
		setOrderId(orderId);
	}
	
	public Integer getTestOrderId() {
		return testOrderId;
	}
	
	public void setTestOrderId(Integer testOrderId) {
		this.testOrderId = testOrderId;
	}
	
	public Integer getSpecimenSource() {
		return specimenSource;
	}
	
	public void setSpecimenSource(Integer specimenSource) {
		this.specimenSource = specimenSource;
	}
	
	public Integer getLaterality() {
		return laterality;
	}
	
	public void setLaterality(Integer laterality) {
		this.laterality = laterality;
	}
	
	public String getClinicalHistory() {
		return clinicalHistory;
	}
	
	public void setClinicalHistory(String clinicalHistory) {
		this.clinicalHistory = clinicalHistory;
	}
	
	/**
	 * @see org.openmrs.Order#getId()
	 */
	public Integer getId() {
		return getTestOrderId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setTestOrderId(id);
	}
}
