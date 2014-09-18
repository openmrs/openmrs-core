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

/**
 * ConceptMapType are used to define relationships between concepts and concept reference terms e.g
 * IS_A or SAME_AS, BROADER_THAN
 *
 * @since 1.9
 */
public class ConceptMapType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
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
	public Integer getId() {
		return getConceptMapTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptMapTypeId(id);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (getName() == null) {
			return "";
		}
		
		return getName();
	}
	
	/**
	 * Returns true if this concept map type is hidden otherwise false
	 *
	 * @return
	 */
	public boolean isHidden() {
		return isHidden;
	}
}
