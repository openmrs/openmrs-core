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

import java.util.Collection;
import java.util.Date;

/**
 * Order sets are used to pre-define sets of orders in order to make the ordering process easier
 * i.e., pick from a list instead of having to manually enter orders for common orders or groups of
 * orders. Order sets can contain 0-to-n members; each member can be a reference to an orderable
 * concept, an order template (pre-defined order), or another order set. Order set is a definition
 * of one or more related orders – e.g., "orders to consider when a patient presents with pneumonia"
 * or "common ways to order penicillin", where someone may be picking & choosing from the set of
 * orders. Order group is a set of actual orders that are grouped from some reason (e.g., they were
 * ordered from a pre-defined order set OR we come up with other reasons to group orders). For
 * example, you might define an order set FIRST LINE HIV THERAPIES that described two recommended
 * starting regimens, each of which contained three drugs. You haven't ordered anything for any
 * patient; rather, you've just created metadata defining the set of orders to be considered in a
 * specific situation. Later, a doctor in the midst of placing orders for a patient finds your order
 * set and uses it to select the 2nd regimen. Three drugs (the three in the 2nd regimen) are added
 * to the orders table for the patient and marked as being in an order group (basically tagging
 * those orders as being created from your order set). The order group – much like an obs group – is
 * a grouping mechanism of actual orders for a patient.
 * 
 * @since 1.9
 */
public class OrderSet extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 1L;
	
	private Integer orderSetId;
	
	private Concept concept;
	
	private String title;
	
	private String comment;
	
	private String operator;
	
	private User reviewedBy;
	
	private Date dateReviewed;
	
	private Collection<OrderSetMember> orderSetMembers;
	
	public OrderSet(Integer orderSetId) {
		setOrderSetId(orderSetId);
	}
	
	public Integer getOrderSetId() {
		return orderSetId;
	}
	
	public void setOrderSetId(Integer orderSetId) {
		this.orderSetId = orderSetId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public User getReviewedBy() {
		return reviewedBy;
	}
	
	public void setReviewedBy(User reviewedBy) {
		this.reviewedBy = reviewedBy;
	}
	
	public Date getDateReviewed() {
		return dateReviewed;
	}
	
	public void setDateReviewed(Date dateReviewed) {
		this.dateReviewed = dateReviewed;
	}
	
	public Collection<OrderSetMember> getOrderSetMembers() {
		return orderSetMembers;
	}
	
	public void setOrderSetMembers(Collection<OrderSetMember> orderSetMembers) {
		this.orderSetMembers = orderSetMembers;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderSetId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderSetId(id);
	}
}
