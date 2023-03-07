/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

/**
 * Implement and register one of these for each subclass handled by a class-hierarchy-supporting
 * resource
 */
public interface DelegatingSubclassHandler<Superclass, Subclass extends Superclass> extends DelegatingResourceHandler<Subclass> {
	
	/**
	 * @return the user-friendly name for the type this handles (e.g. "drugorder" for
	 *         org.openmrs.DrugOrder)
	 */
	String getTypeName();
	
	/**
	 * Convenience method that lets you retrieve the declared superclass at runtime without needing
	 * to use introspection yourself
	 * 
	 * @return
	 */
	Class<Superclass> getSuperclass();
	
	/**
	 * Convenience method that lets you retrieve the declared subclass at runtime without needing to
	 * use introspection yourself
	 * 
	 * @return
	 */
	Class<Subclass> getSubclassHandled();
	
	/**
	 * Gets all instances of this subclass of the resource
	 * 
	 * @param context
	 * @return
	 * @throws ResourceDoesNotSupportOperationException if this resource does not support the
	 *             operation
	 */
	PageableResult getAllByType(RequestContext context) throws ResourceDoesNotSupportOperationException;
	
}
