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
import io.swagger.models.properties.MapProperty;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper1_8;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for ServerLogController, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/serverlog", supportedClass = ServerLogActionWrapper.class, supportedOpenmrsVersions = {
        "1.8.* - 2.3.*" })
public class ServerLogResource1_8 extends BaseDelegatingResource<ServerLogActionWrapper> implements Listable {
	
	private ServerLogActionWrapper serverLogActionWrapper = new ServerLogActionWrapper1_8();
	
	public void setServerLogActionWrapper(ServerLogActionWrapper serverLogActionWrapper) {
		this.serverLogActionWrapper = serverLogActionWrapper;
	}
	
	@Override
	public SimpleObject getAll(RequestContext context) throws ResponseException {
		SimpleObject rest = new SimpleObject();
		rest.put("serverLog", serverLogActionWrapper.getServerLogs());
		return rest;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("serverLog", new MapProperty());
	}
	
	@Override
	public ServerLogActionWrapper getByUniqueId(String uniqueId) {
		throw new UnsupportedOperationException("Serverlog doesn't support to this action");
	}
	
	@Override
	protected void delete(ServerLogActionWrapper delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Serverlog doesn't support to this action");
	}
	
	@Override
	public ServerLogActionWrapper newDelegate() {
		return new ServerLogActionWrapper1_8();
	}
	
	@Override
	public ServerLogActionWrapper save(ServerLogActionWrapper delegate) {
		throw new UnsupportedOperationException("Serverlog doesn't support to this action");
	}
	
	@Override
	public void purge(ServerLogActionWrapper delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Serverlog doesn't support to this action");
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("serverLog", "serverLog");
		return description;
	}
}
