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

import org.openmrs.DrugOrder;

/**
 * This is a contrived example for testing purposes
 */
public class HivDrugOrder extends DrugOrder {
	
	private static final long serialVersionUID = 1L;
	
	// just a plain DrugOrder works fine for testing.
	// We use @PropertySetter and @PropertyGetter in the subclass handler to expose a virtual "standardRegimenCode" property
	
}
