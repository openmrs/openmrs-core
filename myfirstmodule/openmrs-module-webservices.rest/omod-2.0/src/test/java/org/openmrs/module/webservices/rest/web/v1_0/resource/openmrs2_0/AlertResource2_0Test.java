/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.notification.Alert;

public class AlertResource2_0Test extends BaseDelegatingResourceTest<AlertResource2_0, Alert> {

	@Override
	public Alert newObject() {
		Alert alert = new Alert();
		alert.setUuid(getUuidProperty());
		alert.setAlertId(1);
		alert.setText("New Alert");
		alert.setSatisfiedByAny(true);
		alert.setAlertRead(true);
		alert.addRecipient(Context.getUserService().getUserByUuid(RestTestConstants1_8.USER_UUID));
		return alert;
	}

	@Override
	public void validateRefRepresentation() throws Exception {
		super.validateRefRepresentation();
		assertPropEquals("alertId", 1);
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("alertId", 1);
		assertPropEquals("text", "New Alert");
		assertPropEquals("satisfiedByAny", true);
		assertPropEquals("alertRead", true);
		assertPropEquals("dateToExpire", null);
		assertPropPresent("recipients");
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("alertId", 1);
		assertPropEquals("text", "New Alert");
		assertPropEquals("satisfiedByAny", true);
		assertPropEquals("alertRead", true);
		assertPropEquals("dateToExpire", null);
		assertPropPresent("creator");
		assertPropPresent("dateCreated");
		assertPropPresent("changedBy");
		assertPropPresent("dateChanged");
		assertPropPresent("recipients");
		assertPropPresent("recipients");
	}

	@Override
	public String getDisplayProperty() {
		return "New Alert";
	}

	@Override
	public String getUuidProperty() {
		return "78c97b6b-ef39-47a1-ad77-73494e078ecb";
	}
}
