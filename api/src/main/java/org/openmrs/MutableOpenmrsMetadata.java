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
 * Super interface for all mutable OpenmrsMetadata, OpenmrsMetadata is immutable by default
 * therefore subclasses should implement this interface in order for their instances to be mutable.
 *
 * @see MutableOpenmrsData
 * @since 2.2
 */
public interface MutableOpenmrsMetadata extends OpenmrsMetadata {
	
	/**
	 * @see Changeable#getChangedBy()
	 */
	@Override
	User getChangedBy();
	
	/**
	 * @see Changeable#setChangedBy(User)
	 */
	@Override
	void setChangedBy(User changedBy);
	
	/**
	 * @see Changeable#getDateChanged()
	 */
	@Override
	Date getDateChanged();
	
	/**
	 * @see Changeable#setDateChanged(Date)
	 */
	@Override
	void setDateChanged(Date dateChanged);
}
