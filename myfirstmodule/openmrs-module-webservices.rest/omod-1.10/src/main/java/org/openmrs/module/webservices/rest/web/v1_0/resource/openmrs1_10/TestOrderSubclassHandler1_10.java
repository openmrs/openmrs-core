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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.SubClassHandler;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * Exposes the {@link org.openmrs.TestOrder} subclass as a type in
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.OrderResource1_8}
 */
@SubClassHandler(supportedClass = TestOrder.class, supportedOpenmrsVersions = { "1.10.* - 9.*" })
public class TestOrderSubclassHandler1_10 extends BaseDelegatingSubclassHandler<Order, TestOrder> implements DelegatingSubclassHandler<Order, TestOrder> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return "testorder";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#newDelegate()
	 */
	@Override
	public TestOrder newDelegate() {
		return new TestOrder();
	}
	
	/**
	 * @see DelegatingSubclassHandler#getAllByType(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public PageableResult getAllByType(RequestContext context) throws ResourceDoesNotSupportOperationException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
			        .getResourceBySupportedClass(Order.class);
			DelegatingResourceDescription d = orderResource.getRepresentationDescription(rep);
			d.addProperty("specimenSource", Representation.REF);
			d.addProperty("laterality");
			d.addProperty("clinicalHistory");
			d.addProperty("frequency", Representation.REF);
			d.addProperty("numberOfRepeats");
			return d;
		} else if (rep instanceof FullRepresentation) {
			OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
			        .getResourceBySupportedClass(Order.class);
			DelegatingResourceDescription d = orderResource.getRepresentationDescription(rep);
			d.addProperty("specimenSource", Representation.REF);
			d.addProperty("laterality");
			d.addProperty("clinicalHistory");
			d.addProperty("frequency", Representation.DEFAULT);
			d.addProperty("numberOfRepeats");
			return d;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		DelegatingResourceDescription d = orderResource.getCreatableProperties();
		d.addProperty("specimenSource");
		d.addProperty("laterality");
		d.addProperty("clinicalHistory");
		d.addProperty("frequency");
		d.addProperty("numberOfRepeats");
		return d;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		//this actually throws a ResourceDoesNotSupportOperationException
		return orderResource.getUpdatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		ModelImpl orderModel = (ModelImpl) orderResource.getGETModel(rep);
		orderModel
		        .property("laterality", new EnumProperty(TestOrder.Laterality.class))
		        .property("clinicalHistory", new StringProperty())
		        .property("numberOfRepeats", new IntegerProperty());
		
		if (rep instanceof DefaultRepresentation) {
			orderModel
			        .property("specimenSource", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("frequency", new RefProperty("#/definitions/OrderfrequencyGetRef"));
		} else if (rep instanceof FullRepresentation) {
			orderModel
			        .property("specimenSource", new RefProperty("#/definitions/ConceptGet"))
			        .property("frequency", new RefProperty("#/definitions/OrderfrequencyGet"));
		}
		return orderModel;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		ModelImpl orderModel = (ModelImpl) orderResource.getCREATEModel(rep);
		return orderModel
		        .property("specimenSource", new StringProperty().example("uuid"))
		        .property("laterality", new EnumProperty(TestOrder.Laterality.class))
		        .property("clinicalHistory", new StringProperty())
		        .property("frequency", new StringProperty().example("uuid"))
		        .property("numberOfRepeats", new IntegerProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		return orderResource.getUPDATEModel(rep);
	}
	
	public PageableResult getActiveOrders(Patient patient, RequestContext context) {
		String careSettingUuid = context.getRequest().getParameter("careSetting");
		String asOfDateString = context.getRequest().getParameter("asOfDate");
		String sortParam = context.getRequest().getParameter("sort");
		CareSetting careSetting = null;
		java.util.Date asOfDate = null;
		if (StringUtils.isNotBlank(asOfDateString)) {
			asOfDate = (java.util.Date) ConversionUtil.convert(asOfDateString, java.util.Date.class);
		}
		if (StringUtils.isNotBlank(careSettingUuid)) {
			careSetting = ((CareSettingResource1_10) Context.getService(RestService.class).getResourceBySupportedClass(
			    CareSetting.class)).getByUniqueId(careSettingUuid);
		}
		
		String status = context.getRequest().getParameter("status");
		OrderService os = Context.getOrderService();
		OrderType orderType = os.getOrderTypeByName("Test order");
		List<Order> testOrders = OrderUtil.getOrders(patient, careSetting, orderType, status, asOfDate,
		    context.getIncludeAll());
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		
		if (StringUtils.isNotBlank(sortParam)) {
			List<Order> sortedOrder = orderResource.sortOrdersBasedOnDateActivatedOrDateStopped(testOrders, sortParam,
			    status);
			return new NeedsPaging<Order>(sortedOrder, context);
		}
		else {
			return new NeedsPaging<Order>(testOrders, context);
		}
	}
	
	/**
	 * Gets a user-friendly display representation of the delegate
	 * 
	 * @param delegate
	 * @return
	 */
	@PropertyGetter("display")
	public static String getDisplay(TestOrder delegate) {
		OrderResource1_10 orderResource = (OrderResource1_10) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Order.class);
		return orderResource.getDisplayString(delegate);
	}
}
