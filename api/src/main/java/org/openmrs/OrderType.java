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

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Independent;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * OrderTypes are used to classify different types of Orders e.g to distinguish between Serology and
 * Radiology TestOrders
 *
 */
public class OrderType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 23232L;
	
	public static final String DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	
	public static final String TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
	
	private Integer orderTypeId;
	
	private String javaClassName;
	
	private OrderType parent;
	
	@Independent
	private Collection<ConceptClass> conceptClasses;
	
	/**
	 * default constructor
	 */
	public OrderType() {
	}
	
	/**
	 * Constructor with ID
	 * 
	 * @param orderTypeId the ID of the {@link org.openmrs.OrderType}
	 */
	public OrderType(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}
	
	/**
	 * Convenience constructor that takes in the elements required to save this OrderType to the
	 * database
	 * 
	 * @param name The name of this order Type
	 * @param description A short description about this order type
	 * @param javaClassName The fully qualified java class name
	 */
	public OrderType(String name, String description, String javaClassName) {
		setName(name);
		setDescription(description);
		setJavaClassName(javaClassName);
	}
	
	/**
	 * @return Returns the orderTypeId.
	 */
	public Integer getOrderTypeId() {
		return orderTypeId;
	}
	
	/**
	 * @param orderTypeId The orderTypeId to set.
	 */
	public void setOrderTypeId(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderTypeId(id);
		
	}
	
	/**
	 * @return Returns the Java className as String
	 */
	public String getJavaClassName() {
		return javaClassName;
	}
	
	/**
	 * @param javaClassName The Java class to set as String
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}
	
	/**
	 * @return Returns the {@link org.openmrs.OrderType}
	 */
	public OrderType getParent() {
		return parent;
	}
	
	/**
	 * @param parent The {@link org.openmrs.OrderType} to set
	 */
	public void setParent(OrderType parent) {
		this.parent = parent;
	}
	
	/**
	 * @return Get the {@link org.openmrs.ConceptClass}es
	 */
	public Collection<ConceptClass> getConceptClasses() {
		if (conceptClasses == null) {
			conceptClasses = new LinkedHashSet<ConceptClass>();
		}
		return conceptClasses;
	}
	
	/**
	 * @param conceptClasses the collection containing the {@link org.openmrs.ConceptClass}es
	 */
	public void setConceptClasses(Collection<ConceptClass> conceptClasses) {
		this.conceptClasses = conceptClasses;
	}
	
	/**
	 * Convenience method that returns a {@link java.lang.Class} object for the associated
	 * javaClassName
	 * 
	 * @return The Java class as {@link java.lang.Class}
	 * @throws APIException
	 */
	public Class getJavaClass() throws APIException {
		try {
			return Context.loadClass(javaClassName);
		}
		catch (ClassNotFoundException e) {
			//re throw as a runtime exception
			throw new APIException("OrderType.failed.load.class", new Object[] { javaClassName }, e);
		}
	}
	
	/**
	 * Convenience method that adds the specified concept class
	 * 
	 * @param conceptClass the ConceptClass to add
	 * @should add the specified concept class
	 * @should not add a duplicate concept class
	 */
	public void addConceptClass(ConceptClass conceptClass) {
		getConceptClasses().add(conceptClass);
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsObject#toString()
	 */
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(getName())) {
			return getName();
		}
		return super.toString();
	}
}
