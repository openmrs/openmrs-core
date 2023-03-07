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
 * Interface implemented by resources that can return a complete listing all their instances.
 * Typically this will be implemented by metadata resources, and not by data resources
 */
public interface Listable extends Resource {
	
	/**
	 * Fetches for all instances of the given resource in the database
	 * 
	 * @param context the {@link RequestContext} object
	 * @return a list of all objects in the database represented by the implementing resource
	 * @throws ResponseException
	 */
	public SimpleObject getAll(RequestContext context) throws ResponseException;
	
}
