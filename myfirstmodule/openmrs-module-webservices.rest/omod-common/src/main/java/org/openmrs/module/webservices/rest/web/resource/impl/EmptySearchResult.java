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

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Collections;

/**
 * Empty list of search results
 */
public class EmptySearchResult implements PageableResult {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.PageableResult#toSimpleObject(Converter)
	 */
	@Override
	public SimpleObject toSimpleObject(Converter<?> preferredConverter) throws ResponseException {
		return new SimpleObject().add("results", Collections.emptyList());
	}
	
}
