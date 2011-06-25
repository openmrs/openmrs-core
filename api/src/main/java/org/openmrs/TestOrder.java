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
	
	private Integer specimenSource;
	
	private Integer laterality;
	
	private String clinicalHistory;
	
	/**
	 * Default Constructor
	 */
	public TestOrder() {
	}
	
	/**
	 * @deprecated
	 * @param orderId the order id.
	 */
	@Deprecated
	public TestOrder(Integer orderId) {
		setOrderId(orderId);
	}
	
	/**
	 * Gets the specimen source.
	 * 
	 * @return the specimen source.
	 */
	public Integer getSpecimenSource() {
		return specimenSource;
	}
	
	/**
	 * Sets the specimen source.
	 * 
	 * @param specimenSource the specimen source to set.
	 */
	public void setSpecimenSource(Integer specimenSource) {
		this.specimenSource = specimenSource;
	}
	
	/**
	 * Gets the laterality.
	 * 
	 * @return the laterality.
	 */
	public Integer getLaterality() {
		return laterality;
	}
	
	/**
	 * Sets the laterality.
	 * 
	 * @param laterality the laterality to set.
	 */
	public void setLaterality(Integer laterality) {
		this.laterality = laterality;
	}
	
	/**
	 * Gets the clinical history.
	 * 
	 * @return the clinical history.
	 */
	public String getClinicalHistory() {
		return clinicalHistory;
	}
	
	/**
	 * Sets the clinical history.
	 * 
	 * @param clinicalHistory the clinical history to set.
	 */
	public void setClinicalHistory(String clinicalHistory) {
		this.clinicalHistory = clinicalHistory;
	}
	
	/**
	 * @see org.openmrs.Order#getId()
	 */
	public Integer getId() {
		return getOrderId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderId(id);
	}
}
