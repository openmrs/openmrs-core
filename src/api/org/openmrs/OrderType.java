package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * OrderType 
 */
public class OrderType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer orderTypeId;
	private String name;
	private String description;
	private Date dateCreated;
	private User creator;
	private Set orders;

	// Constructors

	/** default constructor */
	public OrderType() {
	}

	/** constructor with id */
	public OrderType(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getOrderTypeId() {
		return this.orderTypeId;
	}

	public void setOrderTypeId(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * 
	 */
	public Set getOrders() {
		return this.orders;
	}

	public void setOrders(Set orders) {
		this.orders = orders;
	}

}