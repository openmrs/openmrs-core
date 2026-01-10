/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.*
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.openmrs.annotation.Independent
import org.openmrs.api.APIException
import org.openmrs.api.context.Context

/**
 * OrderTypes are used to classify different types of Orders e.g to distinguish between Serology and
 * Radiology TestOrders
 */
@Entity
@Table(name = "order_type")
@Audited
class OrderType : BaseChangeableOpenmrsMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_type_id_seq")
    @GenericGenerator(
        name = "order_type_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "order_type_order_type_id_seq")]
    )
    @Column(name = "order_type_id", nullable = false)
    var orderTypeId: Int? = null

    @Column(name = "java_class_name", nullable = false)
    var javaClassName: String? = null

    @ManyToOne
    @JoinColumn(name = "parent")
    var parent: OrderType? = null

    @Independent
    @ManyToMany
    @JoinTable(
        name = "order_type_class_map",
        joinColumns = [JoinColumn(name = "order_type_id")],
        inverseJoinColumns = [JoinColumn(name = "concept_class_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["order_type_id", "concept_class_id"])]
    )
    var conceptClasses: MutableSet<ConceptClass> = LinkedHashSet()
        get() = field
        set(value) {
            field = value
        }

    /**
     * Default constructor
     */
    constructor()

    /**
     * Constructor with ID
     * 
     * @param orderTypeId the ID of the [OrderType]
     */
    constructor(orderTypeId: Int?) {
        this.orderTypeId = orderTypeId
    }

    /**
     * Convenience constructor that takes in the elements required to save this OrderType to the
     * database
     * 
     * @param name The name of this order Type
     * @param description A short description about this order type
     * @param javaClassName The fully qualified java class name
     */
    constructor(name: String?, description: String?, javaClassName: String?) {
        this.name = name
        this.description = description
        this.javaClassName = javaClassName
    }

    /**
     * @see OpenmrsObject.getId
     */
    override var id: Int?
        get() = orderTypeId
        set(value) {
            orderTypeId = value
        }

    /**
     * Convenience method that returns a [Class] object for the associated
     * javaClassName
     * 
     * @return The Java class as [Class]
     * @throws APIException
     */
    val javaClass: Class<*>
        get() {
            try {
                return Context.loadClass(javaClassName)
            } catch (e: ClassNotFoundException) {
                // re throw as a runtime exception
                throw APIException("OrderType.failed.load.class", arrayOf(javaClassName), e)
            }
        }

    /**
     * Convenience method that adds the specified concept class
     * 
     * @param conceptClass the ConceptClass to add
     * <strong>Should</strong> add the specified concept class
     * <strong>Should</strong> not add a duplicate concept class
     */
    fun addConceptClass(conceptClass: ConceptClass) {
        conceptClasses.add(conceptClass)
    }

    /**
     * @see BaseOpenmrsObject.toString
     */
    override fun toString(): String {
        return if (StringUtils.isNotBlank(name)) {
            name!!
        } else {
            super.toString()
        }
    }

    companion object {
        const val serialVersionUID = 23232L

        const val DRUG_ORDER_TYPE_UUID = "131168f4-15f5-102d-96e4-000c29c2a5d7"

        const val TEST_ORDER_TYPE_UUID = "52a447d3-a64a-11e3-9aeb-50e549534c5e"

        const val REFERRAL_ORDER_TYPE_UUID = "f1b63696-2b6c-11ec-8d3d-0242ac130003"
    }
}
