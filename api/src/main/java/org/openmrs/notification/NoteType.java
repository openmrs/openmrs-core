/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/**
 * 
 */
package org.openmrs.notification;

/**
 * 
 */
public class NoteType {
	
	private Integer id;
	
	private String name;
	
	private String description;
	
	/**
	 * Default constructor
	 */
	public NoteType() {
	}
	
	/**
	 * @return Returns the id.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * @param id The id to set.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
