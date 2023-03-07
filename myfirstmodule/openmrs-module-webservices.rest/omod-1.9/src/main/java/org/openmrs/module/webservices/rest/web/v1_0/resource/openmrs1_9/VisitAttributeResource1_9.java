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

import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Resource} for VisitAttributes, supporting standard CRUD operations
 */
@SubResource(parent = VisitResource1_9.class, path = "attribute", supportedClass = VisitAttribute.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class VisitAttributeResource1_9 extends BaseAttributeCrudResource1_9<VisitAttribute, Visit, VisitResource1_9> {
	
	/**
	 * Sets attributes on the given visit.
	 * 
	 * @param instance
	 * @param attr
	 */
	@PropertySetter("attributeType")
	public static void setAttributeType(VisitAttribute instance, VisitAttributeType attr) {
		instance.setAttributeType(attr);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Visit getParent(VisitAttribute instance) {
		return instance.getVisit();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public VisitAttribute newDelegate() {
		return new VisitAttribute();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(VisitAttribute instance, Visit visit) {
		instance.setVisit(visit);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public VisitAttribute getByUniqueId(String uniqueId) {
		return Context.getVisitService().getVisitAttributeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<VisitAttribute> doGetAll(Visit parent, RequestContext context) throws ResponseException {
		if (context.getIncludeAll()) {
			List<VisitAttribute> attrs = new ArrayList<VisitAttribute>();
			for (VisitAttribute visitAttribute : parent.getAttributes()) {
				attrs.add(visitAttribute);
			}
			return new NeedsPaging<VisitAttribute>(attrs, context);
		}
		return new NeedsPaging<VisitAttribute>((List<VisitAttribute>) parent.getActiveAttributes(), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public VisitAttribute save(VisitAttribute delegate) {
		if (delegate.getAttributeType().getMaxOccurs() != null && delegate.getAttributeType().getMaxOccurs() == 1) {
			// there is a convencience method for this case, that avoids the client having to make two calls (to void and create)
			delegate.getVisit().setAttribute(delegate);
		} else {
			// make sure it has not already been added to the visit
			boolean needToAdd = true;
			for (VisitAttribute pa : delegate.getVisit().getActiveAttributes()) {
				if (pa.equals(delegate)) {
					needToAdd = false;
					break;
				}
			}
			if (needToAdd) {
				delegate.getVisit().addAttribute(delegate);
			}
		}
		Context.getVisitService().saveVisit(delegate.getVisit());
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(VisitAttribute delegate, String reason, RequestContext context) throws ResponseException {
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getVisitService().saveVisit(delegate.getVisit());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(VisitAttribute delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Cannot purge VisitAttribute");
	}
}
