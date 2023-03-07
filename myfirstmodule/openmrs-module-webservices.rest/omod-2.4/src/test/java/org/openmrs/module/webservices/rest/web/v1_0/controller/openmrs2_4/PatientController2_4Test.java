/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_4;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.api.db.PersonDAO;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Date;

import static org.junit.Assert.assertEquals;

public class PatientController2_4Test extends RestControllerTestUtils {

	@Autowired
	private PatientDAO hibernatePatientDao;

	@Autowired
	private PersonDAO hibernatePersonDAO;

	@Test
	@Ignore("TRUNK-6010: The generated SQL contains syntax error in Core before version 2.5. This has been fixed on commit c3f63301. Once a submodule that depends on core version 2.5 is added, this test should be moved there and unignored.")
	public void shouldReturnDuplicatedPatientsByAllAttributesIncludingVoided() throws Exception {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		Patient patient1 = new Patient(person1);
		patient1.addIdentifier(new PatientIdentifier("101X", null, null));
		patient1.setVoided(true);
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", null, null));
		patient2.setVoided(true);
		hibernatePatientDao.savePatient(patient2);

		Context.flushSession();

		// when
		MockHttpServletRequest req = request(RequestMethod.GET, "patient");
		req.addParameter("attributesToFindDuplicatesBy", "givenName,middleName,familyName,gender,identifier,birthdate,includeVoided");

		SimpleObject result = deserialize(handle(req));

		// then
		assertEquals(2, Util.getResultsSize(result));
	}

}
