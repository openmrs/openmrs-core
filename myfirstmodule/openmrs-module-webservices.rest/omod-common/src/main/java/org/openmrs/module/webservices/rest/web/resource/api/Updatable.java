/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.api;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Interface implemented by resources that have the standard-pattern Update operation, which takes a
 * map of properties to update on the resource
 */
public interface Updatable extends Resource {
	
	/**
	 * Sets the given properties on the resource identified by the given uuid
	 * 
	 * @param uuid
	 * @param propertiesToUpdate
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException;
	
}
