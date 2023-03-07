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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for EncounterProvider,
 * supporting standard CRUD operations
 */
@SubResource(path = "encounterprovider", parent = EncounterResource1_9.class, supportedClass = EncounterProvider.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class EncounterProviderResource1_9 extends DelegatingSubResource<EncounterProvider, Encounter, EncounterResource1_9> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("provider", Representation.REF);
			description.addProperty("encounterRole", Representation.REF);
			description.addProperty("voided");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		}
		if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("provider", Representation.DEFAULT);
			description.addProperty("encounterRole", Representation.DEFAULT);
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("provider");
		description.addProperty("encounter");
		description.addProperty("encounterRole");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("encounterRole");
		description.addProperty("voided");
		description.addProperty("voidReason");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("provider", new RefProperty("#/definitions/ProviderGetRef"))
			        .property("encounterRole", new RefProperty("#/definitions/EncounterroleGetRef"))
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("provider", new RefProperty("#/definitions/ProviderGet"))
			        .property("encounterRole", new RefProperty("#/definitions/EncounterroleGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("provider", new StringProperty().example("uuid"))
		        .property("encounterRole", new StringProperty().example("uuid"))
		        .property("encounter", new StringProperty()); //FIXME remove if not needed
		if (rep instanceof FullRepresentation) {
			model
			        .property("provider", new RefProperty("#/definitions/ProviderCreate"))
			        .property("encounter", new RefProperty("#/definitions/EncounterCreate"))
			        .property("encounterRole", new RefProperty("#/definitions/EncounterroleCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("encounterRole", new StringProperty())
		        .property("voided", new BooleanProperty())
		        .property("voidReason", new StringProperty());
	}
	
	@Override
	public Encounter getParent(EncounterProvider instance) {
		return instance.getEncounter();
	}
	
	@Override
	public void setParent(EncounterProvider instance, Encounter parent) {
		instance.setEncounter(parent);
	}
	
	@Override
	public PageableResult doGetAll(Encounter parent, RequestContext context) throws ResponseException {
		List<EncounterProvider> encounterProviders = new ArrayList<EncounterProvider>(parent.getEncounterProviders());
		if (!context.getIncludeAll()) {
			for (Iterator<EncounterProvider> i = encounterProviders.iterator(); i.hasNext();) {
				if (i.next().getVoided()) {
					i.remove();
				}
			}
		}
		return new NeedsPaging<EncounterProvider>(encounterProviders, context);
	}
	
	@Override
	public EncounterProvider getByUniqueId(String uniqueId) {
		return Context.getService(RestHelperService.class).getObjectByUuid(EncounterProvider.class, uniqueId);
	}
	
	@Override
	protected void delete(EncounterProvider delegate, String reason, RequestContext context) throws ResponseException {
		// workaround for removing providers until TRUNK-5017 is fixed
		for (EncounterProvider encounterProvider : delegate.getEncounter().getEncounterProviders()) {
			if (encounterProvider.getEncounterRole().equals(delegate.getEncounterRole())
			        && encounterProvider.getProvider().equals(delegate.getProvider()) && !encounterProvider.isVoided()) {
				encounterProvider.setVoided(true);
				encounterProvider.setDateVoided(new Date());
				encounterProvider.setVoidedBy(Context.getAuthenticatedUser());
			}
		}
		
		Context.getEncounterService().saveEncounter(delegate.getEncounter());
	}
	
	@Override
	public EncounterProvider newDelegate() {
		return new EncounterProvider();
	}
	
	@Override
	public EncounterProvider save(EncounterProvider delegate) {
		
		delegate.getEncounter().addProvider(delegate.getEncounterRole(), delegate.getProvider());
		Context.getEncounterService().saveEncounter(delegate.getEncounter());
		
		// bit of a hack, but since addProvider does not return the provider added, we need to fetch it
		// so that the returned delegate has the proper persisted uuid
		// see: https://issues.openmrs.org/browse/RESTWS-638
		for (EncounterProvider encounterProvider : delegate.getEncounter().getEncounterProviders()) {
			if (encounterProvider.getEncounterRole().equals(delegate.getEncounterRole())
			        && encounterProvider.getProvider().equals(delegate.getProvider()) && !encounterProvider.isVoided()) {
				return encounterProvider;
			}
		}
		
		// should never get here, hopefully!
		throw new APIException("Encounter Provider not properly saved");
	}
	
	@Override
	public void purge(EncounterProvider delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * Display string for EncounterProvider
	 * 
	 * @param encounterProvider
	 * @return String uuid
	 */
	@PropertyGetter("display")
	public String getDisplayString(EncounterProvider encounterProvider) {
		if (encounterProvider == null) {
			return "";
		}
		
		Provider provider = encounterProvider.getProvider();
		EncounterRole rolePlayed = encounterProvider.getEncounterRole();
		
		if (rolePlayed == null) {
			if (provider == null) {
				return null;
			} else {
				return provider.getName();
			}
		}
		return provider.getName() + ": " + rolePlayed.getName();
	}
	
	@Override
	public String getResourceVersion() {
		return RestConstants1_9.RESOURCE_VERSION;
	}
}
