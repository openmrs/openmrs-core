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

import java.util.List;

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Interface implemented by resources that have the standard-pattern Retrieve operation by UUID
 */
public interface Retrievable extends Resource {
	
	/**
	 * Gets the object with the given uuid, in the given representation
	 * 
	 * @param uuid
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	public Object retrieve(String uuid, RequestContext context) throws ResponseException;
	
	/**
	 * @return all representations under which resource can be retrieved
	 */
	List<Representation> getAvailableRepresentations();
	
}
