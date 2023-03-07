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

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.openmrs.module.webservices.rest.test.Util.getByPath;
import static org.openmrs.module.webservices.rest.test.Util.getResultsList;
import static org.openmrs.module.webservices.rest.test.Util.getResultsSize;

public class EncounterProviderResource1_9Test extends BaseDelegatingResourceTest<EncounterProviderResource1_9, EncounterProvider> {
	
	public static final String EXISTING_ENCOUNTER_PROVIDER_UUID = "19e0aae8-20ee-46b7-ade6-9e68f897b7a9";
	
	public static final String EXISTING_ENCOUNTER_PROVIDER_PROVIDER_UUID = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	public static final String EXISTING_ENCOUNTER_PROVIDER_ENCOUNTER_ROLE_UUID = "a0b03050-c99b-11e0-9572-0800200c9a66";
	
	@Autowired
	private EncounterService encounterService;
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropPresent("provider");
		assertPropPresent("encounterRole");
		assertPropEquals("encounterRole", getObject().getEncounterRole());
		assertPropEquals("provider", getObject().getProvider());
		assertPropEquals("uuid", getObject().getUuid());
	}
	
	@Override
	public EncounterProvider newObject() {
		EncounterProvider encounterProvider = new EncounterProvider();
		encounterProvider.setId(1);
		encounterProvider.setUuid(getUuidProperty());
		Provider provider = new Provider(1000);
		provider.setName("Testing Provider");
		
		EncounterRole rolePlayed = new EncounterRole(3000);
		rolePlayed.setName("Nurse");
		return encounterProvider;
	}
	
	@Override
	public String getDisplayProperty() {
		EncounterProvider encounterProvider = newObject();
		Provider provider = encounterProvider.getProvider();
		EncounterRole role = encounterProvider.getEncounterRole();
		
		if (role == null) {
			if (provider == null) {
				return null;
			}
			return provider.getName();
		}
		
		return provider.getName() + ": " + role.getName();
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.ENCOUNTER_PROVIDER_UUID;
	}
	
	@Test
	public void testGetAll() throws Exception {
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		
		SimpleObject all = resource.getAll(encounter.getUuid(), new RequestContext());
		assertThat(getResultsSize(all), is(1));
		List<Object> results = getResultsList(all);
		assertThat((String) getByPath(results.get(0), "uuid"), is(EXISTING_ENCOUNTER_PROVIDER_UUID));
		assertThat((String) getByPath(results.get(0), "provider.uuid"), is("c2299800-cca9-11e0-9572-0800200c9a66"));
		assertThat((String) getByPath(results.get(0), "encounterRole.uuid"), is("a0b03050-c99b-11e0-9572-0800200c9a66"));
	}
	
	@Test
	public void testGetOne() throws Exception {
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		
		Object one = resource.retrieve(encounter.getUuid(), EXISTING_ENCOUNTER_PROVIDER_UUID, new RequestContext());
		
		assertThat((String) getByPath(one, "uuid"), is(EXISTING_ENCOUNTER_PROVIDER_UUID));
		assertThat((String) getByPath(one, "provider.uuid"), is("c2299800-cca9-11e0-9572-0800200c9a66"));
		assertThat((String) getByPath(one, "encounterRole.uuid"), is("a0b03050-c99b-11e0-9572-0800200c9a66"));
	}
	
	@Test
	public void testAdd() throws Exception {
		// If we don't explicitly set a locale, then a Context.getLocale() call nested somewhere below will trigger a
		// premature hibernate flush because it looks up the global property for default locale
		Context.setLocale(Locale.UK);
		
		EncounterRole newRole = new EncounterRole();
		newRole.setName("Another role");
		encounterService.saveEncounterRole(newRole);
		
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		SimpleObject post = new SimpleObject().add("provider", "c2299800-cca9-11e0-9572-0800200c9a66").add("encounterRole",
		    newRole.getUuid());
		
		Object created = resource.create(encounter.getUuid(), post, new RequestContext());
		assertThat((String) getByPath(created, "provider.uuid"), is("c2299800-cca9-11e0-9572-0800200c9a66"));
		assertThat((String) getByPath(created, "encounterRole.uuid"), is(newRole.getUuid()));
		
		assertThat(getEncounterProviderCountWithoutFlushing(), is(2));
		assertThat(getEncounterProviderCountWithoutFlushingByUuid(getByPath(created, "uuid").toString()), is(1));
	}
	
	@Test
	public void testAddingDuplicateDoesNotCreateNewRecord() throws Exception {
		// If we don't explicitly set a locale, then a Context.getLocale() call nested somewhere below will trigger a
		// premature hibernate flush because it looks up the global property for default locale
		Context.setLocale(Locale.UK);
		
		// now add a provider *with the same provider and role* as the existing one
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		SimpleObject post = new SimpleObject().add("provider", EXISTING_ENCOUNTER_PROVIDER_PROVIDER_UUID).add(
		    "encounterRole", EXISTING_ENCOUNTER_PROVIDER_ENCOUNTER_ROLE_UUID);
		Object created = resource.create(encounter.getUuid(), post, new RequestContext());
		
		// should return uuid of *existing* encounter provider
		assertThat((String) getByPath(created, "uuid"), is(EXISTING_ENCOUNTER_PROVIDER_UUID));
		Context.flushSession();
		
		// there should still only be one provider
		assertThat(getEncounterProviderCountWithoutFlushing(), is(1));
	}
	
	@Test
	public void testDelete() throws Exception {
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		resource.delete(encounter.getUuid(), EXISTING_ENCOUNTER_PROVIDER_UUID, "reason", new RequestContext());
		
		Context.flushSession();
		assertThat(getNonVoidedEncounterProviderCount(), is(0));
	}
	
	@Test
	public void testDeleteAndAddAndDelete() throws Exception {
		// delete existing provider
		Encounter encounter = encounterService.getEncounter(3);
		EncounterProviderResource1_9 resource = getResource();
		resource.delete(encounter.getUuid(), EXISTING_ENCOUNTER_PROVIDER_UUID, "reason", new RequestContext());
		
		Context.flushSession();
		assertThat(getNonVoidedEncounterProviderCount(), is(0));
		
		// now add a provider *with the same provider and role as the one just deleted*
		SimpleObject post = new SimpleObject().add("provider", EXISTING_ENCOUNTER_PROVIDER_PROVIDER_UUID).add(
		    "encounterRole", EXISTING_ENCOUNTER_PROVIDER_ENCOUNTER_ROLE_UUID);
		resource.create(encounter.getUuid(), post, new RequestContext());
		
		// should now have 1 non-voided provider
		Context.flushSession();
		assertThat(getNonVoidedEncounterProviderCount(), is(1));
		
		// now delete again
		resource.delete(encounter.getUuid(), EXISTING_ENCOUNTER_PROVIDER_UUID, "reason", new RequestContext());
		
		// should be back down to zero again (this was failing previously)
		Context.flushSession();
		assertThat(getNonVoidedEncounterProviderCount(), is(0));
	}
	
	private int getEncounterProviderCountWithoutFlushing() {
		List<List<Object>> temp = Context.getAdministrationService().executeSQL(
		    "select count(*) from encounter_provider where encounter_id = 3", true);
		return ((Number) temp.get(0).get(0)).intValue();
	}
	
	private int getEncounterProviderCountWithoutFlushingByUuid(String uuid) {
		List<List<Object>> temp = Context.getAdministrationService().executeSQL(
		    "select count(*) from encounter_provider where uuid ='" + uuid + "'", true);
		return ((Number) temp.get(0).get(0)).intValue();
	}
	
	private int getNonVoidedEncounterProviderCount() {
		List<List<Object>> temp = Context.getAdministrationService().executeSQL(
		    "select count(*) from encounter_provider where encounter_id = 3 and voided = 0", true);
		return ((Number) temp.get(0).get(0)).intValue();
	}
}
