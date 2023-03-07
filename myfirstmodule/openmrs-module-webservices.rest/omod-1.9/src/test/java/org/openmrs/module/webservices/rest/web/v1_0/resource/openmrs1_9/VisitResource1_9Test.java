/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Contains tests for the {@link VisitResource1_9}
 */
public class VisitResource1_9Test extends BaseDelegatingResourceTest<VisitResource1_9, Visit> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public Visit newObject() {
		return Context.getVisitService().getVisitByUuid(getUuidProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("patient");
		assertPropPresent("visitType");
		assertPropPresent("indication");
		assertPropPresent("location");
		assertPropPresent("encounters");
		assertPropEquals("startDatetime", getObject().getStartDatetime());
		assertPropEquals("stopDatetime", getObject().getStopDatetime());
		assertPropPresent("attributes");
		assertPropEquals("voided", getObject().isVoided());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("patient");
		assertPropPresent("visitType");
		assertPropPresent("indication");
		assertPropPresent("location");
		assertPropPresent("encounters");
		assertPropEquals("startDatetime", getObject().getStartDatetime());
		assertPropEquals("stopDatetime", getObject().getStopDatetime());
		assertPropPresent("attributes");
		assertPropEquals("voided", getObject().isVoided());
		assertPropPresent("auditInfo");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "Initial HIV Clinic Visit @ Unknown Location - 01/01/2005 00:00";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.VISIT_UUID;
	}
}
