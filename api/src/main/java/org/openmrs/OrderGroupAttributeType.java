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

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class OrderGroupAttributeType extends BaseAttributeType<OrderGroup> implements AttributeType<OrderGroup> {
	
	private Integer orderGroupAttributeTypeId;
	
	private Integer cycleNumber;
	
	private String category;
	
	private Integer numberOfCyclesInTheRegimen;
	
	private Integer lengthOfCyclesInTheRegimen;
	
	private String priorOrderGroup;

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderGroupAttributeTypeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderGroupAttributeTypeId(id);
	}

	/**
	 * @return the orderGroupAttributeTypeId
	 */
	public Integer getOrderGroupAttributeTypeId() {
		return orderGroupAttributeTypeId;
	}

	/**
	 * @param orderGroupAttributeTypeId the orderGroupAttributeTypeId to set
	 */
	public void setOrderGroupAttributeTypeId(Integer orderGroupAttributeTypeId) {
		this.orderGroupAttributeTypeId = orderGroupAttributeTypeId;
	}

	/**
	 * @return the cycleNumber
	 */
	public Integer getCycleNumber() {
		return cycleNumber;
	}

	/**
	 * @param cycleNumber the cycleNumber to set
	 */
	public void setCycleNumber(Integer cycleNumber) {
		this.cycleNumber = cycleNumber;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the numberOfCyclesInTheRegimen
	 */
	public Integer getNumberOfCyclesInTheRegimen() {
		return numberOfCyclesInTheRegimen;
	}
	
	/**
	 * @param numberOfCyclesInTheRegimen the number Of cycles in the Regimen to set
	 */
	public void setNumberOfCyclesInTheRegimen(Integer numberOfCyclesInTheRegimen) {
		this.numberOfCyclesInTheRegimen = numberOfCyclesInTheRegimen;
	}

	/**
	 * @return the lengthOfCyclesInTheRegimen
	 */
	public Integer getLengthOfCyclesInTheRegimen() {
		return lengthOfCyclesInTheRegimen;
	}

	/**
	 * @param lengthOfCyclesInTheRegimen the length of cycles in the Regimen to set
	 */
	public void setLengthOfCyclesInTheRegimen(Integer lengthOfCyclesInTheRegimen) {
		this.lengthOfCyclesInTheRegimen = lengthOfCyclesInTheRegimen;
	}

	/**
	 * @return the priorOrderGroup
	 */
	public String getPriorOrderGroup() {
		return priorOrderGroup;
	}

	/**
	 * @param priorOrderGroup the prior order group to set
	 */
	public void setPriorOrderGroup(String priorOrderGroup) {
		this.priorOrderGroup = priorOrderGroup;
	}
}
