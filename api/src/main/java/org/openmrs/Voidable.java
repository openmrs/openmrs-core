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

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

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
public interface Voidable extends OpenmrsObject {
	
	/**
	 * @return Boolean - whether of not this object is voided
	 *
	 * @deprecated as of 2.0, use {@link #getVoided()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isVoided();
	
	/**
	 * @return true if this object is voided and otherwise false
	 */
	public Boolean getVoided();
	
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
