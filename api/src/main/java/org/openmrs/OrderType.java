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

import org.openmrs.api.context.Context;

import java.util.Collection;

/**
 * OrderType
 */
public class OrderType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 23232L;
	
	// Fields
	
	private Integer orderTypeId;
	
	private String javaClass;
	
	private OrderType parent;
	
	private Collection<ConceptClass> conceptClasses;
	
	// Constructors
	
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
	 * @param name        The name of this order Type
	 * @param description A short description about this order type
	 */
	public OrderType(String name, String description, Class javaClass) {
		setName(name);
		setDescription(description);
		setJavaClass(javaClass.toString());
	}
	
	// Property accessors
	
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
	 * @return Returns the Java class as String
	 */
	public String getJavaClass() {
		return javaClass;
	}
	
	/**
	 * Same as the {@link org.openmrs.OrderType#getJavaClass()}, but it returns a {@link java.lang.Class} for convenience
	 *
	 * @return The Java class as {@link java.lang.Class}
	 * @throws ClassNotFoundException
	 */
	public Class getJavaClassObject() throws ClassNotFoundException {
		return Context.loadClass(javaClass);
	}
	
	/**
	 * @param javaClass The Java class to set as String
	 */
	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
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
		return conceptClasses;
	}
	
	/**
	 * @param conceptClasses the collection containing the {@link org.openmrs.ConceptClass}es
	 */
	public void setConceptClasses(Collection<ConceptClass> conceptClasses) {
		this.conceptClasses = conceptClasses;
	}
}
