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

import java.util.Locale;

import org.hibernate.envers.Audited;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


/**
 * ConceptStopWord is the real world term used to filter the words for indexing
 * from search phrase. Common words like 'and', 'if' are examples of this. It's
 * specific to locale.
 * 
 * @since 1.8
 */
@Audited
@Entity
@Table(name = "concept_stop_word")
public class ConceptStopWord extends BaseOpenmrsObject {
	
	private static final long serialVersionUID = 3671020002642184656L;
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_stop_word_id")
	private Integer conceptStopWordId;

	@Column(name = "word", nullable = false)
	private String value;

	@Column(name = "locale", nullable = false)
	private Locale locale;
	
	// Constructors
	
	/**
	 * default constructor
	 */
	public ConceptStopWord() {
	}
	
	/**
	 * Convenience constructor to create a ConceptStopWord object with default
	 * locale English
	 * 
	 * @param value
	 */
	public ConceptStopWord(String value) {
		this(value, Context.getLocale());
	}
	
	/**
	 * Convenience constructor to create a ConceptStopWord object with value and
	 * locale
	 * 
	 * @param value
	 * @param locale
	 */
	public ConceptStopWord(String value, Locale locale) {
		setValue(value);
		setLocale(locale);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		if (StringUtils.hasText(value)) {
			this.value = value.toUpperCase();
		}
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale == null ? Context.getLocale() : locale;
	}
	
	public Integer getConceptStopWordId() {
		return conceptStopWordId;
	}
	
	public void setConceptStopWordId(Integer conceptStopWordId) {
		this.conceptStopWordId = conceptStopWordId;
	}
	
	@Override
	public Integer getId() {
		return getConceptStopWordId();
	}
	
	@Override
	public void setId(Integer id) {
		setConceptStopWordId(id);
	}
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "ConceptStopWord: " + this.value + ", Locale: " + locale;
	}
}
