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

import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

@Resource(name = RestConstants.VERSION_1 + "/ordergroup", supportedClass = OrderGroup.class, supportedOpenmrsVersions = {
        "1.12.* - 9.*" })
public class OrderGroupResource1_12 extends DataDelegatingCrudResource<OrderGroup> {
	
	@Override
	public OrderGroup newDelegate() {
		return new OrderGroup();
	}
	
	@Override
	public OrderGroup save(OrderGroup delegate) {
		return Context.getOrderService().saveOrderGroup(delegate);
	}
	
	@Override
	public OrderGroup getByUniqueId(String uniqueId) {
		
		return Context.getOrderService().getOrderGroupByUuid(uniqueId);
	}
	
	@Override
	protected void delete(OrderGroup delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			return;
		}
		
		for (Order order : delegate.getOrders()) {
			Context.getOrderService().voidOrder(order, reason);
		}
	}
	
	@Override
	public void purge(OrderGroup delegate, RequestContext context) throws ResponseException {
		for (Order order : delegate.getOrders()) {
			Context.getOrderService().purgeOrder(order);
		}
		
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("voided");
			description.addProperty("patient", Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("orders", Representation.REF);
			description.addProperty("orderSet", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
			
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addProperty("patient", Representation.DEFAULT);
			description.addProperty("encounter", Representation.DEFAULT);
			description.addProperty("orders", Representation.DEFAULT);
			description.addProperty("orderSet", Representation.DEFAULT);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("patient");
		description.addRequiredProperty("encounter");
		description.addProperty("orders");
		description.addProperty("orderSet");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("orders");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl.property("uuid", new StringProperty()).property("display", new StringProperty())
			        .property("voided", new BooleanProperty())
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGetRef"))
			        .property("orders", new RefProperty("#/definitions/OrderGetRef"))
			        .property("orderSet", new RefProperty("#/definitions/OrdersetGetRef"));
			
		} else if (rep instanceof FullRepresentation) {
			modelImpl.property("uuid", new StringProperty()).property("display", new StringProperty())
			        .property("voided", new BooleanProperty()).property("auditInfo", new BooleanProperty())
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGetRef"))
			        .property("orders", new ArrayProperty(new RefProperty("#/definitions/OrderGetRef")))
			        .property("orderSet", new RefProperty("#/definitions/OrdersetGetRef"));
		} else if (rep instanceof RefRepresentation) {
			modelImpl.property("display", new StringProperty()).property("uuid", new StringProperty());
			
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		return new ModelImpl().property("patient", new StringProperty().example("uuid"))
		        .property("encounter", new StringProperty().example("uuid"))
		        .property("orders", new ArrayProperty(new RefProperty("#/definitions/OrderCreate")))
		        .property("orderSet", new StringProperty().example("uuid"));
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl().property("orders",
		    new ArrayProperty(new RefProperty("#/definitions/OrderCreate")));
	}
	
	@PropertyGetter("display")
	public String getDisplayString(OrderGroup group) {
		if (group.getOrders() == null)
			return "[No Orders]";
		Patient patient = group.getPatient();
		return patient.getPatientIdentifier().getIdentifier() + " - " + patient.getPersonName().getFullName() + " - "
		        + group.getOrderSet().getName();
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
}
