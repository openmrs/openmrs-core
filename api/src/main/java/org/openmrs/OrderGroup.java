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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.api.APIException;



/**
 * Contains a group of {@link org.openmrs.Order}s that are ordered together within a single encounter,often driven by an {@link org.openmrs.OrderSet}. 
 * Not all orders in an encounter need to be grouped this way, only those that have a specific connection to each other 
 * (e.g. several orders that together make up a treatment protocol for some diagnosis could be grouped).
 * 
 * @since 1.12
 */
@Entity
@Table(name = "order_group")
@Audited
public class OrderGroup extends BaseCustomizableData<OrderGroupAttribute> {
	
	public static final long serialVersionUID = 72232L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_group_id_seq")
	@GenericGenerator(name = "order_group_id_seq", strategy = "native", parameters = @Parameter(name = "sequence", value = "order_group_order_group_id_seq"))
	@Column(name = "order_group_id", nullable = false)
	private Integer orderGroupId;
	
	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id", nullable = false)
	private Encounter encounter;

	@Access(AccessType.PROPERTY)
	@OneToMany(mappedBy = "orderGroup", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("voided asc")
	@BatchSize(size = 100)
	private Set<OrderGroupAttribute> attributes = new LinkedHashSet<>();
	
	@ManyToOne
	@JoinColumn(name = "order_set_id")
	private OrderSet orderSet;

	@ManyToOne
	@JoinColumn(name = "parent_order_group")
	private OrderGroup parentOrderGroup;

	@ManyToOne
	@JoinColumn(name = "order_group_reason")
	private Concept orderGroupReason;

	@ManyToOne
	@JoinColumn(name = "previous_order_group")
	private OrderGroup previousOrderGroup;

	@OneToMany(mappedBy = "orderGroup", fetch = FetchType.LAZY)
	@OrderBy("sort_weight asc")
	private List<Order> orders = new ArrayList<>();

	@Access(AccessType.FIELD)
	@OneToMany(mappedBy = "parentOrderGroup", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
	@OrderBy("orderGroupId asc")
	@BatchSize(size = 25)
	private Set<OrderGroup> nestedOrderGroups;

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
		this.orders = orders;
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
	 * Adds {@link Order}s to existing Order list
	 * 
	 * @param orders
	 * @since 2.2
	 */
	public void addOrders(List<Order> orders) {
		for (Order order : orders) {
			addOrder(order);
		}
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
	
	/**
	 * Gets the parent order group to maintain linkages between groups and support group nesting
	 * 
	 * @return the parent order group
	 * @since 2.4.0
	 */
	public OrderGroup getParentOrderGroup() {
		return parentOrderGroup;
	}
	
	/**
	 * Sets the parent order group to maintain linkages between groups and support group nesting
	 * 
	 * @param parentOrderGroup the parent order group to set.
	 * @since 2.4.0
	 */
	public void setParentOrderGroup(OrderGroup parentOrderGroup) {
		this.parentOrderGroup = parentOrderGroup;
	}

	/**
	 * Gets the order group reason which denotes the reason why the group was
	 * ordered
	 * 
	 * @return the order group reason
	 * @since 2.4.0
	 */
	public Concept getOrderGroupReason() {
		return orderGroupReason;
	}

	/**
	 * Sets the order group reason which denotes the reason why the group was
	 * ordered
	 * 
	 * @param orderGroupReason, the order group reason to set
	 * @since 2.4.0
	 */
	public void setOrderGroupReason(Concept orderGroupReason) {
		this.orderGroupReason = orderGroupReason;
	}
	
	/**
	 * Gets the previous order group to other order groups, to maintain linkages
	 * between groups and support group nesting
	 * 
	 * @param returns the previous order group
	 * @since 2.4.0
	 */
	public OrderGroup getPreviousOrderGroup() {
		return previousOrderGroup;
	}

	/**
	 * Sets the previous order group to other order groups, to maintain linkages
	 * between groups and support group nesting
	 * 
	 * @param previousOrderGroup The previous order group to set
	 * @since 2.4.0
	 */
	public void setPreviousOrderGroup(OrderGroup previousOrderGroup) {
		this.previousOrderGroup = previousOrderGroup;
	}
	
	/**
	 * Gets the nested order groups to other order groups, to maintain linkages
	 * between groups and support group nesting
	 * 
	 * @param returns the nested order groups
	 * @since 2.4.0
	 */
	public Set<OrderGroup> getNestedOrderGroups() {
		return this.nestedOrderGroups;
	}
	
	/**
	 * Sets the nested order groups to other order groups, to maintain linkages
	 * between groups and support group nesting.
	 * 
	 * @param nestedOrderGroup The nested order groups to set
	 * @since 2.4.0
	 */
	public void setNestedOrderGroups(Set<OrderGroup> nestedOrderGroups) {
		this.nestedOrderGroups = nestedOrderGroups;
	}
}
