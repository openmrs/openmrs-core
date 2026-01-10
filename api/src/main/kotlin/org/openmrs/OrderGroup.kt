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
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.openmrs.api.APIException

/**
 * Contains a group of [org.openmrs.Order]s that are ordered together within a single encounter,
 * often driven by an [org.openmrs.OrderSet]. Not all orders in an encounter need to be grouped
 * this way, only those that have a specific connection to each other (e.g. several orders that
 * together make up a treatment protocol for some diagnosis could be grouped).
 * 
 * @since 1.12
 */
@Entity
@Table(name = "order_group")
@Audited
class OrderGroup : BaseCustomizableData<OrderGroupAttribute>() {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_group_id_seq")
    @GenericGenerator(
        name = "order_group_id_seq",
        strategy = "native",
        parameters = [Parameter(name = "sequence", value = "order_group_order_group_id_seq")]
    )
    @Column(name = "order_group_id", nullable = false)
    var orderGroupId: Int? = null

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient? = null

    @ManyToOne
    @JoinColumn(name = "encounter_id", nullable = false)
    var encounter: Encounter? = null

    @get:Access(AccessType.PROPERTY)
    @OneToMany(mappedBy = "orderGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("voided asc")
    @BatchSize(size = 100)
    override var attributes: MutableSet<OrderGroupAttribute> = LinkedHashSet()

    @ManyToOne
    @JoinColumn(name = "order_set_id")
    var orderSet: OrderSet? = null

    @ManyToOne
    @JoinColumn(name = "parent_order_group")
    var parentOrderGroup: OrderGroup? = null

    @ManyToOne
    @JoinColumn(name = "order_group_reason")
    var orderGroupReason: Concept? = null

    @ManyToOne
    @JoinColumn(name = "previous_order_group")
    var previousOrderGroup: OrderGroup? = null

    @OneToMany(mappedBy = "orderGroup", fetch = FetchType.LAZY)
    @OrderBy("sort_weight asc")
    var orders: MutableList<Order> = ArrayList()
        get() = field

    @Access(AccessType.FIELD)
    @OneToMany(mappedBy = "parentOrderGroup", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    @OrderBy("orderGroupId asc")
    @BatchSize(size = 25)
    var nestedOrderGroups: MutableSet<OrderGroup>? = null

    override var id: Int?
        get() = orderGroupId
        set(value) {
            orderGroupId = value
        }

    /**
     * Adds a new order to the existing list of orders
     * 
     * @param order the new order to be added
     */
    fun addOrder(order: Order, position: Int? = null) {
        if (orders.contains(order)) {
            return
        }

        order.orderGroup = this

        val listIndex = findListIndexForGivenPosition(position)
        orders.add(listIndex, order)
        if (order.sortWeight == null) {
            order.sortWeight = findSortWeight(listIndex)
        }
    }

    /**
     * Adds [Order]s to existing Order list
     * 
     * @param orders
     * @since 2.2
     */
    fun addOrders(orders: List<Order>) {
        orders.forEach { addOrder(it) }
    }

    private fun findListIndexForGivenPosition(position: Int?): Int {
        val size = orders.size
        var pos = position
        if (pos != null) {
            if (pos < 0 && pos >= (-1 - size)) {
                pos += size + 1
            } else if (pos > size) {
                throw APIException("Cannot add a member which is out of range of the list")
            }
        } else {
            pos = size
        }
        return pos
    }

    private fun findSortWeight(index: Int): Double {
        val size = orders.size
        if (size == 1) {
            return 10.0
        }
        if (index == 0) {
            return orders[1].sortWeight!! / 2
        }
        if (index == size - 1) {
            return orders[index - 1].sortWeight!! + 10.0
        }
        return (orders[index - 1].sortWeight!! + orders[index + 1].sortWeight!!) / 2
    }

    companion object {
        const val serialVersionUID = 72232L
    }
}
