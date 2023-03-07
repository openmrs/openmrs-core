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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for
 * {@link org.openmrs.ConceptSearchResult}, this resource is used to search for concepts that are
 * orderable. It returns ConceptSearchResults instead of Concepts
 */
@Resource(name = RestConstants.VERSION_1 + "/orderable", supportedClass = ConceptSearchResult.class, supportedOpenmrsVersions = {
        "1.10.* - 9.*" })
public class OrderableResource1_10 extends BaseDelegatingResource<ConceptSearchResult> implements Searchable {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ConceptSearchResult newDelegate() {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#save(Object)
	 */
	@Override
	public ConceptSearchResult save(ConceptSearchResult delegate) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public SimpleObject search(RequestContext context) throws ResponseException {
		
		String query = context.getParameter("q");
		
		List<OrderType> orderTypes = Context.getOrderService().getOrderTypes(false);
		List<ConceptClass> orderableConceptClasses = getOrderableConceptClasses(orderTypes);
		
		List<ConceptClass> conceptClasses = null;
		String[] classUuids = context.getRequest().getParameterValues("conceptClasses");
		
		if (classUuids != null) {
			for (String uuid : classUuids) {
				ConceptClass cc = (ConceptClass) ConversionUtil.convert(uuid, ConceptClass.class);
				if (cc != null) {
					if (conceptClasses == null) {
						conceptClasses = new ArrayList<ConceptClass>();
					}
					
					conceptClasses.add(cc);
				}
			}
		}
		
		String[] orderTypeUuids = context.getRequest().getParameterValues("orderTypes");
		if (orderTypeUuids != null) {
			
			for (String uuid : orderTypeUuids) {
				// look for order type in the orders collections
				OrderType orderType = getOrderTypeByUuid(uuid, orderTypes);
				
				if (orderType != null) {
					
					Collection<ConceptClass> orderTypeConceptClasses = orderType.getConceptClasses();
					if (orderableConceptClasses != null) {
						
						if (conceptClasses == null) {
							conceptClasses = new ArrayList<ConceptClass>();
						}
						
						for (ConceptClass cc : orderTypeConceptClasses) {
							
							if (!conceptClasses.contains(cc))
								conceptClasses.add(cc);
						}
					}
				}
			}
		}
		
		if (conceptClasses == null || conceptClasses.isEmpty())
			conceptClasses = orderableConceptClasses;
		
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(Context.getLocale());
		
		return new NeedsPaging<ConceptSearchResult>(Context.getConceptService().getConcepts(query, locales,
		    context.getIncludeAll(), conceptClasses, null, null, null, null, context.getStartIndex(), context.getLimit()),
		        context).toSimpleObject(this);
	}
	
	private OrderType getOrderTypeByUuid(String uuid, List<OrderType> orderTypes) {
		
		for (int i = 0; i < orderTypes.size(); i++) {
			if (orderTypes.get(i).getUuid().equals(uuid))
				return orderTypes.get(i);
		}
		return null;
	}
	
	protected List<ConceptClass> getOrderableConceptClasses(List<OrderType> orderTypes) {
		
		List<ConceptClass> conceptClasses = new ArrayList<ConceptClass>();
		
		if (orderTypes == null || orderTypes.isEmpty())
			return conceptClasses;
		
		for (int i = 0; i < orderTypes.size(); i++) {
			
			OrderType type = orderTypes.get(i);
			conceptClasses.addAll(type.getConceptClasses());
		}
		
		return conceptClasses;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ConceptSearchResult getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	protected void delete(ConceptSearchResult delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(ConceptSearchResult delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		if (rep instanceof RefRepresentation || rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("concept", Representation.REF);
			description.addProperty("conceptName", Representation.REF);
		} else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("conceptName", Representation.DEFAULT);
			description.addProperty("word");
			description.addProperty("transientWeight");
		}
		
		return description;
	}
	
	@PropertyGetter("display")
	public String getDisplayString(ConceptSearchResult csr) {
		ConceptName cn = csr.getConcept().getName();
		return cn == null ? null : cn.getName();
	}
}
