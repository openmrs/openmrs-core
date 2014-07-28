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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
	 * @should not fail if uuid is null
	 */
	@Override
	public int hashCode() {
		if (getUuid() == null) {
			return super.hashCode();
		}
		return getUuid().hashCode();
	}
	
	/**
	 * Returns <code>true</code> if and only if <code>x</code> and <code>y</code> refer to the same
	 * object (<code>x == y</code> has the value <code>true</code>) or both have the same
	 * <code>uuid</code> (<code>((x.uuid != null) && x.uuid.equals(y.uuid))</code> has the value
	 * <code>true</code>).
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should return false if given obj is not instance of BaseOpenmrsObject
	 * @should return false if given obj is null
	 * @should return false if given obj has null uuid
	 * @should return false if uuid is null
	 * @should return true if objects are the same
	 * @should return true if uuids are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BaseOpenmrsObject)) {
			return false;
		}
		BaseOpenmrsObject other = (BaseOpenmrsObject) obj;
		// Need to call getUuid to make sure the hibernate proxy objects return the correct uuid.
		// The private member may not be set for a hibernate proxy.
		if (getUuid() == null) {
			return false;
		}
		return getUuid().equals(other.getUuid());
	}
	
	/**
	 * Returns a string equal to the value of: <blockquote>ClassName{hashCode=...,
	 * uuid=...}</blockquote>
	 * <p>
	 * If the <code>uuid</code> field is <code>null</code>, it returns: <blockquote>
	 * ClassName{hashCode=...} </blockquote>
	 *
	 * @should include hashCode if uuid is null
	 * @should include uuid if not null
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("hashCode",
		    Integer.toHexString(hashCode())).append("uuid", getUuid()).build();
	}
}
