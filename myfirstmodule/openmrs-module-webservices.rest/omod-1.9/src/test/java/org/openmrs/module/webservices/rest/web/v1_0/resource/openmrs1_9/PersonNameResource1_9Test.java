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

import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PersonNameResource1_8;

public class PersonNameResource1_9Test extends BaseDelegatingResourceTest<PersonNameResource1_8, PersonName> {
	
	@Override
	public PersonName newObject() {
		return Context.getPersonService().getPersonNameByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("givenName", getObject().getGivenName());
		assertPropEquals("middleName", getObject().getMiddleName());
		assertPropEquals("familyName", getObject().getFamilyName());
		assertPropEquals("familyName2", getObject().getFamilyName2());
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("givenName", getObject().getGivenName());
		assertPropEquals("middleName", getObject().getMiddleName());
		assertPropEquals("familyName", getObject().getFamilyName());
		assertPropEquals("familyName2", getObject().getFamilyName2());
		assertPropEquals("preferred", getObject().getPreferred());
		assertPropEquals("prefix", getObject().getPrefix());
		assertPropEquals("familyNamePrefix", getObject().getFamilyNamePrefix());
		assertPropEquals("familyNameSuffix", getObject().getFamilyNameSuffix());
		assertPropEquals("degree", getObject().getDegree());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Horatio Test Hornblower";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.PERSON_NAME_UUID;
	}
	
}
