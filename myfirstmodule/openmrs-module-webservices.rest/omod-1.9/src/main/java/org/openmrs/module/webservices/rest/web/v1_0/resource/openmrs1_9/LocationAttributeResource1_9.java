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

import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * {@link Resource} for LocationAttributes, supporting standard CRUD operations
 */
@SubResource(parent = LocationResource1_9.class, path = "attribute", supportedClass = LocationAttribute.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class LocationAttributeResource1_9 extends BaseAttributeCrudResource1_9<LocationAttribute, Location, LocationResource1_9> {
	
	/**
	 * Sets attributeType on the given LocationAttribute.
	 * 
	 * @param instance
	 * @param attr
	 */
	@PropertySetter("attributeType")
	public static void setAttributeType(LocationAttribute instance, LocationAttributeType attr) {
		instance.setAttributeType(attr);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Location getParent(LocationAttribute instance) {
		return instance.getLocation();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public LocationAttribute newDelegate() {
		return new LocationAttribute();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(LocationAttribute instance, Location location) {
		instance.setLocation(location);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public LocationAttribute getByUniqueId(String uniqueId) {
		return Context.getLocationService().getLocationAttributeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<LocationAttribute> doGetAll(Location parent, RequestContext context) throws ResponseException {
		return new NeedsPaging<LocationAttribute>((List<LocationAttribute>) parent.getActiveAttributes(), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public LocationAttribute save(LocationAttribute delegate) {
		// make sure it has not already been added to the location
		boolean needToAdd = true;
		for (LocationAttribute pa : delegate.getLocation().getActiveAttributes()) {
			if (pa.equals(delegate)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd) {
			delegate.getLocation().addAttribute(delegate);
		}
		Context.getLocationService().saveLocation(delegate.getLocation());
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(LocationAttribute delegate, String reason, RequestContext context) throws ResponseException {
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getLocationService().saveLocation(delegate.getLocation());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(LocationAttribute delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Cannot purge LocationAttribute");
	}
}
