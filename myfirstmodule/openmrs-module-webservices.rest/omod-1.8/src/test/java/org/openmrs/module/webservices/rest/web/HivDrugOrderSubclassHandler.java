/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubClassHandler;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.util.OpenmrsConstants;

/**
 * This is a contrived example for testing purposes
 */
@SubClassHandler(supportedClass = HivDrugOrder.class, supportedOpenmrsVersions = { "1.8.*" })
public class HivDrugOrderSubclassHandler extends BaseDelegatingSubclassHandler<Order, HivDrugOrder> implements DelegatingSubclassHandler<Order, HivDrugOrder> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#newDelegate()
	 */
	@Override
	public HivDrugOrder newDelegate() {
		HivDrugOrder o = new HivDrugOrder();
		o.setOrderType(Context.getOrderService().getOrderType(OpenmrsConstants.ORDERTYPE_DRUG));
		return o;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription d = new DelegatingResourceDescription();
			d.addProperty("patient", Representation.REF);
			d.addProperty("concept", Representation.REF);
			d.addProperty("startDate");
			d.addProperty("autoExpireDate");
			d.addProperty("standardRegimenCode");
			return d;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription d = new DelegatingResourceDescription();
			d.addProperty("patient");
			d.addProperty("concept");
			d.addProperty("startDate");
			d.addProperty("autoExpireDate");
			d.addProperty("standardRegimenCode");
			d.addProperty("instructions");
			return d;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("patient");
		d.addRequiredProperty("concept");
		d.addProperty("startDate");
		d.addProperty("autoExpireDate");
		d.addProperty("standardRegimenCode");
		d.addProperty("instructions");
		return d;
		
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("startDate", new DateProperty())
			        .property("autoExpireDate", new DateProperty())
			        .property("standardRegimenCode", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("patient", new RefProperty("#/definitions/PatientGet"))
			        .property("concept", new RefProperty("#/definitions/ConceptGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("patient", new StringProperty().example("uuid"))
		        .property("concept", new StringProperty().example("uuid"))
		        .property("startDate", new DateProperty())
		        .property("autoExpireDate", new DateProperty())
		        .property("standardRegimenCode", new StringProperty())
		        .property("instructions", new StringProperty())
		        
		        .required("patient").required("concept");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getAllByType(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public PageableResult getAllByType(RequestContext context) throws ResourceDoesNotSupportOperationException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return "hivdrugorder";
	}
	
	@PropertyGetter("standardRegimenCode")
	public static String getStandardRegimenCode(HivDrugOrder delegate) {
		return delegate.getInstructions().substring("Standard Regimen: ".length());
	}
	
	@PropertySetter("standardRegimenCode")
	public static void setStandardRegimenCode(HivDrugOrder delegate, String code) {
		delegate.setInstructions("Standard Regimen: " + code);
	}
	
}
