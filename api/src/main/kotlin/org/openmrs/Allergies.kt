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

import org.openmrs.api.APIException
import org.openmrs.util.OpenmrsUtil

/**
 * Represents patient allergies
 */
class Allergies : MutableList<Allergy> {

    companion object {
        const val UNKNOWN: String = "Unknown"
        const val NO_KNOWN_ALLERGIES: String = "No known allergies"
        const val SEE_LIST: String = "See list"
    }

    var allergyStatus: String = UNKNOWN
        private set

    private val allergies: MutableList<Allergy> = ArrayList()

    override fun add(element: Allergy): Boolean {
        throwExceptionIfHasDuplicateAllergen(element)
        allergyStatus = SEE_LIST
        return allergies.add(element)
    }

    override fun remove(element: Allergy): Boolean {
        val result = allergies.remove(element)
        if (allergies.isEmpty()) {
            allergyStatus = UNKNOWN
        }
        return result
    }

    override fun clear() {
        allergyStatus = UNKNOWN
        allergies.clear()
    }

    fun confirmNoKnownAllergies() {
        if (allergies.isNotEmpty()) {
            throw APIException("Cannot confirm no known allergies if allergy list is not empty")
        }
        allergyStatus = NO_KNOWN_ALLERGIES
    }

    override val size: Int
        get() = allergies.size

    override fun isEmpty(): Boolean = allergies.isEmpty()

    override fun contains(element: Allergy): Boolean = allergies.contains(element)

    override fun containsAll(elements: Collection<Allergy>): Boolean = allergies.containsAll(elements)

    override fun get(index: Int): Allergy = allergies[index]

    override fun indexOf(element: Allergy): Int = allergies.indexOf(element)

    override fun lastIndexOf(element: Allergy): Int = allergies.lastIndexOf(element)

    override fun iterator(): MutableIterator<Allergy> = allergies.iterator()

    override fun listIterator(): MutableListIterator<Allergy> = allergies.listIterator()

    override fun listIterator(index: Int): MutableListIterator<Allergy> = allergies.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Allergy> = allergies.subList(fromIndex, toIndex)

    override fun add(index: Int, element: Allergy) {
        throwExceptionIfHasDuplicateAllergen(element)
        allergies.add(index, element)
        allergyStatus = SEE_LIST
    }

    override fun addAll(elements: Collection<Allergy>): Boolean {
        throwExceptionIfHasDuplicateAllergen(elements)
        for (allergy in elements) {
            throwExceptionIfHasDuplicateAllergen(allergy)
        }
        allergyStatus = SEE_LIST
        return allergies.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<Allergy>): Boolean {
        throwExceptionIfHasDuplicateAllergen(elements)
        for (allergy in elements) {
            throwExceptionIfHasDuplicateAllergen(allergy)
        }
        allergyStatus = SEE_LIST
        return allergies.addAll(index, elements)
    }

    override fun removeAt(index: Int): Allergy {
        val allergy = allergies.removeAt(index)
        if (allergies.isEmpty()) {
            allergyStatus = UNKNOWN
        }
        return allergy
    }

    override fun removeAll(elements: Collection<Allergy>): Boolean {
        val changed = allergies.removeAll(elements.toSet())
        if (allergies.isEmpty()) {
            allergyStatus = UNKNOWN
        }
        return changed
    }

    override fun retainAll(elements: Collection<Allergy>): Boolean {
        val changed = allergies.retainAll(elements.toSet())
        if (allergies.isEmpty()) {
            allergyStatus = UNKNOWN
        }
        return changed
    }

    override fun set(index: Int, element: Allergy): Allergy {
        allergyStatus = SEE_LIST
        return allergies.set(index, element)
    }

    /**
     * Gets an allergy with a given id
     *
     * @param allergyId the allergy id
     * @return the allergy with a matching id
     */
    fun getAllergy(allergyId: Int?): Allergy? =
        allergies.firstOrNull { OpenmrsUtil.nullSafeEquals(it.allergyId, allergyId) }

    /**
     * Throws an exception if the given allergy has the same allergen
     * as any of those in the allergies that we already have.
     *
     * @param allergy the given allergy whose allergen to compare with
     */
    private fun throwExceptionIfHasDuplicateAllergen(allergy: Allergy) {
        throwExceptionIfHasAllergen(allergy, allergies)
    }

    /**
     * Throws an exception if the given allergies collection has duplicate allergen
     *
     * @param allergies the given allergies collection
     */
    private fun throwExceptionIfHasDuplicateAllergen(allergies: Collection<Allergy>) {
        val allergiesCopy = ArrayList(allergies)
        for (allergy in allergies) {
            allergiesCopy.remove(allergy)
            throwExceptionIfHasAllergen(allergy, allergiesCopy)
            allergiesCopy.add(allergy)
        }
    }

    /**
     * Throws an exception if the given allergies collection has
     * an allergen similar to that of the given allergy
     *
     * @param allergy the given allergy
     * @param allergies the given allergies collection
     */
    private fun throwExceptionIfHasAllergen(allergy: Allergy, allergies: Collection<Allergy>) {
        if (containsAllergen(allergy, allergies)) {
            throw APIException("Duplicate allergens not allowed")
        }
    }

    /**
     * Checks if a given allergy has the same allergen as any in the given allergies
     *
     * @param allergy the allergy whose allergen to compare with
     * @param allergies the allergies whose allergens to compare with
     * @return true if the same allergen exists, else false
     */
    fun containsAllergen(allergy: Allergy, allergies: Collection<Allergy>): Boolean =
        allergies.any { it.hasSameAllergen(allergy) }

    /**
     * Checks if we already have an allergen similar to that in the given allergy
     *
     * @param allergy the allergy whose allergen to compare with
     * @return true if the same allergen exists, else false
     */
    fun containsAllergen(allergy: Allergy): Boolean = containsAllergen(allergy, allergies)
}
