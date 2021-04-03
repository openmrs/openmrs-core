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
 * Base interface for domain objects that need to track information related to when they were
 * changed and the user that last changed them.
 * 
 * @since 2.2
 */
public interface Changeable extends OpenmrsObject {
	
	/**
	 * @return User - the user who last changed the object
	 */
	User getChangedBy();
	
	/**
	 * @param changedBy - the user who last changed the object
	 */
	void setChangedBy(User changedBy);
	
	/**
	 * @return Date - the date the object was last changed
	 */
	Date getDateChanged();
	
	/**
	 * @param dateChanged - the date the object was last changed
	 */
	void setDateChanged(Date dateChanged);
}
