/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.db.hibernate.envers;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Custom revision entity for OpenMRS application
 * Can be used to store revision metadata 
 */
@RevisionEntity(OpenmrsRevisionEntityListener.class)
@Entity
@Table(name = "revision_entity")
public class OpenmrsRevisionEntity extends DefaultRevisionEntity {

	private Integer changedBy;
	
	private Date changedOn;

	public Integer getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(Integer userId) {
		this.changedBy = userId;
	}

	public Date getChangedOn() {
		return changedOn;
	}

	public void setChangedOn(Date changedOn) {
		this.changedOn = changedOn;
	}
}
