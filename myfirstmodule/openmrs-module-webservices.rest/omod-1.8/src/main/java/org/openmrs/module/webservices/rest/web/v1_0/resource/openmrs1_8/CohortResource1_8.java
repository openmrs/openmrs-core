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

import java.util.List;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for Cohorts, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/cohort", supportedClass = Cohort.class, supportedOpenmrsVersions = { "1.8.* - 2.0.*" })
public class CohortResource1_8 extends DataDelegatingCrudResource<Cohort> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(Cohort cohort, String reason, RequestContext context) throws ResponseException {
		if (cohort.isVoided()) {
			// http operation DELETE is idempotent, so we return success here
			return;
		}
		Context.getCohortService().voidCohort(cohort, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Cohort getByUniqueId(String param) {
		Cohort result = Context.getCohortService().getCohortByUuid(param);
		if (result == null)
			return Context.getCohortService().getCohort(param);
		return result;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("voided");
			description.addProperty("memberIds", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("memberIds");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep));
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("name", new StringProperty())
			        .property("description", new StringProperty())
			        .property("voided", new StringProperty())
			        .property("memberIds", new ArrayProperty(new IntegerProperty())); //FIXME
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property("memberIds", new ArrayProperty(new IntegerProperty())) //FIXME
		        .required("name").required("description").required("memberIds");
	}
	
	@Override
	public Model getUPDATEModel(Representation representation) {
		return new ModelImpl()
		        .property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .required("name").required("description");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addRequiredProperty("memberIds");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Cohort newDelegate() {
		return new Cohort();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Cohort cohort, RequestContext context) throws ResponseException {
		if (cohort == null) {
			// http operation DELETE is idempotent, so we return success here
			return;
		}
		Context.getCohortService().purgeCohort(cohort);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Cohort save(Cohort cohort) {
		return Context.getCohortService().saveCohort(cohort);
	}
	
	/**
	 * @param cohort
	 * @return cohort's name
	 */
	@PropertyGetter("display")
	public String getDisplayString(Cohort cohort) {
		return cohort.getName();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Cohort> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Cohort>(Context.getCohortService().getAllCohorts(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected NeedsPaging<Cohort> doSearch(RequestContext context) {
		List<Cohort> cohorts = Context.getCohortService().getCohorts(context.getParameter("q"));
		return new NeedsPaging<Cohort>(cohorts, context);
	}
	
}
