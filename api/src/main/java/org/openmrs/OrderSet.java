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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.APIException;

/**
 * Represents the grouping of orders into a set,
 * so as to give decision support for the doctors
 * 
 * @since 1.12
 */
public class OrderSet extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 72232L;
	
	/**
	 * Restrictions put on saving an orderSet.
	 * ALL: All the members of the orderSet need to be selected for saving
	 * ONE: Only one of the member of the orderSet needs to be selected for saving
	 * ANY: Any of the members of the orderSet can be selected for saving
	 */
	public enum Operator {
		ALL, ONE, ANY
	}
	
	private Integer orderSetId;
	
	private Operator operator;
	
	private List<OrderSetMember> orderSetMembers;
	
	/**
	 * Gets the orderSetId
	 *
	 * @return the orderSetId
	 */
	public Integer getOrderSetId() {
		return orderSetId;
	}
	
	/**
	 * Sets the orderSetId
	 *
	 * @param orderSetId the orderSetId to set
	 */
	public void setOrderSetId(Integer orderSetId) {
		this.orderSetId = orderSetId;
	}
	
	/**
	 * Gets the operator
	 *
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}
	
	/**
	 * Sets the operator
	 *
	 * @param operator the operator to set
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	/**
	 * Gets the orderSetMembers
	 *
	 * @return the orderSetMembers
	 */
	public List<OrderSetMember> getOrderSetMembers() {
		if (orderSetMembers == null) {
			orderSetMembers = new ArrayList<>();
		}
		return orderSetMembers;
	}
	
	/**
	 * Sets the orderSetMembers
	 *
	 * @param orderSetMembers the orderSetMembers to set
	 */
	public void setOrderSetMembers(List<OrderSetMember> orderSetMembers) {
		this.orderSetMembers = orderSetMembers;
	}
	
	/**
	 * Adds an orderSetMember to the existing list of orderSetMembers
	 * 
	 * @param orderSetMember the new orderSetMember to be added
	 * @param position the position where it is to be added, if position is null it adds to the last position 
	 */
	
	public void addOrderSetMember(OrderSetMember orderSetMember, Integer position) {
		Integer listIndex = findListIndexForGivenPosition(position);
		orderSetMember.setOrderSet(this);
		getOrderSetMembers().add(listIndex, orderSetMember);
	}
	
	private Integer findListIndexForGivenPosition(Integer position) {
		Integer size = getOrderSetMembers().size();
		if (position != null) {
			if (position < 0 && position >= (-1 - size)) {
				position = position + size + 1;
			} else if (position > size) {
				throw new APIException("Cannot add a member which is out of range of the list");
			}
		} else {
			position = size;
		}
		return position;
	}
	
	/**
	 * Adds an orderSetMember to the existing list of orderSetMembers
	 *
	 * @param orderSetMember the new orderSetMember to be added at the end of the current list of order set members
	 */
	
	public void addOrderSetMember(OrderSetMember orderSetMember) {
		this.addOrderSetMember(orderSetMember, null);
	}
	
	@Override
	public Integer getId() {
		return getOrderSetId();
	}
	
	@Override
	public void setId(Integer id) {
		setOrderSetId(id);
	}

	/**
	 * Fetches the list of orderSetMembers that are not retired
	 *
	 * @return the orderSetMembers that are not retired
	 */
	public List<OrderSetMember> getUnRetiredOrderSetMembers() {
		List<OrderSetMember> osm = new ArrayList<>();
		for (OrderSetMember orderSetMember : getOrderSetMembers()) {
			if (!orderSetMember.getRetired()) {
				osm.add(orderSetMember);
			}
		}
		return osm;
	}

	/**
	 * Removes and orderSetMember from a list of existing orderSetMembers
	 *
	 * @param orderSetMember that is to be removed
	 */
	public void removeOrderSetMember(OrderSetMember orderSetMember) {
		if (getOrderSetMembers().contains(orderSetMember)) {
			getOrderSetMembers().remove(orderSetMember);
			orderSetMember.setOrderSet(null);
		}
	}

	/**
	 * Retires an orderSetMember
	 *
	 * @param orderSetMember to be retired
	 */
	public void retireOrderSetMember(OrderSetMember orderSetMember) {
		orderSetMember.setRetired(true);
	}
	
}
