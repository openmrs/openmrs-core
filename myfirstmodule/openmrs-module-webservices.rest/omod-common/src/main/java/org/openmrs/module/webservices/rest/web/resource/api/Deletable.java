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

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Interface implemented by resources that support the standard-pattern Delete operation
 */
public interface Deletable extends Resource {
	
	/**
	 * Deletes the specified resource, which in the OpenMRS context means either voiding or retiring
	 * it
	 * 
	 * @param uuid
	 * @param reason
	 * @param context
	 * @throws ResponseException
	 */
	void delete(String uuid, String reason, RequestContext context) throws ResponseException;
	
	/**
	 * Undeletes the specified resource, which in the OpenMRS context means either unvoiding or
	 * unretiring it
	 * 
	 * @param uuid Uuid for the resource to unvoid/unretire
	 * @param context Holds information related to a REST web service request
	 * @throws ResponseException
	 * @return unvoided/unretired object
	 * @since 2.25.0
	 */
	Object undelete(String uuid, RequestContext context) throws ResponseException;
	
}
