/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/orderset", supportedClass = OrderSet.class, supportedOpenmrsVersions = {
        "1.12.* - 9.*" })
public class OrderSetResource1_12 extends MetadataDelegatingCrudResource<OrderSet> {
	
	@Override
	public OrderSet getByUniqueId(String uniqueId) {
		return Context.getOrderSetService().getOrderSetByUuid(uniqueId);
	}
	
	@Override
	public OrderSet newDelegate() {
		return new OrderSet();
	}
	
	@Override
	public OrderSet save(OrderSet orderSet) {
		if (CollectionUtils.isNotEmpty(orderSet.getOrderSetMembers())) {
			for (OrderSetMember orderSetMember : orderSet.getOrderSetMembers()) {
				if (null != orderSetMember.getConcept() && StringUtils.isNotEmpty(orderSetMember.getConcept().getUuid())) {
					orderSetMember.setConcept(Context.getConceptService().getConceptByUuid(
					    orderSetMember.getConcept().getUuid()));
				}
				if (null != orderSetMember.getOrderType() && StringUtils.isNotEmpty(orderSetMember.getOrderType().getUuid())) {
					orderSetMember.setOrderType(Context.getOrderService().getOrderTypeByUuid(
					    orderSetMember.getOrderType().getUuid()));
				}
			}
		}
		return Context.getOrderSetService().saveOrderSet(orderSet);
	}
	
	@PropertySetter("orderSetMembers")
	public static void setOrderSetMembers(OrderSet instance, List<OrderSetMember> orderSetMembers) {
		instance.setOrderSetMembers(orderSetMembers);
	}
	
	@Override
	public void purge(OrderSet delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<OrderSet> doGetAll(RequestContext context) {
		return new NeedsPaging<OrderSet>(Context.getOrderSetService().getOrderSets(context.getIncludeAll()), context);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("operator");
			description.addProperty("orderSetMembers", Representation.REF);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = super.getRepresentationDescription(rep);
			description.addProperty("operator");
			description.addProperty("orderSetMembers", Representation.DEFAULT);
			return description;
		} else {
			return null;
		}
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = super.getCreatableProperties();
		d.addProperty("operator");
		d.addProperty("orderSetMembers");
		return d;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("operator", new EnumProperty(OrderSet.Operator.class));
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("orderSetMembers", new ArrayProperty(new RefProperty(
			                "#/definitions/OrdersetOrdersetmemberGetRef")));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("orderSetMembers", new ArrayProperty(
			                new RefProperty("#/definitions/OrdersetOrdersetmemberGet")));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		return new ModelImpl()
		        .property("operator", new EnumProperty(OrderSet.Operator.class))
		        .property("orderSetMembers",
		            new ArrayProperty(new RefProperty("#/definitions/OrdersetOrdersetmemberCreate")));
	}
}
