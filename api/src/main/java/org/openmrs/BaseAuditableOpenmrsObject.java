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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

/**
 * Intermediate base class for OpenMRS objects that track who last changed them and when. Both
 * {@link BaseOpenmrsData} and {@link BaseOpenmrsMetadata} share the {@code changedBy} and
 * {@code dateChanged} fields; this class provides a single implementation so that the deprecated
 * accessor methods are not duplicated across subclasses.
 *
 * <p>As of version 2.2, these mutator / accessor methods are deprecated because OpenmrsData and
 * OpenmrsMetadata are immutable by default. Subclasses that need mutability should extend
 * {@link BaseChangeableOpenmrsData} or {@link BaseChangeableOpenmrsMetadata} instead.</p>
 *
 * @since 2.2
 * @see Changeable
 */
@MappedSuperclass
@Audited
public abstract class BaseAuditableOpenmrsObject extends BaseOpenmrsObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	/**
	 * Delegates to {@link BaseOpenmrsObject#equals(Object)}. Audit fields ({@code changedBy},
	 * {@code dateChanged}) are intentionally excluded from equality checks because object identity
	 * in OpenMRS is determined by UUID alone.
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	/**
	 * Delegates to {@link BaseOpenmrsObject#hashCode()}.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/**
	 * @return the user who last changed this object
	 * @see Changeable#getChangedBy()
	 * @deprecated as of version 2.2, use {@link BaseChangeableOpenmrsData} or
	 *             {@link BaseChangeableOpenmrsMetadata} instead
	 */
	@Deprecated(since = "2.2", forRemoval = false)
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy the user who last changed this object
	 * @see Changeable#setChangedBy(User)
	 * @deprecated as of version 2.2, use {@link BaseChangeableOpenmrsData} or
	 *             {@link BaseChangeableOpenmrsMetadata} instead
	 */
	@Deprecated(since = "2.2", forRemoval = false)
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return the date this object was last changed
	 * @see Changeable#getDateChanged()
	 * @deprecated as of version 2.2, use {@link BaseChangeableOpenmrsData} or
	 *             {@link BaseChangeableOpenmrsMetadata} instead
	 */
	@Deprecated(since = "2.2", forRemoval = false)
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged the date this object was last changed
	 * @see Changeable#setDateChanged(Date)
	 * @deprecated as of version 2.2, use {@link BaseChangeableOpenmrsData} or
	 *             {@link BaseChangeableOpenmrsMetadata} instead
	 */
	@Deprecated(since = "2.2", forRemoval = false)
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
