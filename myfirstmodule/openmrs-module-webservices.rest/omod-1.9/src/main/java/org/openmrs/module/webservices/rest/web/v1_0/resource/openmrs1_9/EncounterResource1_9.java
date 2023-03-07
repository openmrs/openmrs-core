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

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link Resource} for {@link EncounterResource1_9}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/encounter", supportedClass = Encounter.class, supportedOpenmrsVersions = {
        "1.9.* - 2.1.*" })
public class EncounterResource1_9 extends org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.EncounterResource1_8 {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("visit", Representation.REF);
			description.removeProperty("provider");
			description.addProperty("encounterProviders", Representation.REF);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("visit", Representation.DEFAULT);
			description.removeProperty("provider");
			description.addProperty("encounterProviders", Representation.DEFAULT);
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = super.getUpdatableProperties();
		description.addProperty("encounterProviders");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("visit");
		description.addProperty("encounterProviders");
		return description;
	}
	
	@Override
	public Encounter save(Encounter delegate) {
		//This is a hack to save encounterProviders correctly. Without this they are created without encounter_id in
		//the database.
		for (EncounterProvider ep : delegate.getEncounterProviders()) {
			ep.setEncounter(delegate);
		}
		Context.getEncounterService().saveEncounter(delegate);
		return delegate;
	}
	
	@PropertyGetter("encounterProviders")
	public static Set<EncounterProvider> getActiveEncounterProviders(Encounter instance) {
		Set<EncounterProvider> encounterProviders = instance.getEncounterProviders();
		Set<EncounterProvider> providers = new LinkedHashSet<EncounterProvider>();
		for (EncounterProvider encounterProvider : encounterProviders) {
			if (!encounterProvider.isVoided()) {
				providers.add(encounterProvider);
			}
		}
		return providers;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_9.RESOURCE_VERSION;
	}
	
}
