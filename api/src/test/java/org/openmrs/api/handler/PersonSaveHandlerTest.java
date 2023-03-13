/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests the {@link PersonSaveHandler} class
 */
public class PersonSaveHandlerTest extends BaseContextSensitiveTest {

    /**
     * @see PersonSaveHandler#handle(Person,User, Date,String)
     */
    @Test
    public void handle_shouldIgnoreBlankAddresses() {
        PersonSaveHandler handler = new PersonSaveHandler();
        Person person = new Person();
        PersonName personName = new PersonName("John","","Smith");
        person.addName(personName);
        person.setGender("M");
        PersonAddress personAddress = new PersonAddress();
        personAddress.setAddress1("     ");
        person.addAddress(personAddress);

        handler.handle(person,null,null,null);
        assertEquals(0,person.getAddresses().size());
    }
}
