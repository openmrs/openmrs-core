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

import java.util.UUID;

/**
 * This is the base implementation of the {@link OpenmrsObject} interface.<br/>
 * It implements the uuid variable that all objects are expected to have.
 */
public abstract class BaseOpenmrsObject implements OpenmrsObject {
	
	private String uuid = UUID.randomUUID().toString();
	
	/**
	 * @see org.openmrs.OpenmrsObject#getUuid()
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setUuid(java.lang.String)
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * Returns a hash code based on the <code>uuid</code> field.
	 * <p>
	 * If the <code>uuid</code> field is <code>null</code>, it delegates to
	 * {@link Object#hashCode()}.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (uuid == null)
			return super.hashCode();
		return uuid.hashCode();
	}
	
	/**
	 * Returns <code>true</code> if and only if <code>x</code> and <code>y</code> refer to the same
	 * object (<code>x == y</code> has the value <code>true</code>) or both have the same
	 * <code>uuid</code> (<code>((x.uuid != null) && x.uuid.equals(y.uuid))</code> has the value
	 * <code>true</code>).
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BaseOpenmrsObject))
			return false;
		BaseOpenmrsObject other = (BaseOpenmrsObject) obj;
		if (uuid == null)
			return false;
		return uuid.equals(other.uuid);
	}
	
	/**
	 * Returns a string consisting of the name of the class of which the object is an instance and
	 * the <code>uuid</code> field surrounded by <code>[</code> and <code>]</code>. In other words,
	 * this method returns a string equal to the value of: <blockquote>
	 * 
	 * <pre>
	 * getClass().getName() + '[' + uuid + ']'
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * If the <code>uuid</code> field is <code>null</code>, it delegates to
	 * {@link Object#toString()}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (uuid != null) {
			return getClass().getName() + "[" + uuid + "]";
		} else {
			return super.toString();
		}
	}
}
