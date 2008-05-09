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

import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * ConceptClass 
 */
@Root(strict=false)
public class ConceptClass implements java.io.Serializable {

	public static final long serialVersionUID = 33473L;

	// Fields

	private Integer conceptClassId;
	private String name;
	private String description;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptClass() {
	}

	/** constructor with id */
	public ConceptClass(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptClass) {
			ConceptClass c = (ConceptClass)obj;
			return (this.conceptClassId.equals(c.getConceptClassId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptClassId() == null) return super.hashCode();
		return this.getConceptClassId().hashCode();
	}

	// Property accessors

	/**
	 * 
	 */
	@Attribute(required=true)
	public Integer getConceptClassId() {
		return this.conceptClassId;
	}

	@Attribute(required=true)
	public void setConceptClassId(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}

	/**
	 * 
	 */
	@Element(data=true, required=true)
	public String getName() {
		return this.name;
	}

	@Element(data=true, required=true)
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	@Element(data=true, required=true)
	public String getDescription() {
		return this.description;
	}

	@Element(data=true, required=true)
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	@Element(required=true)
	public User getCreator() {
		return this.creator;
	}

	@Element(required=true)
	public void setCreator(User user) {
		this.creator = user;
	}

	/**
	 * 
	 */
	@Element(required=true)
	public Date getDateCreated() {
		return this.dateCreated;
	}

	@Element(required=true)
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}