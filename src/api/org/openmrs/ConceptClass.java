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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * ConceptClass
 */
@Root(strict = false)
public class ConceptClass extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 33473L;
	
	// Fields
	
	private Integer conceptClassId;
	
	private LocalizedString localizedName;
	
	// Constructors
	
	/** default constructor */
	public ConceptClass() {
	}
	
	/** constructor with id */
	public ConceptClass(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}
	
	@Override
    public boolean equals(Object obj) {
		if (obj instanceof ConceptClass) {
			ConceptClass c = (ConceptClass) obj;
			return (this.conceptClassId.equals(c.getConceptClassId()));
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		if (this.getConceptClassId() == null)
			return super.hashCode();
		return this.getConceptClassId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * 
	 */
	@Attribute(required = true)
	public Integer getConceptClassId() {
		return this.conceptClassId;
	}
	
	@Attribute(required = true)
	public void setConceptClassId(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptClassId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptClassId(id);
		
	}
	
	/**
	 * @return the localizedName
	 */
	@Override
	public LocalizedString getLocalizedName() {
		if (localizedName == null)
			localizedName = new LocalizedString();
		return localizedName;
	}
	
	/**
	 * @param localizedName the localizedName to set
	 */
	public void setLocalizedName(LocalizedString localizedName) {
		this.localizedName = localizedName;
	}
	
	/**
	 * @return the name
	 * @see LocalizedString#getValue()
	 * @should return unlocalized name when no localization is added
	 */
	@Override
	public String getName() {
		return getLocalizedName().getValue();
	}
	
	/**
	 * @param name the name to set
	 * @see LocalizedString#setUnlocalizedValue(String)
	 * @should set unlocalized name correctly
	 */
	@Override
	public void setName(String name) {
		getLocalizedName().setUnlocalizedValue(name);
	}
	
}
