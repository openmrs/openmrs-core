/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * One row of the user_security_state table. The mapping exists so that the in-memory test database,
 * whose schema Hibernate generates from the mappings, has the table, and so that the native queries
 * in {@link UserSecurityStateDAO} can name it as their synchronized query space. That DAO never
 * loads instances of this class, so that security state is never served from a cache.
 *
 * @since 3.0.0
 */
@Entity
@Table(name = "user_security_state", uniqueConstraints = @UniqueConstraint(name = "user_security_state_user_property_unique", columnNames = {
        "user_id", "property" }))
public class UserSecurityState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_security_state_id")
	private Integer userSecurityStateId;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "property", nullable = false, length = 255)
	private String property;

	@Column(name = "property_value", nullable = false, length = 4000)
	private String value;

	@Column(name = "date_created", nullable = false)
	private Date dateCreated;

	@Column(name = "date_changed")
	private Date dateChanged;

	public Integer getUserSecurityStateId() {
		return userSecurityStateId;
	}

	public void setUserSecurityStateId(Integer userSecurityStateId) {
		this.userSecurityStateId = userSecurityStateId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
