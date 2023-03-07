/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DrugOrderSubclassHandler1_10Test {
	
	@Test
	public void getDisplayString_shouldNotFailForDcOrder() throws Exception {
		Drug drug = new Drug();
		drug.setName("Aspirin");
		
		DrugOrder order = new DrugOrder();
		order.setAction(Order.Action.DISCONTINUE);
		order.setDrug(drug);
		
		String actual = DrugOrderSubclassHandler1_10.getDisplay(order);
		assertThat(actual, is("(DISCONTINUE) Aspirin"));
	}
}
