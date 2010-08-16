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
 * voided or retired. When existing data remain valid but should no longer be used for new entries,
 * they are <em>retired</em>. Typically this applies to metadata (see {@link OpenmrsMetadata}). For
 * example, an encounter form type or a patient attribute type may no longer be valid but cannot be
 * removed because there are (and may be in perpetuity) data previously collected using these
 * metadata. Making these metadata Retirable allows them to be retired, meaning that existing
 * references remain valid but future references are not allowed.
 * 
 * @since 1.5
 * @see OpenmrsMetadata
 * @see Voidable
 */
public interface Retireable extends OpenmrsObject {
	
	/**
	 * @return Boolean - whether of not this object is retired
	 */
	public Boolean isRetired();
	
	/**
	 * @param retired - whether of not this object is retired
	 */
	public void setRetired(Boolean retired);
	
	/**
	 * @return User - the user who retired the object
	 */
	public User getRetiredBy();
	
	/**
	 * @param retiredBy - the user who retired the object
	 */
	public void setRetiredBy(User retiredBy);
	
	/**
	 * @return Date - the date the object was retired
	 */
	public Date getDateRetired();
	
	/**
	 * @param dateRetired - the date the object was retired
	 */
	public void setDateRetired(Date dateRetired);
	
	/**
	 * @return String - the reason the object was retired
	 */
	public String getRetireReason();
	
	/**
	 * @param retireReason - the reason the object was retired
	 */
	public void setRetireReason(String retireReason);
}
