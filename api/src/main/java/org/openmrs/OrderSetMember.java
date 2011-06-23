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
	
	/**
	 * selected == true means that the member is ordered by default e.g., an
	 * order set may contain a list of common post-surgical analgesics with the
	 * preferred treatment already selected as the one to be.
	 */
	private Boolean selected;
	
	private String orderSetMemberType;
	
	/**
	 * Refers to an orderable concept without any further detail e.g., a
	 * reference to AMPICILLIN without any pre-defined instructions (you just go
	 * through ordering ampicillin as if you searched for it & selected as a new
	 * order, which could pull up an empty order form or could pull up an order
	 * set linked to AMPICILLIN).
	 */
	private Concept concept;
	
	/**
	 * A pre-defined order, optionally including choices/defaults for various
	 * components of the order.
	 */
	private String orderTemplate;
	
	/**
	 * A list of orders nested within the current order set e.g., the first of
	 * several alternative regimens. Anonymous nested order sets may also be
	 * used to group orders to control selection (via the operator property) or
	 * to simply add a title or comment to a subset of orders within an order
	 * set.
	 */
	private OrderSet orderSet;
	
	/**
	 * Directs the user to select any orderable within a given concept class
	 * e.g., any radiology test.
	 */
	private ConceptClass conceptClass;
	
	/**
	 * Directs the user to select from members of the order set e.g., choose a
	 * drug from any of the BETA BLOCKERS.
	 */
	private ConceptSet conceptSet;
	
	/**
	 * Constructs an order set member with a given order set member id and
	 * parent order set.
	 * 
	 * @param orderSetMemberId
	 *            the order set member id.
	 * @param parentOrderSet
	 *            the parent order set.
	 */
	public OrderSetMember(Integer orderSetMemberId, OrderSet parentOrderSet) {
		setOrderSetMemberId(orderSetMemberId);
		setParentOrderSet(parentOrderSet);
	}
	
	/**
	 * Gets the order set member id.
	 * 
	 * @return the order set member id.
	 */
	public Integer getOrderSetMemberId() {
		return orderSetMemberId;
	}
	
	/**
	 * Sets the order set member id.
	 * 
	 * @param orderSetMemberId
	 *            the order set member id.
	 */
	public void setOrderSetMemberId(Integer orderSetMemberId) {
		this.orderSetMemberId = orderSetMemberId;
	}
	
	/**
	 * Gets the parent order set.
	 * 
	 * @return the parent order set.
	 */
	public OrderSet getParentOrderSet() {
		return parentOrderSet;
	}
	
	/**
	 * Sets the parent order set.
	 * 
	 * @param parentOrderSet
	 *            the parent order set to set.
	 */
	public void setParentOrderSet(OrderSet parentOrderSet) {
		this.parentOrderSet = parentOrderSet;
	}
	
	/**
	 * Gets the sort weight.
	 * 
	 * @return the sort weight.
	 */
	public Integer getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * Sets the sort weight.
	 * 
	 * @param sortWeight
	 *            the sort weight to set.
	 */
	public void setSortWeight(Integer sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * Gets the exclusion logic.
	 * 
	 * @return the exclusion logic.
	 */
	public String getExclusionLogic() {
		return exclusionLogic;
	}
	
	/**
	 * Sets the exclusion logic.
	 * 
	 * @param exclusionLogic
	 *            the exclusion logic to set.
	 */
	public void setExclusionLogic(String exclusionLogic) {
		this.exclusionLogic = exclusionLogic;
	}
	
	/**
	 * Gets the contextual logic.
	 * 
	 * @return the contextual logic.
	 */
	public String getContextualLogic() {
		return contextualLogic;
	}
	
	/**
	 * Sets the contextual logic.
	 * 
	 * @param contextualLogic
	 *            the contextual logic to set.
	 */
	public void setContextualLogic(String contextualLogic) {
		this.contextualLogic = contextualLogic;
	}
	
	/**
	 * Gets the title.
	 * 
	 * @return the title.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the comment.
	 * 
	 * @return the comment.
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            the comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Gets the evidence.
	 * 
	 * @return the evidence.
	 */
	public String getEvidence() {
		return evidence;
	}
	
	/**
	 * Sets the evidence.
	 * 
	 * @param evidence
	 *            the evidence to set.
	 */
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	
	/**
	 * Gets the priority.
	 * 
	 * @return the priority.
	 */
	public Double getPriority() {
		return priority;
	}
	
	/**
	 * Sets the priority.
	 * 
	 * @param priority
	 *            the priority to set.
	 */
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	/**
	 * Gets the indication.
	 * 
	 * @return the indication.
	 */
	public Integer getIndication() {
		return indication;
	}
	
	/**
	 * Sets the indication.
	 * 
	 * @param indication
	 *            the indication to set.
	 */
	public void setIndication(Integer indication) {
		this.indication = indication;
	}
	
	/**
	 * Gets the selected property.
	 * 
	 * @return the selected property.
	 */
	public Boolean getSelected() {
		return selected;
	}
	
	/**
	 * Sets the selected property.
	 * 
	 * @param selected
	 *            the selected value.
	 */
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Gets the order set member type.
	 * 
	 * @return the order set member type.
	 */
	public String getOrderSetMemberType() {
		return orderSetMemberType;
	}
	
	/**
	 * Sets the order set member type.
	 * 
	 * @param orderSetMemberType
	 *            the order set member type to set.
	 */
	public void setOrderSetMemberType(String orderSetMemberType) {
		this.orderSetMemberType = orderSetMemberType;
	}
	
	/**
	 * Gets the concept.
	 * 
	 * @return the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Sets the concept.
	 * 
	 * @param concept
	 *            the concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * Gets the order template.
	 * 
	 * @return the order template.
	 */
	public String getOrderTemplate() {
		return orderTemplate;
	}
	
	/**
	 * Sets the order template.
	 * 
	 * @param orderTemplate
	 *            the order template to set.
	 */
	public void setOrderTemplate(String orderTemplate) {
		this.orderTemplate = orderTemplate;
	}
	
	/**
	 * Gets the order set.
	 * 
	 * @return the order set.
	 */
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	/**
	 * Sets the order set.
	 * 
	 * @param orderSet
	 *            the order set to set.
	 */
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	/**
	 * Gets the concept class.
	 * 
	 * @return the concept class.
	 */
	public ConceptClass getConceptClass() {
		return conceptClass;
	}
	
	/**
	 * Sets the concept class.
	 * 
	 * @param conceptClass
	 *            the concept class to set.
	 */
	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}
	
	/**
	 * Gets the concept set.
	 * 
	 * @return the concept set.
	 */
	public ConceptSet getConceptSet() {
		return conceptSet;
	}
	
	/**
	 * Sets the concept set.
	 * 
	 * @param conceptSet
	 *            the concept set.
	 */
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
