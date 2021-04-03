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
 * created and the user that created them.
 * 
 * @since 2.2
 */
public interface Creatable extends OpenmrsObject {
	
	/**
	 * @return User - the user who created the object
	 */
	User getCreator();
	
	/**
	 * @param creator - the user who created the object
	 */
	void setCreator(User creator);
	
	/**
	 * @return Date - the date the object was created
	 */
	Date getDateCreated();
	
	/**
	 * @param dateCreated - the date the object was created
	 */
	void setDateCreated(Date dateCreated);
}
