/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.UnknownResourceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles unknown resource requests which result in 404.
 * 
 * @see https://jira.springsource.org/browse/SPR-8837
 */
@Controller
@RequestMapping("/rest/unknownResource")
public class UnknownResourceController extends BaseRestController {
	
	/**
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject handleUnknownResource() throws UnknownResourceException {
		throw new UnknownResourceException();
	}
}
