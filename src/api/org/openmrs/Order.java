package org.openmrs;

import java.util.Date;

/**
 * Order 
 */
public class Order implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer orderId;
	private OrderType orderType;
	private Concept concept;
	private String instructions;
	private Date startDate;
	private Date autoExpireDate;
	private Encounter encounter;
	private User orderer;
	private User creator;
	private Date dateCreated;
	private User discontinuedBy;
	private Date discontinuedDate;
	private String discontinuedReason;

	
	// Constructors

	/** default constructor */
	public Order() {
	}

	/** constructor with id */
	public Order(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * true/false whether or not this is a drug order
	 * overridden in extending class drugOrders. 
	 */
	public Boolean isDrugOrder() {
		return false;
	}
	
	// Property accessors

	/**
	 * 
	 */
	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public String getInstructions() {
		return this.instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * 
	 */
	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * 
	 */
	public Date getAutoExpireDate() {
		return this.autoExpireDate;
	}

	public void setAutoExpireDate(Date autoExpireDate) {
		this.autoExpireDate = autoExpireDate;
	}

	/**
	 * 
	 */
	public Date getDiscontinuedDate() {
		return this.discontinuedDate;
	}

	public void setDiscontinuedDate(Date discontinuedDate) {
		this.discontinuedDate = discontinuedDate;
	}

	/**
	 * 
	 */
	public String getDiscontinuedReason() {
		return this.discontinuedReason;
	}

	public void setDiscontinuedReason(String discontinuedReason) {
		this.discontinuedReason = discontinuedReason;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Encounter getEncounter() {
		return this.encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/**
	 * 
	 */
	public User getOrderer() {
		return this.orderer;
	}

	public void setOrderer(User orderer) {
		this.orderer = orderer;
	}

	/**
	 * 
	 */
	public User getDiscontinuedBy() {
		return this.discontinuedBy;
	}

	public void setDiscontinuedBy(User discontinuedBy) {
		this.discontinuedBy = discontinuedBy;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * 
	 */
	public OrderType getOrderType() {
		return this.orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

}