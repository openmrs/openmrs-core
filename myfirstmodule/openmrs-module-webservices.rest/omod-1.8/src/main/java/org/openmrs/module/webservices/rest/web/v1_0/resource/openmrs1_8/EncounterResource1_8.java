/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.resource.impl.ServiceSearcher;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource for Encounters, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/encounter", supportedClass = Encounter.class, supportedOpenmrsVersions = "1.8.*")
public class EncounterResource1_8 extends DataDelegatingCrudResource<Encounter> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("encounterDatetime");
			description.addProperty("patient", Representation.REF);
			description.addProperty("location", Representation.REF);
			description.addProperty("form", Representation.REF);
			description.addProperty("encounterType", Representation.REF);
			description.addProperty("provider", Representation.REF);
			description.addProperty("obs", Representation.REF);
			description.addProperty("orders", Representation.REF);
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("encounterDatetime");
			description.addProperty("patient", Representation.REF);
			description.addProperty("location");
			description.addProperty("form");
			description.addProperty("encounterType");
			description.addProperty("provider");
			description.addProperty("obs");
			description.addProperty("orders");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("encounterDatetime", new DateProperty())
			        .property("provider", new StringProperty()) //FIXME
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("patient", new RefProperty("#/definitions/PatientGetRef")) //FIXME
			        .property("location", new RefProperty("#/definitions/LocationGetRef")) //FIXME
			        .property("form", new RefProperty("#/definitions/FormGetRef")) //FIXME
			        .property("encounterType", new RefProperty("#/definitions/EncountertypeGetRef")) //FIXME
			        .property("obs", new ArrayProperty(new RefProperty("#/definitions/ObsGetRef"))) //FIXME
			        .property("orders", new ArrayProperty(new RefProperty("#/definitions/OrderGetRef"))); //FIXME
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("patient", new RefProperty("#/definitions/PatientGet")) //FIXME
			        .property("location", new RefProperty("#/definitions/LocationGet")) //FIXME
			        .property("form", new RefProperty("#/definitions/FormGet")) //FIXME
			        .property("encounterType", new RefProperty("#/definitions/EncountertypeGet")) //FIXME
			        .property("obs", new ArrayProperty(new RefProperty("#/definitions/ObsGet"))) //FIXME
			        .property("orders", new ArrayProperty(new RefProperty("#/definitions/OrderGet"))); //FIXME
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("patient", new RefProperty("#/definitions/PatientCreate"))
		        .property("encounterType", new RefProperty("#/definitions/EncountertypeCreate"))
		        .property("encounterDatetime", new DateProperty())
		        .property("location", new RefProperty("#/definitions/LocationCreate"))
		        .property("form", new RefProperty("#/definitions/FormCreate"))
		        .property("provider", new StringProperty())
		        .property("orders", new ArrayProperty(new RefProperty("#/definitions/OrderCreate")))
		        .property("obs", new ArrayProperty(new RefProperty("#/definitions/ObsCreate")))
		        
		        .required("patient").required("encounterType");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 * <strong>Should</strong> create an encounter type
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addProperty("encounterDatetime"); // has a default value set, hence not required here
		description.addRequiredProperty("patient");
		description.addRequiredProperty("encounterType");
		
		description.addProperty("location");
		description.addProperty("form");
		description.addProperty("provider");
		description.addProperty("orders");
		description.addProperty("obs");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Encounter newDelegate() {
		Encounter enc = new Encounter();
		// default to now(), so a web client can create a real-time encounter based on the server time
		enc.setEncounterDatetime(new Date());
		// As of 2012-04-27 there is a bug in Encounter.getOrders() where, if null, it returns an empty list without keeping a reference to it
		enc.setOrders(new LinkedHashSet<Order>());
		return enc;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(org.openmrs.Encounter)
	 */
	@Override
	public Encounter save(Encounter enc) {
		return Context.getEncounterService().saveEncounter(enc);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Encounter getByUniqueId(String uuid) {
		return Context.getEncounterService().getEncounterByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(org.openmrs.Encounter,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(Encounter enc, String reason, RequestContext context) throws ResponseException {
		if (enc.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getEncounterService().voidEncounter(enc, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#undelete(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected Encounter undelete(Encounter enc, RequestContext context) throws ResponseException {
		if (enc.isVoided()) {
			enc = Context.getEncounterService().unvoidEncounter(enc);
		}
		return enc;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(org.openmrs.Encounter,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Encounter enc, RequestContext context) throws ResponseException {
		if (enc == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getEncounterService().purgeEncounter(enc);
	}
	
	/**
	 * @param encounter
	 * @return encounter type and date
	 */
	@PropertyGetter("display")
	public String getDisplayString(Encounter encounter) {
		String ret = encounter.getEncounterType() == null ? "?" : encounter.getEncounterType().getName();
		ret += " ";
		ret += encounter.getEncounterDatetime() == null ? "?" : Context.getDateFormat().format(
		    encounter.getEncounterDatetime());
		return ret;
	}
	
	/**
	 * @param instance
	 * @return all non-voided top-level obs from the given encounter
	 */
	@PropertyGetter("obs")
	public static Object getObsAtTopLevel(Encounter instance) {
		return instance.getObsAtTopLevel(false);
	}
	
	@PropertySetter("obs")
	public static void setObs(Encounter instance, Set<Obs> obs) {
		instance.getAllObs(true).clear();
		for (Obs o : obs)
			instance.addObs(o);
	}
	
	@PropertySetter("orders")
	public static void setOrders(Encounter instance, Set<Order> orders) {
		for (Order o : orders)
			instance.addOrder(o);
	}
	
	/**
	 * Gets encounters for the given patient (paged according to context if necessary) only if a
	 * patient parameter exists in the request set on the {@link RequestContext} otherwise searches
	 * for encounters that match the specified query
	 * 
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			if (patient == null)
				return new EmptySearchResult();
			List<Encounter> encs = Context.getEncounterService().getEncountersByPatient(patient);
			return new NeedsPaging<Encounter>(encs, context);
		}
		
		return new ServiceSearcher<Encounter>(EncounterService.class, "getEncounters", "getCountOfEncounters").search(
		    context.getParameter("q"), context);
	}
	
}
