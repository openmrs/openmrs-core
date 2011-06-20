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
 * This represents a single member within an order set.
 * 
 * @since 1.9
 */
public class OrderSetMember extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final long serialVersionUID = 1L;
	
	private Integer orderSetMemberId;
	
	private OrderSet parentOrderSet;
	
	private Integer sortWeight;
	
	private String exclusionLogic;
	
	private String contextualLogic;
	
	private String title;
	
	private String comment;
	
	private String evidence;
	
	private Double priority;
	
	private Integer indication;
	
	private Boolean selected;
	
	private String orderSetMemberType;
	
	private Concept concept;
	
	private String orderTemplate;
	
	private OrderSet orderSet;
	
	private ConceptClass conceptClass;
	
	private ConceptSet conceptSet;
	
	public OrderSetMember(Integer orderSetMemberId, OrderSet parentOrderSet) {
		setOrderSetMemberId(orderSetMemberId);
		setParentOrderSet(parentOrderSet);
	}
	
	public Integer getOrderSetMemberId() {
		return orderSetMemberId;
	}
	
	public void setOrderSetMemberId(Integer orderSetMemberId) {
		this.orderSetMemberId = orderSetMemberId;
	}
	
	public OrderSet getParentOrderSet() {
		return parentOrderSet;
	}
	
	public void setParentOrderSet(OrderSet parentOrderSet) {
		this.parentOrderSet = parentOrderSet;
	}
	
	public Integer getSortWeight() {
		return sortWeight;
	}
	
	public void setSortWeight(Integer sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	public String getExclusionLogic() {
		return exclusionLogic;
	}
	
	public void setExclusionLogic(String exclusionLogic) {
		this.exclusionLogic = exclusionLogic;
	}
	
	public String getContextualLogic() {
		return contextualLogic;
	}
	
	public void setContextualLogic(String contextualLogic) {
		this.contextualLogic = contextualLogic;
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
	
	public String getEvidence() {
		return evidence;
	}
	
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	
	public Double getPriority() {
		return priority;
	}
	
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public Integer getIndication() {
		return indication;
	}
	
	public void setIndication(Integer indication) {
		this.indication = indication;
	}
	
	public Boolean getSelected() {
		return selected;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	
	public String getOrderSetMemberType() {
		return orderSetMemberType;
	}
	
	public void setOrderSetMemberType(String orderSetMemberType) {
		this.orderSetMemberType = orderSetMemberType;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getOrderTemplate() {
		return orderTemplate;
	}
	
	public void setOrderTemplate(String orderTemplate) {
		this.orderTemplate = orderTemplate;
	}
	
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	public ConceptClass getConceptClass() {
		return conceptClass;
	}
	
	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}
	
	public ConceptSet getConceptSet() {
		return conceptSet;
	}
	
	public void setConceptSet(ConceptSet conceptSet) {
		this.conceptSet = conceptSet;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderSetMemberId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderSetMemberId(id);
	}
}
