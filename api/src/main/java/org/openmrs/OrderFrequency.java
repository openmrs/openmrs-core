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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The frequency at which an Order's action should be repeated, e.g. TWICE DAILY or EVERY 6 HOURS.
 * This class is backed by a Concept for i18n, synonyms, mappings, etc, but it contains additional
 * details an electronic system can use to understand its meaning.
 * 
 * @since 1.10
 */
@Entity
@Table(name = "order_frequency")
@AttributeOverride(name = "name", column = @Column(name = "name", nullable = true))
public class OrderFrequency extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_frequency_id_seq")
	@GenericGenerator(
		name = "order_frequency_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "order_frequency_order_frequency_id_seq")
	)
	@Column(name = "order_frequency_id")
	private Integer orderFrequencyId;

	@Column(name = "frequency_per_day", length = 22)
	private Double frequencyPerDay;

	@Column(name = "uuid", length = 38, unique = true)
	private String uuid;
	
	@ManyToOne
	@JoinColumn(name = "concept_id", nullable = false, unique = true)
	private Concept concept;
	
	/**
	 * Get the orderFrequencyId
	 */
	public Integer getOrderFrequencyId() {
		return orderFrequencyId;
	}
	
	/**
	 * Sets the orderFrequencyId
	 * 
	 * @param orderFrequencyId
	 */
	public void setOrderFrequencyId(Integer orderFrequencyId) {
		this.orderFrequencyId = orderFrequencyId;
	}
	
	/**
	 * Get the frequencyPerDay
	 */
	public Double getFrequencyPerDay() {
		return frequencyPerDay;
	}
	
	public void setFrequencyPerDay(Double frequencyPerDay) {
		this.frequencyPerDay = frequencyPerDay;
	}
	
	/**
	 * Get the uuid
	 */
	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderFrequencyId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderFrequencyId(id);
	}
	
	/**
	 * Get the concept for the drugFrequency
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Sets the concept for the drugFrequency
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @see BaseOpenmrsMetadata#getDescription()
	 */
	@Override
	public String getName() {
		if (getConcept() != null && getConcept().getName() != null) {
			return getConcept().getName().toString();
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsMetadata#getName()
	 */
	@Override
	public String getDescription() {
		if (getConcept() != null && getConcept().getDescription() != null) {
			return getConcept().getDescription().getDescription();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
