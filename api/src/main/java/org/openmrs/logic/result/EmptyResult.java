/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.result;

import java.util.Collection;

/**
 *
 */
public class EmptyResult extends Result {
	
	private static final long serialVersionUID = 6317773013593085780L;
	
	/**
	 * @see java.util.ArrayList#add(int, Object)
	 */
	@Override
	public void add(int arg0, Result arg1) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see java.util.ArrayList#add(Object)
	 */
	@Override
	public boolean add(Result value) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Result> newValues) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Result> newValues) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#clear()
	 */
	@Override
	public void clear() {
		throw new ImmutableResultException("Cannot clear EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#isEmpty()
	 * @should return true
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#toBoolean()
	 */
	@Override
	public Boolean toBoolean() {
		return Boolean.FALSE;
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#isNull()
	 * @should return true
	 */
	@Override
	public boolean isNull() {
		return true;
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#remove(int)
	 */
	@Override
	public Result remove(int index) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object value) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see java.util.ArrayList#set(int, Object)
	 */
	@Override
	public Result set(int index, Result element) {
		throw new ImmutableResultException("Cannot set values within EmptyResult");
	}
	
}
