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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

/**
 * The frequency at which an Order's action should be repeated, e.g. TWICE DAILY or EVERY 6 HOURS.
 * This class is backed by a Concept for i18n, synonyms, mappings, etc.
 *
 * @since 2.5.0
 */
@Entity
@Table(name = "order_frequency")
@Audited
public class OrderFrequency extends BaseChangeableOpenmrsMetadata {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_frequency_id")
	private Integer orderFrequencyId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false, unique = true)
	private Concept concept;

	@Column(name = "frequency_per_day", precision = 22)
	private Double frequencyPerDay;

	@Column(name = "uuid", length = 38, unique = true)
	private String uuid;

	/** Default constructor */
	public OrderFrequency() {
	}

	@Override
	public Integer getId() {
		return getOrderFrequencyId();
	}

	@Override
	public void setId(Integer id) {
		setOrderFrequencyId(id);
	}

	public Integer getOrderFrequencyId() {
		return orderFrequencyId;
	}

	public void setOrderFrequencyId(Integer orderFrequencyId) {
		this.orderFrequencyId = orderFrequencyId;
	}

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public Double getFrequencyPerDay() {
		return frequencyPerDay;
	}

	public void setFrequencyPerDay(Double frequencyPerDay) {
		this.frequencyPerDay = frequencyPerDay;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getName() {
		if (getConcept() != null && getConcept().getName() != null) {
			return getConcept().getName().toString();
		}
		return null;
	}

	@Override
	public String getDescription() {
		if (getConcept() != null && getConcept().getDescription() != null) {
			return getConcept().getDescription().getDescription();
		}
		return null;
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
