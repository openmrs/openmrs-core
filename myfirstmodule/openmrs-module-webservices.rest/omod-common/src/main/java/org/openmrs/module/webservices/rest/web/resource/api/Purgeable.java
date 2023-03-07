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
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Interface implemented by resources that support the standard-pattern Purge operation (i.e. a
 * DELETE on the resource, with a purge=true parameter)
 */
public interface Purgeable extends Resource {
	
	/**
	 * Deletes a resources from persistent storage, so that it no longer exists, not even in a
	 * voided or retired state.
	 * 
	 * @param uuid
	 * @param context
	 * @throws ObjectNotFoundException
	 * @throws ResponseException
	 */
	void purge(String uuid, RequestContext context) throws ResponseException;
	
}
