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

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

/**
 * A value for a user-defined {@link OrderGroupAttributeType} that is stored in an {@link OrderGroup}.
 * @see Attribute
 * @since 2.4.0
 */
public class OrderGroupAttribute extends BaseAttribute<OrderGroupAttributeType, OrderGroup> implements Attribute<OrderGroupAttributeType, OrderGroup> {

	private Integer orderGroupAttributeId;
	
	/**
	 * @return the order group attribute Id
	 */
	public Integer getOrderGroupAttributeId() {
		return orderGroupAttributeId;
	}

	/**
	 * @param orderGroupAttributeId the order group attribute Id to set
	 */
	public void setOrderGroupAttributeId(Integer orderGroupAttributeId) {
		this.orderGroupAttributeId = orderGroupAttributeId;
	}

	/**
	 * @return the order group
	 */
	public OrderGroup getOrderGroup(){
		return getOwner();
	}
	
	/**
	 * @param orderGroup the order group to set
	 */
	public void setOrderGroup(OrderGroup orderGroup){
		setOwner(orderGroup);
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderGroupAttributeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderGroupAttributeId(id);
	}
}
