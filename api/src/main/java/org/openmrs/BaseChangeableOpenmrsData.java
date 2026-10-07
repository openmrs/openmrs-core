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
 * Base superclass for all mutable OpenmrsData.
 *
 * @since 2.2
 */
@MappedSuperclass
@Audited
public abstract class BaseChangeableOpenmrsData extends BaseOpenmrsData implements Changeable {

	//***** Properties *****

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "date_changed")
	private Date dateChanged;

	//***** Property Access *****

	/**
	 * @see org.openmrs.Changeable#getChangedBy()
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @see org.openmrs.Changeable#setChangedBy(User)
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @see org.openmrs.Changeable#getDateChanged()
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @see org.openmrs.Changeable#setDateChanged(Date)
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
