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

/**
 * In OpenMRS, the convention is to track basic audit information for each object related to who
 * initially created the object and when, and who last changed the object and when. This allows us
 * to check, for example, when a patient record was created, or when a person address was last
 * updated in the system. Any object that needs to keep track of this information should implement
 * this interface.
 * 
 * @see OpenmrsData
 * @see OpenmrsMetadata
 */
public interface Auditable {
	
	/**
	 * @return User - the user who created the object
	 */
	public User getCreator();
	
	/**
	 * @param creator - the user who created the object
	 */
	public void setCreator(User creator);
	
	/**
	 * @return Date - the date the object was created
	 */
	public Date getDateCreated();
	
	/**
	 * @param dateCreated - the date the object was created
	 */
	public void setDateCreated(Date dateCreated);
	
	/**
	 * @return User - the user who last changed the object
	 */
	public User getChangedBy();
	
	/**
	 * @param changedBy - the user who last changed the object
	 */
	public void setChangedBy(User changedBy);
	
	/**
	 * @return Date - the date the object was last changed
	 */
	public Date getDateChanged();
	
	/**
	 * @param dateChanged - the date the object was last changed
	 */
	public void setDateChanged(Date dateChanged);
	
}
