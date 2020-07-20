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
 * A value for a user-defined {@link OrderSetAttributeType} that is stored on a {@link OrderSet}.
 * @see Attribute
 * @since 2.4.0
 */
public class OrderSetAttribute extends BaseAttribute<OrderSetAttributeType, OrderSet> implements Attribute<OrderSetAttributeType, OrderSet>{
	
	private static final long serialVersionUID = 1L;
		
	private Integer orderSetAttributeId;


	/**
	 * @return the orderSetAttributeId
	 */
	public Integer getOrderSetAttributeId() {
		return orderSetAttributeId;
	}

	/**
	 * @param orderSetAttributeId the orderSetAttributeId to set
	 */
	public void setOrderSetAttributeId(Integer orderSetAttributeId) {
		this.orderSetAttributeId = orderSetAttributeId;
	}

	/**
	 * @return the orderSet
	 */
	public OrderSet getOrderSet() {
		return getOwner();
	}

	/**
	 * @param orderSet the orderSet to set
	 */
	public void setOrderSet(OrderSet orderSet) {
		setOwner(orderSet);
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderSetAttributeId();
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderSetAttributeId(id);
	}

}
