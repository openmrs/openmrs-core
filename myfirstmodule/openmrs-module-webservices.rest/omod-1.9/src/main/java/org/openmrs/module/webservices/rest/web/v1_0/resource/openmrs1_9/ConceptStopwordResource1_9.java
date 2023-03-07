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
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.*;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.ConceptStopWord;

import java.util.List;

/**
 * {@link Resource} for {@link ConceptStopWord}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptstopword", supportedClass = ConceptStopWord.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ConceptStopwordResource1_9 extends DelegatingCrudResource<ConceptStopWord> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("value");
			description.addProperty("locale");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("value");
			description.addProperty("locale");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@PropertyGetter("display")
	public String getDisplayString(ConceptStopWord delegate) {
		return StringUtils.isEmpty(delegate.getValue()) ? "" : delegate.getValue();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("value");
		description.addProperty("locale");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty());
		
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("value", new StringProperty())
			        .property("locale", new StringProperty().example("en")); //FIXME type
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("value", new StringProperty())
		        .property("locale", new StringProperty().example("en"))
		        
		        .required("value");
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptStopWord getByUniqueId(String uniqueId) {
		List<ConceptStopWord> datatypes = Context.getConceptService().getAllConceptStopWords();
		for (ConceptStopWord datatype : datatypes) {
			if (datatype.getUuid().equals(uniqueId)) {
				return datatype;
			}
		}
		return null;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptStopWord newDelegate() {
		return new ConceptStopWord();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptStopWord save(ConceptStopWord delegate) {
		return Context.getConceptService().saveConceptStopWord(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptStopWord delegate, RequestContext context) throws ResponseException {
		Context.getConceptService().deleteConceptStopWord(delegate.getId());
	}
	
	@Override
	protected void delete(ConceptStopWord delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		
		List<ConceptStopWord> conceptStopWords = Context.getConceptService().getAllConceptStopWords();
		return new NeedsPaging<ConceptStopWord>(conceptStopWords, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_9.RESOURCE_VERSION;
	}
}
