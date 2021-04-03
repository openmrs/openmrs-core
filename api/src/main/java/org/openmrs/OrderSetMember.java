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
 * @since 1.12
 * OrderSetMember
 */
public class OrderSetMember extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 72232L;
	
	private Integer orderSetMemberId;
	
	private OrderType orderType;
	
	private OrderSet orderSet;
	
	private String orderTemplate;
	
	private String orderTemplateType;
	
	private Concept concept;
	
	/**
	 * Gets the orderSetMemberId
	 *
	 * @return the orderSetMemberId
	 */
	public Integer getOrderSetMemberId() {
		return orderSetMemberId;
	}
	
	/**
	 * Sets the orderSetMemberId
	 *
	 * @param orderSetMemberId the orderSetMemberId to set
	 */
	public void setOrderSetMemberId(Integer orderSetMemberId) {
		this.orderSetMemberId = orderSetMemberId;
	}
	
	/**
	 * Gets the orderType
	 *
	 * @return the orderType
	 */
	public OrderType getOrderType() {
		return orderType;
	}
	
	/**
	 * Sets the orderType
	 *
	 * @param orderType the orderType to set
	 */
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	
	/**
	 * Gets the orderTemplate
	 *
	 * @return the orderTemplate
	 */
	public String getOrderTemplate() {
		return orderTemplate;
	}
	
	/**
	 * Sets the orderTemplate
	 *
	 * @param orderTemplate the orderTemplate to set
	 */
	public void setOrderTemplate(String orderTemplate) {
		this.orderTemplate = orderTemplate;
	}
	
	/**
	 * Returns the orderTemplateType
	 * 
	 * @return the orderTemplateType
	 */
	public String getOrderTemplateType() {
		return orderTemplateType;
	}
	
	/**
	 * It takes in a name of a handler, which defines the schema of orderTemplate to be generated
	 * 
	 * @param orderTemplateType the orderTemplateType to be set
	 */
	public void setOrderTemplateType(String orderTemplateType) {
		this.orderTemplateType = orderTemplateType;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Sets the concept
	 *
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * Gets the orderSet for the member
	 *
	 * @return the orderSet
	 */
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	/**
	 * Sets the orderSet for the member
	 *
	 * @param orderSet
	 */
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	@Override
	public Integer getId() {
		return getOrderSetMemberId();
	}
	
	@Override
	public void setId(Integer id) {
		setOrderSetMemberId(id);
	}
	
}
