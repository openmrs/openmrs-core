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
package org.openmrs.logic.result;

import java.util.Collection;

/**
 *
 */
public class EmptyResult extends Result {
	
	private static final long serialVersionUID = 6317773013593085780L;
	
	/**
	 * @see org.openmrs.logic.result.Result#add(int, Result)
	 */
	@Override
	public void add(int arg0, Result arg1) {
		throw new ImmutableResultException("Cannot add to EmptyResult");
	}
	
	/**
	 * @see org.openmrs.logic.result.Result#add(Result)
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
	 * @see org.openmrs.logic.result.Result#set(int, Result)
	 */
	@Override
	public Result set(int index, Result element) {
		throw new ImmutableResultException("Cannot set values within EmptyResult");
	}
	
}
