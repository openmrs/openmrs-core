/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.openmrs.ImplementationId;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.validator.ImplementationIdValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/implementationid")
public class ImplementationIdController2_0 extends BaseRestController {

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Object getCurrentConfiguration() {
		AdministrationService administrationService = Context.getAdministrationService();
		ImplementationId implementationId = administrationService.getImplementationId();

		return ConversionUtil.convertToRepresentation(implementationId, Representation.FULL);
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateCurrentConfiguration(@RequestBody ImplementationId implementationId) {
		AdministrationService administrationService = Context.getAdministrationService();

		BindException exceptions = new BindException(implementationId, "");
		new ImplementationIdValidator().validate(implementationId, exceptions);

		if (exceptions.hasErrors()) {
			throw new IllegalRequestException(exceptions);
		}

		administrationService.setImplementationId(implementationId);
	}

}
