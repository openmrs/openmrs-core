/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Date;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.Person;
import org.openmrs.User;

public class JavaSerializationTest {
	
	@Test
	public void shouldSerializeOpenMrsData() {

        Date date = new Date();
        User user = new User(1);

        Person originalPerson = new Person();
        originalPerson.setGender("M");
        originalPerson.setBirthdate(date);
        originalPerson.setBirthdateEstimated(false);
        originalPerson.setUuid("abc123");
        originalPerson.setDateCreated(date);
        originalPerson.setCreator(user);
        originalPerson.setDateChanged(date);
        originalPerson.setChangedBy(user);
        originalPerson.setVoided(true);
        originalPerson.setVoidedBy(user);
        originalPerson.setDateVoided(date);
        originalPerson.setVoidReason("test");

        byte[] serialized = SerializationUtils.serialize(originalPerson);
        Person copyPerson = SerializationUtils.deserialize(serialized);

        assertThat(copyPerson.getGender(), is(originalPerson.getGender()));
        assertThat(copyPerson.getBirthdate(), is(originalPerson.getBirthdate()));
        assertThat(copyPerson.getBirthdateEstimated(), is(originalPerson.getBirthdateEstimated()));
        assertThat(copyPerson.getDateCreated(), is(originalPerson.getDateCreated()));
        assertThat(copyPerson.getCreator(), is(originalPerson.getCreator()));
        assertThat(copyPerson.getDateChanged(), is(originalPerson.getDateChanged()));
        assertThat(copyPerson.getChangedBy(), is(originalPerson.getChangedBy()));
        assertThat(copyPerson.getVoided(), is(originalPerson.getVoided()));
        assertThat(copyPerson.getVoidedBy(), is(originalPerson.getVoidedBy()));
        assertThat(copyPerson.getDateVoided(), is(originalPerson.getDateVoided()));
        assertThat(copyPerson.getVoidReason(), is(originalPerson.getVoidReason()));
        assertThat(copyPerson.getUuid(), is(originalPerson.getUuid()));
    }

    @Test
	public void shouldSerializeOpenMrsMetadata() {

        Date date = new Date();
        User user = new User(1);

        Concept originalConcept = new Concept();
        originalConcept.setConceptClass(new ConceptClass(1));
        originalConcept.setDatatype(new ConceptDatatype(1));
        originalConcept.setUuid("abc123");
        originalConcept.setDateCreated(date);
        originalConcept.setCreator(user);
        originalConcept.setDateChanged(date);
        originalConcept.setChangedBy(user);
        originalConcept.setRetired(true);
        originalConcept.setRetiredBy(user);
        originalConcept.setDateRetired(date);
        originalConcept.setRetireReason("test");

        byte[] serialized = SerializationUtils.serialize(originalConcept);
        Concept copyConcept = SerializationUtils.deserialize(serialized);

        assertThat(copyConcept.getConceptClass(), is(originalConcept.getConceptClass()));
        assertThat(copyConcept.getDatatype(), is(originalConcept.getDatatype()));
        assertThat(copyConcept.getDateCreated(), is(originalConcept.getDateCreated()));
        assertThat(copyConcept.getCreator(), is(originalConcept.getCreator()));
        assertThat(copyConcept.getDateChanged(), is(originalConcept.getDateChanged()));
        assertThat(copyConcept.getChangedBy(), is(originalConcept.getChangedBy()));
        assertThat(copyConcept.getRetired(), is(originalConcept.getRetired()));
        assertThat(copyConcept.getRetiredBy(), is(originalConcept.getRetiredBy()));
        assertThat(copyConcept.getDateRetired(), is(originalConcept.getDateRetired()));
        assertThat(copyConcept.getRetireReason(), is(originalConcept.getRetireReason()));
        assertThat(copyConcept.getUuid(), is(originalConcept.getUuid()));
    }
}
