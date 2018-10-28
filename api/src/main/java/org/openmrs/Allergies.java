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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represents patient allergies
 */
public class Allergies implements List<Allergy> {
	
	public static final String UNKNOWN = "Unknown";
	
	public static final String NO_KNOWN_ALLERGIES = "No known allergies";
	
	public static final String SEE_LIST = "See list";
	
	private String allergyStatus = UNKNOWN;
	
	private List<Allergy> allergies = new ArrayList<>();

	/**
     * @return the allergyStatus
     */
    public String getAllergyStatus() {
    	return allergyStatus;
    }

	@Override
	public boolean add(Allergy allergy) {
		throwExceptionIfHasDuplicateAllergen(allergy);
		allergyStatus = SEE_LIST;
		return allergies.add(allergy);
	}
	
	public boolean remove(Allergy allergy) {
		boolean result = allergies.remove(allergy);
		if (allergies.isEmpty()) {
			allergyStatus = UNKNOWN;
		}
		return result;
	}
	
	@Override
	public void clear() {
		allergyStatus = UNKNOWN;
		allergies.clear();
	}
	
	public void confirmNoKnownAllergies() throws APIException {
		if (!allergies.isEmpty()) {
			throw new APIException("Cannot confirm no known allergies if allergy list is not empty");
		}
		allergyStatus = NO_KNOWN_ALLERGIES;
	}
	
	/**
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<Allergy> iterator() {
		return allergies.iterator();
	}
	
	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, Allergy element) {
		throwExceptionIfHasDuplicateAllergen(element);
		allergies.add(index, element);
		allergyStatus = SEE_LIST;
	}
	
	/**
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Allergy> c) {
		throwExceptionIfHasDuplicateAllergen(c);
		for (Allergy allergy : c) {
			throwExceptionIfHasDuplicateAllergen(allergy);
		}
		allergyStatus = SEE_LIST;
		return allergies.addAll(c);
	}
	
	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Allergy> c) {
		throwExceptionIfHasDuplicateAllergen(c);
		for (Allergy allergy : c) {
			throwExceptionIfHasDuplicateAllergen(allergy);
		}
		allergyStatus = SEE_LIST;
		return allergies.addAll(index, c);
	}
	
	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return allergies.contains(o);
	}
	
	/**
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return allergies.containsAll(c);
	}
	
	/**
	 * @see java.util.List#get(int)
	 */
	@Override
	public Allergy get(int index) {
		return allergies.get(index);
	}
	
	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return allergies.indexOf(o);
	}
	
	/**
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return allergies.isEmpty();
	}
	
	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return allergies.lastIndexOf(o);
	}
	
	/**
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<Allergy> listIterator() {
		return allergies.listIterator();
	}
	
	/**
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<Allergy> listIterator(int index) {
		return allergies.listIterator(index);
	}
	
	/**
	 * @see java.util.List#remove(int)
	 */
	@Override
	public Allergy remove(int index) {
		Allergy allergy = allergies.remove(index);
		if (allergies.isEmpty()) {
			allergyStatus = UNKNOWN;
		}
		return allergy;
	}
	
	/**
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		Boolean removed = allergies.remove(o);
		if (allergies.isEmpty()) {
			allergyStatus = UNKNOWN;
		}
		return removed;
	}
	
	/**
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = allergies.removeAll(c);
		if (allergies.isEmpty()) {
			allergyStatus = UNKNOWN;
		}
		return changed;
	}
	
	/**
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = allergies.retainAll(c);
		if (allergies.isEmpty()) {
			allergyStatus = UNKNOWN;
		}
		return changed;
	}
	
	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public Allergy set(int index, Allergy element) {
		allergyStatus = SEE_LIST;
		return allergies.set(index, element);
	}
	
	/**
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		return allergies.size();
	}
	
	/**
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<Allergy> subList(int fromIndex, int toIndex) {
		return allergies.subList(fromIndex, toIndex);
	}
	
	/**
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray() {
		return allergies.toArray();
	}
	
	/**
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return allergies.toArray(a);
	}
	
	/**
	 * Gets an allergy with a given id
	 * 
	 * @param allergyId the allergy id
	 * @return the allergy with a matching id
	 */
	public Allergy getAllergy(Integer allergyId) {
		for (Allergy allergy : allergies) {
			if (OpenmrsUtil.nullSafeEquals(allergy.getAllergyId(), allergyId)) {
				return allergy;
			}
		}
		
		return null;
	}
	
	/**
	 * Throws an exception if the given allergy has the same allergen
	 * as any of those in the allergies that we already have.
	 * 
	 * @param allergy the given allergy whose allergen to compare with
	 */
	private void throwExceptionIfHasDuplicateAllergen(Allergy allergy) {
		throwExceptionIfHasAllergen(allergy, allergies);
	}
	
	/**
	 * Throws an exception if the given allergies collection has duplicate allergen
	 * 
	 * @param allergies the given allergies collection
	 */
	private void throwExceptionIfHasDuplicateAllergen(Collection<? extends Allergy> allergies) {
		List<Allergy> allergiesCopy = new ArrayList<>(allergies);
		
		for (Allergy allergy : allergies) {
			allergiesCopy.remove(allergy);
			throwExceptionIfHasAllergen(allergy, allergiesCopy);
			allergiesCopy.add(allergy);
		}
	}
	
	/**
	 * Throws an exception if the given allergies collection has
	 * an allergen similar to that of the given allergy
	 * 
	 * @param allergy the given allergy
	 * @param allergies the given allergies collection
	 */
	private void throwExceptionIfHasAllergen(Allergy allergy, Collection<? extends Allergy> allergies) {
		if (containsAllergen(allergy, allergies)) {
			throw new APIException("Duplicate allergens not allowed");
		}
	}
	
	/**
	 * Checks if a given allergy has the same allergen as any in the given allergies
	 * 
	 * @param allergy the allergy whose allergen to compare with
	 * @param allergies the allergies whose allergens to compare with
	 * @return true if the same allergen exists, else false
	 */
	public boolean containsAllergen(Allergy allergy, Collection<? extends Allergy> allergies) {
		for (Allergy alg : allergies) {
			if (alg.hasSameAllergen(allergy)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if we already have an allergen similar to that in the given allergy
	 * 
	 * @param allergy the allergy whose allergen to compare with
	 * @return true if the same allergen exists, else false
	 */
	public boolean containsAllergen(Allergy allergy) {
		return containsAllergen(allergy, allergies);
	}
}
