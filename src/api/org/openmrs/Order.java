package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Order 
 */
public class Order implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer orderId;
	private Integer conceptId;
	private String instructions;
	private Date startDate;
	private Date autoExpireDate;
	private Date discontinuedDate;
	private String discontinuedReason;
	private Date dateCreated;
	private DrugOrder drugOrder;
	private Set obs;
	private Encounter encounter;
	private User userByOrderer;
	private User userByDiscontinuedBy;
	private User userByCreator;
	private OrderType orderType;

	// Constructors

	/** default constructor */
	public Order() {
	}

	/** constructor with id */
	public Order(Integer orderId) {
		this.orderId = orderId;
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
	public Integer getConceptId() {
		return this.conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
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
	public DrugOrder getDrugOrder() {
		return this.drugOrder;
	}

	public void setDrugOrder(DrugOrder drugOrder) {
		this.drugOrder = drugOrder;
	}

	/**
	 * 
	 */
	public Set getObs() {
		return this.obs;
	}

	public void setObs(Set obs) {
		this.obs = obs;
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
	public User getUserByOrderer() {
		return this.userByOrderer;
	}

	public void setUserByOrderer(User userByOrderer) {
		this.userByOrderer = userByOrderer;
	}

	/**
	 * 
	 */
	public User getUserByDiscontinuedBy() {
		return this.userByDiscontinuedBy;
	}

	public void setUserByDiscontinuedBy(User userByDiscontinuedBy) {
		this.userByDiscontinuedBy = userByDiscontinuedBy;
	}

	/**
	 * 
	 */
	public User getUserByCreator() {
		return this.userByCreator;
	}

	public void setUserByCreator(User userByCreator) {
		this.userByCreator = userByCreator;
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