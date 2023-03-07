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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.OrderResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for {@link org.openmrs.Order}
 * , supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/order", supportedClass = Order.class, supportedOpenmrsVersions = { "1.10.* - 2.1.*" })
public class OrderResource1_10 extends OrderResource1_8 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("orderNumber");
			description.addProperty("accessionNumber");
			description.addProperty("patient", Representation.REF);
			description.addProperty("concept", Representation.REF);
			description.addProperty("action");
			description.addProperty("careSetting", Representation.REF);
			description.addProperty("previousOrder", Representation.REF);
			description.addProperty("dateActivated");
			description.addProperty("scheduledDate");
			description.addProperty("dateStopped");
			description.addProperty("autoExpireDate");
			description.addProperty("encounter", Representation.REF);
			description.addProperty("orderer", Representation.REF);
			description.addProperty("orderReason", Representation.REF);
			description.addProperty("orderReasonNonCoded");
			description.addProperty("orderType");
			description.addProperty("urgency");
			description.addProperty("instructions");
			description.addProperty("commentToFulfiller");
			description.addProperty("display");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("orderNumber");
			description.addProperty("accessionNumber");
			description.addProperty("patient", Representation.REF);
			description.addProperty("concept", Representation.REF);
			description.addProperty("action");
			description.addProperty("careSetting", Representation.DEFAULT);
			description.addProperty("previousOrder", Representation.REF);
			description.addProperty("dateActivated");
			description.addProperty("scheduledDate");
			description.addProperty("dateStopped");
			description.addProperty("autoExpireDate");
			description.addProperty("encounter", Representation.REF);
			description.addProperty("orderer", Representation.REF);
			description.addProperty("orderReason", Representation.REF);
			description.addProperty("orderReasonNonCoded");
			description.addProperty("orderType");
			description.addProperty("urgency");
			description.addProperty("instructions");
			description.addProperty("commentToFulfiller");
			description.addProperty("display");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		} else {
			return null;
		}
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("encounter", new StringProperty().example("uuid"))
		        .property("orderType", new StringProperty().example("uuid"))
		        .property("action", new EnumProperty(Order.Action.class))
		        .property("accessionNumber", new StringProperty())
		        .property("dateActivated", new DateProperty())
		        .property("scheduledDate", new DateProperty())
		        .property("patient", new StringProperty().example("uuid"))
		        .property("concept", new StringProperty().example("uuid"))
		        .property("careSetting", new StringProperty().example("uuid"))
		        .property("dateStopped", new DateProperty())
		        .property("autoExpireDate", new DateProperty())
		        .property("orderer", new StringProperty().example("uuid"))
		        .property("previousOrder", new StringProperty().example("uuid"))
		        .property("urgency", new EnumProperty(Order.Urgency.class))
		        .property("orderReason", new StringProperty().example("uuid"))
		        .property("orderReasonNonCoded", new StringProperty())
		        .property("instructions", new StringProperty())
		        .property("commentToFulfiller", new StringProperty())
		        
		        .required("orderType").required("patient").required("concept");
		if (rep instanceof FullRepresentation) {
			model
			        .property("encounter", new RefProperty("#/definitions/EncounterCreate"))
			        .property("patient", new RefProperty("#/definitions/PatientCreate"))
			        .property("concept", new RefProperty("#/definitions/ConceptCreate"))
			        .property("orderer", new RefProperty("#/definitions/UserCreate"))
			        .property("previousOrder", new RefProperty("#/definitions/OrderCreate"))
			        .property("orderReason", new RefProperty("#/definitions/ConceptCreate"));
		}
		//FIXME missing prop: type
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("encounter");
		d.addProperty("action");
		d.addProperty("accessionNumber");
		d.addProperty("dateActivated");
		d.addProperty("scheduledDate");
		d.addProperty("patient");
		d.addProperty("concept");
		d.addProperty("careSetting");
		d.addProperty("dateStopped");
		d.addProperty("autoExpireDate");
		d.addProperty("orderer");
		d.addProperty("previousOrder");
		d.addProperty("urgency");
		d.addProperty("orderType");
		d.addProperty("orderReason");
		d.addProperty("orderReasonNonCoded");
		d.addProperty("instructions");
		d.addProperty("commentToFulfiller");
		return d;
	}
	
	/**
	 * Fetches an order by uuid or order number
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public Order getByUniqueId(String uniqueId) {
		Order order = super.getByUniqueId(uniqueId);
		if (order == null) {
			order = Context.getOrderService().getOrderByOrderNumber(uniqueId);
		}
		return order;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * Gets orders by given patient (paged according to context if necessary) only if a patient
	 * parameter exists in the request set on the {@link RequestContext}, optional careSetting,
	 * orderType, asOfDate request parameters can be specified to filter on
	 * 
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 * @return all orders for a given patient (possibly filtered by context.type)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			if (patient == null) {
				throw new ObjectNotFoundException();
			}
			
			// if the user indicated a specific type, try to delegate to the appropriate subclass handler
			if (context.getType() != null) {
				PageableResult ret = (PageableResult) findAndInvokeSubclassHandlerMethod(context.getType(),
				    "getActiveOrders", patient, context);
				if (ret != null) {
					return ret;
				}
			}
			
			String careSettingUuid = context.getRequest().getParameter("careSetting");
			String asOfDateString = context.getRequest().getParameter("asOfDate");
			String orderTypeUuid = context.getRequest().getParameter("orderType");
			String sortParam = context.getRequest().getParameter("sort");
			
			CareSetting careSetting = null;
			Date asOfDate = null;
			OrderType orderType = null;
			if (StringUtils.isNotBlank(asOfDateString)) {
				asOfDate = (Date) ConversionUtil.convert(asOfDateString, Date.class);
			}
			if (StringUtils.isNotBlank(careSettingUuid)) {
				careSetting = ((CareSettingResource1_10) Context.getService(RestService.class).getResourceBySupportedClass(
				    CareSetting.class)).getByUniqueId(careSettingUuid);
				if (careSetting == null) {
					throw new ObjectNotFoundException();
				}
			}
			if (StringUtils.isNotBlank(orderTypeUuid)) {
				orderType = ((OrderTypeResource1_10) Context.getService(RestService.class).getResourceBySupportedClass(
				    OrderType.class)).getByUniqueId(orderTypeUuid);
				if (orderType == null) {
					throw new ObjectNotFoundException();
				}
			}
			
			String status = context.getRequest().getParameter("status");
			List<Order> orders = OrderUtil.getOrders(patient, careSetting, orderType, status, asOfDate,
			    context.getIncludeAll());
			// if the user indicated a specific type, and we couldn't delegate to a subclass handler above, filter here
			if (context.getType() != null) {
				filterByType(orders, context.getType());
			}
			
			if (StringUtils.isNotBlank(sortParam)) {
				List<Order> sortedOrder = sortOrdersBasedOnDateActivatedOrDateStopped(orders, sortParam, status);
				return new NeedsPaging<Order>(sortedOrder, context);
			}
			else {
				return new NeedsPaging<Order>(orders, context);
			}
		}
		
		return new EmptySearchResult();
	}
	
	private static Date getUsableDate(Order order) {
		return order.getDateStopped() != null ? order.getDateStopped() : (order.getAutoExpireDate() != null ? order
		        .getAutoExpireDate() : order.getDateCreated());
	}
	
	private static Date getActiveOrderSortDate(Order order) {
		return order.getDateActivated() != null ? order.getDateActivated() : order.getDateCreated();
	}
	
	public List<Order> sortOrdersBasedOnDateActivatedOrDateStopped(List<Order> orders, final String sortOrder,
	        final String status) {
		List<Order> sortedList = new ArrayList<Order>(orders);
		Collections.sort(sortedList, new Comparator<Order>() {
			
			@Override
			public int compare(Order o1, Order o2) {
				if (status.equalsIgnoreCase("inactive")) {
					return sortOrder.equalsIgnoreCase("ASC") ?
					        getUsableDate(o1).compareTo(getUsableDate(o2)) : getUsableDate(o2).compareTo(getUsableDate(o1));
				}
				else {
					
					return sortOrder.equalsIgnoreCase("asc") ? getActiveOrderSortDate(o1).compareTo(
					    getActiveOrderSortDate(o2)) : getActiveOrderSortDate(o2).compareTo(getActiveOrderSortDate(o1));
				}
			}
		});
		return sortedList;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_10.RESOURCE_VERSION;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public Order save(Order delegate) {
		return Context.getOrderService().saveOrder(delegate, null);
	}
}
