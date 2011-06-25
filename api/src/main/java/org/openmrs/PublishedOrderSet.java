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
 * An {@link OrderSet} that has been published by linking it to a {@link Concept} in the dictionary. Only published
 * {@link OrderSet}s are {@link Orderable}
 */
public class PublishedOrderSet extends BaseOrderable<Order> {
	
	private Integer publishedOrderSetId;
	
	/**
	 * orderSet represents the details of the OrderSet
	 * concept (in BaseOrderable) is the Concept this OrderSet is published as
	 */
	private OrderSet orderSet;
	
	/**
	 * @return the publishedOrderSetId
	 */
	public Integer getPublishedOrderSetId() {
		return publishedOrderSetId;
	}
	
	/**
	 * @param publishedOrderSetId the publishedOrderSetId to set
	 */
	public void setPublishedOrderSetId(Integer publishedOrderSetId) {
		this.publishedOrderSetId = publishedOrderSetId;
	}
	
	/**
	 * @return the orderSet
	 */
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	/**
	 * @param orderSet the orderSet to set
	 */
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	/**
	 * Returns name from orderSet
	 * @see org.openmrs.Orderable#getName()
	 */
	@Override
	public String getName() {
		if (orderSet.getName() != null)
			return orderSet.getName();
		else
			return super.getName();
	}
	
	/**
	 * Returns description from orderSet
	 * @see org.openmrs.Orderable#getDescription()
	 */
	@Override
	public String getDescription() {
		if (orderSet.getDescription() != null)
			return orderSet.getDescription();
		else
			return super.getDescription();
	}
	
}
