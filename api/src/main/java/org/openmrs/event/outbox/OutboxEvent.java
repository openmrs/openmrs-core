/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * OutboxEvent persisted in DB.
 *
 * @since 2.9.0
 */
@Audited
@Entity
@Table(name = "outbox_event")
public class OutboxEvent extends BaseOpenmrsObject implements Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Lob
	@Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
	private String payload;

	@Column(name = "date_created", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;

	@Column(name = "date_changed", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateChanged = new Date();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "error_count", nullable = false)
	private Integer errorCount = 0;

	@Column(name = "error_message", length = 1024)
	private String errorMessage;

	@Lob
	@Column(name = "completed_listeners")
	private String completedListeners;

	@ManyToOne
	@JoinColumn(name = "creator")
	private User creator;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getCompletedListeners() {
		return completedListeners;
	}

	public void setCompletedListeners(String completedListeners) {
		this.completedListeners = completedListeners;
	}

	// --- Auditable Interface Implementation ---

	@Override
	public User getCreator() {
		return creator;
	}

	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}

	@Override
	public Date getDateCreated() {
		return this.dateCreated;
	}

	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public User getChangedBy() {
		return changedBy;
	}

	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	@Override
	public Date getDateChanged() {
		return this.dateChanged;
	}

	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public enum Status {
		PENDING,
		PROCESSING,
		COMPLETED,
		FAILED
	}
}
