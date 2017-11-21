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

/**
 * An EncounterType defines how a certain kind of {@link Encounter}.
 * 
 * @see Encounter
 */
public class EncounterType extends BaseChangeableOpenmrsMetadata {
	
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
	@Override
	public Integer getId() {
		return getEncounterTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
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
