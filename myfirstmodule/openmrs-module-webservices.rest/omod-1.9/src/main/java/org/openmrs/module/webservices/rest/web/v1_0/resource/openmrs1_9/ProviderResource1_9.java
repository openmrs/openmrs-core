/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import java.util.Set;

/**
 * {@link Resource} for Provider, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/provider", supportedClass = Provider.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ProviderResource1_9 extends MetadataDelegatingCrudResource<Provider> {
	
	public ProviderResource1_9() {
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
			description.addProperty("person", Representation.REF);
			description.addProperty("identifier");
			description.addProperty("attributes", "activeAttributes", Representation.REF);
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("person", Representation.DEFAULT);
			description.addProperty("identifier");
			description.addProperty("attributes", "activeAttributes", Representation.DEFAULT);
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
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
		description.addRequiredProperty("person");
		description.addRequiredProperty("identifier");
		description.addProperty("attributes");
		description.addProperty("retired");
		return description;
	}
	
	/**
	 * Sets the attributes of a Provider
	 * 
	 * @param provider whose attributes to be set
	 * @param attributes the attributes to be set
	 */
	@PropertySetter("attributes")
	public static void setAttributes(Provider provider, Set<ProviderAttribute> attributes) {
		for (ProviderAttribute attribute : attributes) {
			attribute.setOwner(provider);
		}
		provider.setAttributes(attributes);
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
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGetRef"))
			        .property("identifier", new StringProperty())
			        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/ProviderAttributeGetRef")))
			        .property("preferredHandlerClassname", new StringProperty());
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGet"))
			        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/ProviderAttributeGet")));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getCREATEModel(rep))
		        .property("person", new StringProperty().example("uuid"))
		        .property("identifier", new StringProperty())
		        .property("attributes", new ArrayProperty(new RefProperty("#/definitions/ProviderAttributeCreate")))
		        .property("retired", new BooleanProperty())
		        
		        .required("person").required("identifier");
		if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonCreate"));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Provider newDelegate() {
		return new Provider();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Provider save(Provider provider) {
		return Context.getProviderService().saveProvider(provider);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Provider getByUniqueId(String uuid) {
		return Context.getProviderService().getProviderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(Provider provider, String reason, RequestContext context) throws ResponseException {
		if (provider.isRetired()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getProviderService().retireProvider(provider, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Provider provider, RequestContext context) throws ResponseException {
		if (provider == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getProviderService().purgeProvider(provider);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String query = context.getParameter("q");
		if (query == null) {
			return new EmptySearchResult();
		}
		
		List<Provider> providers = Context.getProviderService().getProviders(query, context.getStartIndex(),
		    context.getLimit(), null, context.getIncludeAll());
		
		int count = Context.getProviderService().getCountOfProviders(query, context.getIncludeAll());
		boolean hasMore = count > context.getStartIndex() + context.getLimit();
		
		return new AlreadyPaged<Provider>(context, providers, hasMore, Long.valueOf(count));
		
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(java.lang.String,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Provider> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Provider>(Context.getProviderService()
		        .getAllProviders(context.getIncludeAll()), context);
	}
	
	/**
	 * @param provider
	 * @return identifier + name (for concise display purposes)
	 */
	@Override
	@PropertyGetter("display")
	public String getDisplayString(Provider provider) {
		if (provider.getIdentifier() == null) {
			return provider.getName();
		}
		return provider.getIdentifier() + " - " + provider.getName();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
}
