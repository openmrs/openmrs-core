/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.openmrs.Concept;
import org.openmrs.OrderFrequency;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.ConceptResource1_9;

/**
 * {@link Resource} for {@link OrderFrequency}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/orderfrequency", supportedClass = OrderFrequency.class, supportedOpenmrsVersions = {
        "1.10.* - 9.*" })
public class OrderFrequencyResource1_10 extends MetadataDelegatingCrudResource<OrderFrequency> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("frequencyPerDay");
			description.addProperty("retired");
			description.addProperty("concept", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("frequencyPerDay");
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("retired");
			description.addSelfLink();
			description.addProperty("auditInfo");
			return description;
		} else if (rep.getRepresentation().equals("fullconcept")) {
			DelegatingResourceDescription description = getRepresentationDescription(Representation.FULL);
			description.addProperty("concept", Representation.FULL);
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("frequencyPerDay");
		description.addRequiredProperty("concept");
		
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public OrderFrequency newDelegate() {
		return new OrderFrequency();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public OrderFrequency save(OrderFrequency orderFrequency) {
		return Context.getOrderService().saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * Fetches a orderFrequency by uuid, or by the uuid or reference term of its concept. (E.g.
	 * supports specifying as "SNOMED CT:307486002")
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public OrderFrequency getByUniqueId(String uuid) {
		OrderFrequency frequency = Context.getOrderService().getOrderFrequencyByUuid(uuid);
		if (frequency == null) {
			// concept resource handles things like "SNOMED CT:307486002" in addition to UUIDs
			Concept concept = new ConceptResource1_9().getByUniqueId(uuid);
			if (concept != null) {
				frequency = Context.getOrderService().getOrderFrequencyByConcept(concept);
			}
		}
		return frequency;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(OrderFrequency orderFrequency, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<OrderFrequency> doGetAll(RequestContext context) {
		return new NeedsPaging<OrderFrequency>(Context.getOrderService().getOrderFrequencies(context.getIncludeAll()),
		        context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<OrderFrequency> doSearch(RequestContext context) {
		return new NeedsPaging<OrderFrequency>(Context.getOrderService().getOrderFrequencies(context.getParameter("q"),
		    null, false, context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_10.RESOURCE_VERSION;
	}
}
