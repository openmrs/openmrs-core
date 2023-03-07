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
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for Privilege, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/privilege", supportedClass = Privilege.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class PrivilegeResource1_8 extends MetadataDelegatingCrudResource<Privilege> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Privilege getByUniqueId(String uniqueId) {
		Privilege privilege = Context.getUserService().getPrivilegeByUuid(uniqueId);
		if (privilege == null)
			privilege = Context.getUserService().getPrivilege(uniqueId);
		return privilege;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Privilege newDelegate() {
		return new Privilege();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#save(java.lang.Object)
	 */
	@Override
	public Privilege save(Privilege delegate) {
		return Context.getUserService().savePrivilege(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Privilege delegate, RequestContext context) throws ResponseException {
		if (delegate == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getUserService().purgePrivilege(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		// you aren't allowed to edit the name of an existing privilege, since that is its PK.
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("description");
		
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("description", new StringProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		
		description.addProperty("description");
		
		return description;
	}
	
	/**
	 * @param delegate
	 * @return
	 */
	@PropertyGetter("name")
	public static String getPrivilegeName(Privilege delegate) {
		return delegate.getPrivilege();
	}
	
	/**
	 * @param delegate
	 * @param name
	 */
	@PropertySetter("name")
	public static void setPrivilegeName(Privilege delegate, String name) {
		delegate.setPrivilege(name);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#getDisplayString(org.openmrs.OpenmrsMetadata)
	 */
	@Override
	@PropertyGetter("display")
	public String getDisplayString(Privilege delegate) {
		// TODO figure out how to delegate to the superclass method to handler message-based i18n
		String ret = getPrivilegeName(delegate);
		return StringUtils.isNotBlank(ret) ? ret : "[No Name]";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Privilege> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Privilege>(Context.getUserService().getAllPrivileges(), context);
	}
	
}
