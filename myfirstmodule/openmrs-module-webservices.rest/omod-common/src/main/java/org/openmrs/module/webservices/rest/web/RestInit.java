/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

/**
 * Setups xstream marshaller to support annotated classes.
 */
@Component
public class RestInit {
	
	@Autowired
	@Qualifier("xStreamMarshaller")
	XStreamMarshaller marshaller;
	
	@PostConstruct
	public void init() {
		marshaller.setAutodetectAnnotations(true);
	}
}
