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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "read_audit")
public class OpenmrsReadAuditEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private int accessedBy;
	
	private Date accessedOn;
	
	private String type;
	
	public OpenmrsReadAuditEntity() {}

	public OpenmrsReadAuditEntity(int accessedBy, Date accessedOn, String type) {
		this.accessedBy = accessedBy;
		this.accessedOn = accessedOn;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccessedBy() {
		return accessedBy;
	}

	public void setAccessedBy(int accessedBy) {
		this.accessedBy = accessedBy;
	}

	public Date getAccessedOn() {
		return accessedOn;
	}

	public void setAccessedOn(Date accessedOn) {
		this.accessedOn = accessedOn;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
