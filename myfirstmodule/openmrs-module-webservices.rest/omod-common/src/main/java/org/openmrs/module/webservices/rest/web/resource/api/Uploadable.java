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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface implemented by resources that have the standard-pattern Upload operation, which takes a
 * file to create new instance of resource
 */
public interface Uploadable {
	
	/**
	 * Creates new instance of resource with given file and returns it
	 */
	public Object upload(MultipartFile file, RequestContext context) throws ResponseException, IOException;
}
