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

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Custom revision entity for OpenMRS application
 * Can be used to store revision metadata
 * In Hibernate 7, DefaultRevisionEntity is final, so we create our own revision entity.
 */
@RevisionEntity(OpenmrsRevisionEntityListener.class)
@Entity
@Table(name = "revision_entity")
public class OpenmrsRevisionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@RevisionNumber
	@Column(name = "rev")
	private int id;

	@RevisionTimestamp
	@Column(name = "revtstmp")
	private long timestamp;

	@Column(name = "changed_by")
	private Integer changedBy;
	
	@Column(name = "changed_on")
	private Date changedOn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Date getRevisionDate() {
		return new Date(timestamp);
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpenmrsRevisionEntity)) return false;
		
		OpenmrsRevisionEntity that = (OpenmrsRevisionEntity) o;
		return id == that.id && timestamp == that.timestamp;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}
}
