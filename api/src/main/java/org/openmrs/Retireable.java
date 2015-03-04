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
