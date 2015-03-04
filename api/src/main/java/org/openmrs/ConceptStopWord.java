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

import org.openmrs.api.context.Context;
import org.simpleframework.xml.Root;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * ConceptStopWord is the real world term used to filter the words for indexing
 * from search phrase. Common words like 'and', 'if' are examples of this. It's
 * specific to locale.
 * 
 * @since 1.8
 */
@Root
public class ConceptStopWord extends BaseOpenmrsObject implements java.io.Serializable {
	
	private static final long serialVersionUID = 3671020002642184656L;
	
	// Fields
	private Integer conceptStopWordId;
	
	private String value;
	
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
	 * @param local
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
	
	public Integer getId() {
		return getConceptStopWordId();
	}
	
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
