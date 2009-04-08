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
 * An EncounterType defines how a certain kind of {@link Encounter}.
 * 
 * @see Encounter
 */
public class EncounterType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 789L;
	
	private Integer encounterTypeId;
	
	// Constructors
	
	/** default constructor */
	public EncounterType() {
	}
	
	/**
	 * Constructor with id
	 * 
	 * @should set encounter type id with given parameter
	 */
	public EncounterType(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this encounter type
	 * @param description a short description of why this encounter type exists
	 */
	public EncounterType(String name, String description) {
		setName(name);
		setDescription(description);
	}
	
	/**
	 * Compares two EncounterType objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 * @should have equal encounter type objects by encounter type id
	 * @should not have equal encounter type objects by encounterTypeId
	 * @should have equal encounter type objects with no encounterTypeId
	 * @should not have equal encounter type objects when one has null encounterTypeId
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof EncounterType))
			return false;
		
		EncounterType encounterType = (EncounterType) obj;
		if (this.encounterTypeId != null && encounterType.getEncounterTypeId() != null)
			return (this.encounterTypeId.equals(encounterType.getEncounterTypeId()));
		else
			return this == encounterType;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @should get hashCode even with null attributes
	 */
	public int hashCode() {
		if (this.getEncounterTypeId() == null)
			return super.hashCode();
		return this.getEncounterTypeId().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the encounterTypeId.
	 */
	public Integer getEncounterTypeId() {
		return encounterTypeId;
	}
	
	/**
	 * @param encounterTypeId The encounterTypeId to set.
	 */
	public void setEncounterTypeId(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getEncounterTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setEncounterTypeId(id);
		
	}
	
}
