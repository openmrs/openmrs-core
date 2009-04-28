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
 * In OpenMRS, data are rarely fully deleted (purged) from the system; rather, they are either
 * voided or retired. When data can be removed (effectively deleted from the user's perspective),
 * then they are voidable. Voided data are no longer valid and references from other non-voided data
 * are not valid. For example, when duplicate patient records are merged, the record that is not
 * kept is voided (invalidated). Unlike {@link Retireable}, voiding data invalidates any data
 * referencing them. e.g., when a patient is voided, all observations for that patient must be
 * voided as well.
 * 
 * @since 1.5
 * @see OpenmrsData
 * @see Retireable
 */
public interface Voidable {
	
	/**
	 * @return Boolean - whether of not this object is voided
	 */
	public Boolean isVoided();
	
	/**
	 * @param voided - whether of not this object is voided
	 */
	public void setVoided(Boolean voided);
	
	/**
	 * @return User - the user who voided the object
	 */
	public User getVoidedBy();
	
	/**
	 * @param voidedBy - the user who voided the object
	 */
	public void setVoidedBy(User voidedBy);
	
	/**
	 * @return Date - the date the object was voided
	 */
	public Date getDateVoided();
	
	/**
	 * @param dateVoided - the date the object was voided
	 */
	public void setDateVoided(Date dateVoided);
	
	/**
	 * @return String - the reason the object was voided
	 */
	public String getVoidReason();
	
	/**
	 * @param voidReason - the reason the object was voided
	 */
	public void setVoidReason(String voidReason);
}
