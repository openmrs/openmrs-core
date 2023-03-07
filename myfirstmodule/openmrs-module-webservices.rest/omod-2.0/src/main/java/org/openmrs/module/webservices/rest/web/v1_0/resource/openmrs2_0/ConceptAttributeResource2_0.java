/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;

import java.util.List;

/**
 * {@link Resource} for ConceptAttributes, supporting standard CRUD operations
 */
@SubResource(parent = ConceptResource2_0.class, path = "attribute", supportedClass = ConceptAttribute.class, supportedOpenmrsVersions = {
        "2.0.* - 9.*" })
public class ConceptAttributeResource2_0 extends BaseAttributeCrudResource1_9<ConceptAttribute, Concept, ConceptResource2_0> {
	
	/**
	 * Sets attributeType on the given ConceptAttribute.
	 * 
	 * @param instance
	 * @param attr
	 */
	@PropertySetter("attributeType")
	public static void setAttributeType(ConceptAttribute instance, ConceptAttributeType attr) {
		instance.setAttributeType(attr);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(Object)
	 */
	@Override
	public Concept getParent(ConceptAttribute instance) {
		return instance.getConcept();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ConceptAttribute newDelegate() {
		return new ConceptAttribute();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(Object,
	 *      Object)
	 */
	@Override
	public void setParent(ConceptAttribute instance, Concept concept) {
		instance.setConcept(concept);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ConceptAttribute getByUniqueId(String uniqueId) {
		return Context.getConceptService().getConceptAttributeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(Object,
	 *      RequestContext)
	 */
	@Override
	public NeedsPaging<ConceptAttribute> doGetAll(Concept parent, RequestContext context) throws ResponseException {
		return new NeedsPaging<ConceptAttribute>((List<ConceptAttribute>) parent.getActiveAttributes(), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(Object)
	 */
	@Override
	public ConceptAttribute save(ConceptAttribute delegate) {
		// make sure it has not already been added to the concept
		boolean needToAdd = true;
		for (ConceptAttribute pa : delegate.getConcept().getActiveAttributes()) {
			if (pa.equals(delegate)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd) {
			delegate.getConcept().addAttribute(delegate);
		}
		Context.getConceptService().saveConcept(delegate.getConcept());
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(Object,
	 *      String, RequestContext)
	 */
	@Override
	protected void delete(ConceptAttribute delegate, String reason, RequestContext context) throws ResponseException {
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getConceptService().saveConcept(delegate.getConcept());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(Object,
	 *      RequestContext)
	 */
	@Override
	public void purge(ConceptAttribute delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Cannot purge ConceptAttribute");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "2.0";
	}
	
}
