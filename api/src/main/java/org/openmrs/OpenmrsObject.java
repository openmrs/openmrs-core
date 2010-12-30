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
 * This is the base interface for all OpenMRS-defined classes
 * 
 * @since 1.5
 */
public interface OpenmrsObject {
	
	/**
	 * @return id - The unique Identifier for the object
	 */
	public Integer getId();
	
	/**
	 * @param id - The unique Identifier for the object
	 */
	public void setId(Integer id);
	
	/**
	 * @return the universally unique id for this object
	 */
	public String getUuid();
	
	/**
	 * @param uuid a universally unique id for this object
	 */
	public void setUuid(String uuid);
	
}
