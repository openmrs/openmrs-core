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
 * Contains a group of {@link org.openmrs.Order}s that are ordered together within a single encounter,often driven by an {@link org.openmrs.OrderSet}. 
 * Not all orders in an encounter need to be grouped this way, only those that have a specific connection to each other 
 * (e.g. several orders that together make up a treatment protocol for some diagnosis could be grouped).
 * 
 * @since 1.12
 */
public class OrderGroup extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 72232L;
	
	private Integer orderGroupId;
	
	private Patient patient;
	
	private Encounter encounter;
	
	private List<Order> orders;
	
	private OrderSet orderSet;
	
	/**
	 * Gets the orderGroupId
	 *
	 * @return the orderGroupId
	 */
	public Integer getOrderGroupId() {
		return orderGroupId;
	}
	
	/**
	 * Sets the orderGroupId
	 *
	 * @param orderGroupId the orderGroupId to set
	 */
	public void setOrderGroupId(Integer orderGroupId) {
		this.orderGroupId = orderGroupId;
	}
	
	/**
	 * Gets the patient
	 *
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * Sets the patient
	 *
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * Gets the encounter
	 *
	 * @return the encounter
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * Sets the encounter
	 *
	 * @param encounter the encounter to set
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	/**
	 * Gets the orders
	 *
	 * @return the orders
	 */
	public List<Order> getOrders() {
		if (orders == null) {
			orders = new ArrayList<>();
		}
		return orders;
	}
	
	/**
	 * Sets the orders
	 *
	 * @param orders the orders to set
	 */
	public void setOrders(List<Order> orders) {
		for (Order order : orders) {
			addOrder(order);
		}
	}
	
	/**
	 * Adds a new order to the existing list of orders
	 * 
	 * @param order the new order to be added
	 */
	public void addOrder(Order order) {
		this.addOrder(order, null);
	}
	
	/**
	 * Adds a new order to the existing list of orders.
	 * The <tt>sortWeight</tt> for the order is auto calculated based on the given position.
	 * 
	 * @param order the new order to be added
	 * @param position the position where the order has to be added
	 */
	public void addOrder(Order order, Integer position) {
		if (order == null || getOrders().contains(order)) {
			return;
		}
                
                order.setOrderGroup(this);  
                 
		Integer listIndex = findListIndexForGivenPosition(position);
		getOrders().add(listIndex, order);
		if (order.getSortWeight() == null) {
			order.setSortWeight(findSortWeight(listIndex));
		}
	}
	
	private Integer findListIndexForGivenPosition(Integer position) {
		Integer size = getOrders().size();
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
	
	private double findSortWeight(int index) {
		int size = getOrders().size();
		if (size == 1) {
			return 10.0;
		}
		if (index == 0) {
			return getOrders().get(1).getSortWeight() / 2;
		}
		if (index == size - 1) {
			return getOrders().get(index - 1).getSortWeight() + 10.0;
		}
		return (getOrders().get(index - 1).getSortWeight() + getOrders().get(index + 1).getSortWeight()) / 2;
	}
	
	/**
	 * Gets the orderSet
	 *
	 * @return the orderSet
	 */
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	/**
	 * Sets the orderSet
	 *
	 * @param orderSet the orderSet to set
	 */
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	@Override
	public Integer getId() {
		return getOrderGroupId();
	}
	
	@Override
	public void setId(Integer id) {
		setOrderGroupId(id);
	}
	
}
