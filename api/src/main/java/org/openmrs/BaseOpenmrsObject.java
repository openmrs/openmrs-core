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

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.Hibernate;

/**
 * This is the base implementation of the {@link OpenmrsObject} interface.<br>
 * It implements the uuid variable that all objects are expected to have.
 */
@MappedSuperclass
public abstract class BaseOpenmrsObject implements Serializable, OpenmrsObject {
	
	@Column(name = "uuid", unique = true, nullable = false, length = 38, updatable = false)
	private String uuid = UUID.randomUUID().toString();
	
	/**
	 * @see org.openmrs.OpenmrsObject#getUuid()
	 */
	@Override
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setUuid(java.lang.String)
	 */
	@Override
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
	 * <strong>Should</strong> not fail if uuid is null
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
	 * <code>uuid</code> (<code>((x.uuid != null) &amp;&amp; x.uuid.equals(y.uuid))</code> has the value
	 * <code>true</code>).
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 * <strong>Should</strong> return false if given obj is not instance of BaseOpenmrsObject
	 * <strong>Should</strong> return false if given obj is null
	 * <strong>Should</strong> return false if given obj has null uuid
	 * <strong>Should</strong> return false if uuid is null
	 * <strong>Should</strong> return true if objects are the same
	 * <strong>Should</strong> return true if uuids are equal
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
		//In case of hibernate proxy objects we need to get real classes
		Class<?> thisClass = Hibernate.getClass(this);
		Class<?> objClass = Hibernate.getClass(obj);
		if (!(thisClass.isAssignableFrom(objClass) || objClass.isAssignableFrom(thisClass))){
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
	 * <strong>Should</strong> include hashCode if uuid is null
	 * <strong>Should</strong> include uuid if not null
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("hashCode",
		    Integer.toHexString(hashCode())).append("uuid", getUuid()).build();
	}
}
