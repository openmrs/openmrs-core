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
import org.openmrs.notification.AlertRecipient;

public class AlertRecipientResource2_0Test extends BaseDelegatingResourceTest<AlertRecipientResource2_0, AlertRecipient> {

	@Override
	public AlertRecipient newObject() {
		AlertRecipient recipient = new AlertRecipient();
		recipient.setUuid(getUuidProperty());
		recipient.setRecipient(Context.getUserService().getUserByUuid(RestTestConstants1_8.USER_UUID));
		recipient.setAlertRead(true);
		return recipient;
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("recipient");
		assertPropEquals("alertRead", true);
		assertPropPresent("dateChanged");
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("recipient");
		assertPropEquals("alertRead", true);
		assertPropPresent("dateChanged");
	}

	@Override
	public String getDisplayProperty() {
		return Context.getUserService().getUserByUuid(RestTestConstants1_8.USER_UUID).getDisplayString();
	}

	@Override
	public String getUuidProperty() {
		return "647beb7c-d4fc-404c-b751-5c0dc14f5345";
	}
}
