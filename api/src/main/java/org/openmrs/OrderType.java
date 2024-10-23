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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.annotation.Independent;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * OrderTypes are used to classify different types of Orders e.g to distinguish between Serology and
 * Radiology TestOrders
 *
 */
@Entity
@Table(name = "order_type")
@Audited
public class OrderType extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 23232L;
	
	public static final String DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7";
	
	public static final String TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e";
	
	public static final String REFERRAL_ORDER_TYPE_UUID = "f1b63696-2b6c-11ec-8d3d-0242ac130003";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_type_id_seq")
	@GenericGenerator(
		name = "order_type_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "order_type_order_type_id_seq")
	)
	@Column(name = "order_type_id", nullable = false)
	private Integer orderTypeId;
	
	@Column(name = "java_class_name", nullable = false)
	private String javaClassName;
	
	@ManyToOne
	@JoinColumn(name = "parent")
	private OrderType parent;
	
	@Independent
	@ManyToMany
	@JoinTable(
		name = "order_type_class_map", 
		joinColumns = @JoinColumn(name = "order_type_id"), 
		inverseJoinColumns = @JoinColumn(name = "concept_class_id"), 
		uniqueConstraints = @UniqueConstraint(columnNames = {"order_type_id", "concept_class_id"})
	)
	private Set<ConceptClass> conceptClasses;
	
	/**
	 * default constructor
	 */
	public OrderType() {
	}
	
	/**
	 * Constructor with ID
	 * 
	 * @param orderTypeId the ID of the {@link OrderType}
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
	 * @see OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderTypeId();
	}
	
	/**
	 * @see OpenmrsObject#setId(Integer)
	 */
	@Override
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
	 * @return Returns the {@link OrderType}
	 */
	public OrderType getParent() {
		return parent;
	}
	
	/**
	 * @param parent The {@link OrderType} to set
	 */
	public void setParent(OrderType parent) {
		this.parent = parent;
	}
	
	/**
	 * @return Get the {@link ConceptClass}es
	 */
	public Set<ConceptClass> getConceptClasses() {
		if (conceptClasses == null) {
			conceptClasses = new LinkedHashSet<>();
		}
		return conceptClasses;
	}
	
	/**
	 * @param conceptClasses the collection containing the {@link ConceptClass}es
	 */
	public void setConceptClasses(Set<ConceptClass> conceptClasses) {
		this.conceptClasses = conceptClasses;
	}
	
	/**
	 * Convenience method that returns a {@link Class} object for the associated
	 * javaClassName
	 * 
	 * @return The Java class as {@link Class}
	 * @throws APIException
	 */
	public Class getJavaClass() {
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
	 * <strong>Should</strong> add the specified concept class
	 * <strong>Should</strong> not add a duplicate concept class
	 */
	public void addConceptClass(ConceptClass conceptClass) {
		getConceptClasses().add(conceptClass);
	}
	
	/**
	 * @see BaseOpenmrsObject#toString()
	 */
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(getName())) {
			return getName();
		}
		return super.toString();
	}
}
