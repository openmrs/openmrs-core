/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		ConceptStopWord that = (ConceptStopWord) o;
		
		if (conceptStopWordId != null ? !conceptStopWordId.equals(that.conceptStopWordId) : that.conceptStopWordId != null)
			return false;
		if (locale != null ? !locale.equals(that.locale) : that.locale != null)
			return false;
		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;
		
		return true;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptStopWordId == null) ? super.hashCode() : conceptStopWordId.hashCode());
		result = prime * result + ((locale == null) ? super.hashCode() : locale.hashCode());
		result = prime * result + ((value == null) ? super.hashCode() : value.hashCode());
		return result;
	}
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "ConceptStopWord: " + this.value + ", Locale: " + locale;
	}
	
}
