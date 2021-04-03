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

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * ConceptMapType are used to define relationships between concepts and concept reference terms e.g
 * IS_A or SAME_AS, BROADER_THAN
 *
 * @since 1.9
 */
public class ConceptMapType extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	private Integer conceptMapTypeId;
	
	private Boolean isHidden = Boolean.FALSE;
	
	public static final String SAME_AS_MAP_TYPE_UUID = "35543629-7d8c-11e1-909d-c80aa9edcf4e";
	
	/** default constructor */
	public ConceptMapType() {
	}
	
	/** constructor with id */
	public ConceptMapType(Integer conceptMapTypeId) {
		this.conceptMapTypeId = conceptMapTypeId;
	}
	
	/**
	 * @return the conceptMapTypeId
	 */
	public Integer getConceptMapTypeId() {
		return conceptMapTypeId;
	}
	
	/**
	 * @param conceptMapTypeId the conceptMapTypeId to set
	 */
	public void setConceptMapTypeId(Integer conceptMapTypeId) {
		this.conceptMapTypeId = conceptMapTypeId;
	}
	
	/**
	 * @return the isHidden
	 */
	public Boolean getIsHidden() {
		return isHidden;
	}
	
	/**
	 * @param isHidden the isHidden to set
	 */
	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptMapTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptMapTypeId(id);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (getName() == null) {
			return "";
		}
		
		return getName();
	}
	
	/**
	 * Returns true if this concept map type is hidden otherwise false
	 *
	 * @return true if this concept map type is hidden otherwise false
	 *
	 * @deprecated as of 2.0, use {@link #getIsHidden()}
	 */
	@Deprecated
	@JsonIgnore
	public boolean isHidden() {
		return getIsHidden();
	}
}
