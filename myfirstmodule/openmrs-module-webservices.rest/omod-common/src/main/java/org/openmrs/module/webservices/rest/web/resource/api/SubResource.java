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
 * A resource that belongs completely to another resource. You only interact with this resource
 * through its parent resource (e.g. adding children to a parent, listing all children of a parent)
 * but not directly (e.g. you cannot search for all sub-resources of any parent with a given
 * characteristic.
 */
public interface SubResource extends Resource {
	
	/**
	 * Add a sub-resource to a parent resource
	 * 
	 * @param parentUniqueId
	 * @param post
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	Object create(String parentUniqueId, SimpleObject post, RequestContext context) throws ResponseException;
	
	/**
	 * Fetch the given sub-resource of the given parent resource
	 * 
	 * @param parentUniqueId
	 * @param uniqueId
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	Object retrieve(String parentUniqueId, String uniqueId, RequestContext context) throws ResponseException;
	
	/**
	 * Edit an existing sub-resource of a given parent resource
	 * 
	 * @param parentUniqueId
	 * @param uniqueId
	 * @param propertiesToUpdate
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	Object update(String parentUniqueId, String uniqueId, SimpleObject propertiesToUpdate, RequestContext context)
	        throws ResponseException;
	
	/**
	 * Remove an existing sub-resource from a parent resource (voiding it or retiring it)
	 * 
	 * @param parentUniqueId
	 * @param uniqueId
	 * @param reason
	 * @param context
	 * @throws ResponseException
	 */
	void delete(String parentUniqueId, String uniqueId, String reason, RequestContext context) throws ResponseException;
	
	/**
	 * Completely removes an existing sub-resource from persistent storage
	 * 
	 * @param parentUniqueId
	 * @param uniqueId
	 * @param context
	 * @throws ResponseException
	 */
	void purge(String parentUniqueId, String uniqueId, RequestContext context) throws ResponseException;
	
	/**
	 * Lists all instances of this sub-resource that belong to the given parent resource
	 * 
	 * @param parentUniqueId
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	SimpleObject getAll(String parentUniqueId, RequestContext context) throws ResponseException;
	
	/**
	 * Put a sub-resource to a parent resource
	 * 
	 * @param parentUniqueId
	 * @param post
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	void put(String parentUniqueId, SimpleObject post, RequestContext context) throws ResponseException;
	
}
