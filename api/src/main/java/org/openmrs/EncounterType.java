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
	
	private Privilege viewPrivilege;
	
	private Privilege editPrivilege;
	
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
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getEncounterTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setEncounterTypeId(id);
		
	}
	
	/**
	 * Gets privilege which can view this type of encounters
	 * @return the viewPrivilege the privilege instance
	 */
	public Privilege getViewPrivilege() {
		return viewPrivilege;
	}
	
	/**
	 * Sets privilege which can view this type of encounters
	 * @param viewPrivilege the viewPrivilege to set
	 */
	public void setViewPrivilege(Privilege viewPrivilege) {
		this.viewPrivilege = viewPrivilege;
	}
	
	/**
	 * Gets privilege which can edit this type of encounters
	 * @return the editPrivilege the privilege instance
	 */
	public Privilege getEditPrivilege() {
		return editPrivilege;
	}
	
	/**
	 * Sets privilege which can edit this type of encounters
	 * @param editPrivilege the editPrivilege to set
	 */
	public void setEditPrivilege(Privilege editPrivilege) {
		this.editPrivilege = editPrivilege;
	}
	
}
