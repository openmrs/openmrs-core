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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * {@link Resource} for OrderSetMembers, supporting standard CRUD operations
 */
@SubResource(parent = OrderSetResource1_12.class, path = "ordersetmember", supportedClass = OrderSetMember.class, supportedOpenmrsVersions = {
        "1.12.* - 9.*" })
public class OrderSetMemberResource1_12 extends DelegatingSubResource<OrderSetMember, OrderSet, OrderSetResource1_12> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		if (rep instanceof DefaultRepresentation) {
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("retired");
			description.addProperty("orderType", Representation.REF);
			description.addProperty("orderTemplate");
			description.addProperty("orderTemplateType");
			description.addProperty("concept", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("retired");
			description.addProperty("orderType", Representation.DEFAULT);
			description.addProperty("orderTemplate");
			description.addProperty("orderTemplateType");
			description.addProperty("concept", Representation.DEFAULT);
			description.addSelfLink();
			description.addProperty("auditInfo");
			return description;
		} else if (rep instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("concept", Representation.REF);
			description.addSelfLink();
		}
		return null;
	}
	
	@PropertyGetter("display")
	public String getDisplayString(OrderSetMember orderSetMember) {
		return orderSetMember.getDescription();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("orderType");
		description.addProperty("orderTemplate");
		description.addProperty("concept");
		description.addProperty("retired");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("retired", new BooleanProperty())
			        .property("orderTemplate", new StringProperty())
			        .property("orderTemplateType", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("orderType", new RefProperty("#/definitions/OrdertypeGetRef"))
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("orderType", new RefProperty("#/definitions/OrdertypeGet"))
			        .property("concept", new RefProperty("#/definitions/ConceptGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("orderType", new ObjectProperty()
		                .property("uuid", new StringProperty()))
		        .property("orderTemplate", new StringProperty())
		        .property("concept", new StringProperty().example("uuid"))
		        .property("retired", new BooleanProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	@Override
	public OrderSetMember getByUniqueId(String uniqueId) {
		return Context.getOrderSetService().getOrderSetMemberByUuid(uniqueId);
	}
	
	@Override
	protected void delete(OrderSetMember orderSetMember, String reason, RequestContext context) throws ResponseException {
		OrderSet orderSet = orderSetMember.getOrderSet();
		orderSet.retireOrderSetMember(orderSetMember);
		Context.getOrderSetService().saveOrderSet(orderSet);
	}
	
	@Override
	public OrderSetMember newDelegate() {
		return new OrderSetMember();
	}
	
	@Override
	public OrderSetMember save(OrderSetMember delegate) {
		OrderSet parent = delegate.getOrderSet();
		parent.addOrderSetMember(delegate);
		Context.getOrderSetService().saveOrderSet(parent);
		return delegate;
	}
	
	@Override
	public void purge(OrderSetMember orderSetMember, RequestContext context) throws ResponseException {
		OrderSet orderSet = orderSetMember.getOrderSet();
		orderSet.removeOrderSetMember(orderSetMember);
		Context.getOrderSetService().saveOrderSet(orderSet);
	}
	
	@Override
	public OrderSet getParent(OrderSetMember instance) {
		return instance.getOrderSet();
	}
	
	@Override
	public void setParent(OrderSetMember instance, OrderSet orderSet) {
		instance.setOrderSet(orderSet);
	}
	
	@Override
	public PageableResult doGetAll(OrderSet parent, RequestContext context) throws ResponseException {
		List<OrderSetMember> orderSetMembers = new ArrayList<OrderSetMember>();
		if (parent != null) {
			for (OrderSetMember orderSetMember : parent.getOrderSetMembers()) {
				orderSetMembers.add(orderSetMember);
			}
		}
		return new NeedsPaging<OrderSetMember>(orderSetMembers, context);
	}
}
