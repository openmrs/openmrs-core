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

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Base superclass for all mutable OpenmrsMetadata.
 * 
 * @since 2.2
 */
@MappedSuperclass
public abstract class BaseChangeableOpenmrsMetadata extends BaseOpenmrsMetadata {

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "date_changed")
	private Date dateChanged;
	
	/**
	 * @see Auditable#getChangedBy()
	 */
	@Override
	public User getChangedBy() {
		return this.changedBy;
	}
	
	/**
	 * @see Auditable#setChangedBy(User)
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @see Auditable#getDateChanged()
	 */
	@Override
	public Date getDateChanged() {
		return this.dateChanged;
	}
	
	/**
	 * @see Auditable#setDateChanged(Date)
	 */
	@Override
	public void setDateChanged(Date dateChanged) {this.dateChanged = dateChanged;}
}
