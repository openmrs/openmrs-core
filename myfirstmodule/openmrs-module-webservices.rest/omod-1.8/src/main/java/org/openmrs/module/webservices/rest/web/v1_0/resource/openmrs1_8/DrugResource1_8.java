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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * {@link Resource} for {@link Drug}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/drug", supportedClass = Drug.class, supportedOpenmrsVersions = { "1.8.* - 1.9.*" })
public class DrugResource1_8 extends MetadataDelegatingCrudResource<Drug> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Drug getByUniqueId(String uniqueId) {
		return Context.getConceptService().getDrugByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Drug newDelegate() {
		return new Drug();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public Drug save(Drug delegate) {
		return Context.getConceptService().saveDrug(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Drug delegate, RequestContext context) throws ResponseException {
		if (delegate == null)
			return;
		Context.getConceptService().purgeDrug(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("dosageForm", Representation.REF);
			description.addProperty("doseStrength");
			description.addProperty("maximumDailyDose");
			description.addProperty("minimumDailyDose");
			description.addProperty("units");
			description.addProperty("concept", Representation.REF);
			description.addProperty("combination");
			description.addProperty("route", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("dosageForm", Representation.REF);
			description.addProperty("doseStrength");
			description.addProperty("maximumDailyDose");
			description.addProperty("minimumDailyDose");
			description.addProperty("units");
			description.addProperty("concept", Representation.REF);
			description.addProperty("combination");
			description.addProperty("route", Representation.REF);
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		//Let the superclass handle this
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("doseStrength", new DoubleProperty())
			        .property("maximumDailyDose", new DoubleProperty())
			        .property("minimumDailyDose", new DoubleProperty())
			        .property("units", new StringProperty())
			        .property("combination", new BooleanProperty()._default(false));
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("dosageForm", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("route", new RefProperty("#/definitions/ConceptGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("dosageForm", new RefProperty("#/definitions/ConceptGet"))
			        .property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("route", new RefProperty("#/definitions/ConceptGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getCREATEModel(rep))
		        .property("combination", new BooleanProperty()._default(false))
		        .property("concept", new StringProperty())
		        .property("doseStrength", new DoubleProperty())
		        .property("maximumDailyDose", new DoubleProperty())
		        .property("minimumDailyDose", new DoubleProperty())
		        .property("units", new StringProperty())
		        .property("dosageForm", new StringProperty())
		        .property("route", new StringProperty())
		        
		        .required("combination").required("concept");
		if (rep instanceof FullRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptCreate"))
			        .property("dosageForm", new RefProperty("#/definitions/ConceptCreate"))
			        .property("route", new RefProperty("#/definitions/ConceptCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep); //FIXME no updatableProperties()
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("combination");
		description.addRequiredProperty("concept");
		
		description.addProperty("name");
		description.addProperty("doseStrength");
		description.addProperty("maximumDailyDose");
		description.addProperty("minimumDailyDose");
		description.addProperty("units");
		description.addProperty("dosageForm");
		description.addProperty("route");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Drug> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Drug>(Context.getConceptService().getAllDrugs(context.getIncludeAll()), context);
	}
	
	/**
	 * Drug searches support the following query parameters:
	 * <ul>
	 * <li>q=(name): searches drug with name containing the query string</li>
	 * </ul>
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext ctx) {
		boolean searchOnPhrase = true;
		boolean searchDrugConceptNames = false;
		boolean includeRetired = ctx.getIncludeAll();
		Integer startIndex = ctx.getStartIndex();
		Integer limit = ctx.getLimit();
		String drugName = ctx.getParameter("q");
		
		Integer countOfDrugs = Context.getConceptService().getCountOfDrugs(drugName, null, searchOnPhrase,
		    searchDrugConceptNames, includeRetired);
		List<Drug> drugs = Context.getConceptService().getDrugs(drugName, null, searchOnPhrase, searchDrugConceptNames,
		    includeRetired, startIndex, limit);
		boolean hasMore = countOfDrugs > startIndex + limit;
		return new AlreadyPaged<Drug>(ctx, drugs, hasMore, Long.valueOf(countOfDrugs));
	}
}
