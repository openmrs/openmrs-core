/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs1_8;

import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.UserResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;

/**
 * An implementation of Converter to be able to create a representation from a User when User is
 * used in another resource. Currently UserAndPassword doesn't convert User
 */
@Handler(supports = User.class, order = 0)
public class UserConverter1_8 implements Converter<User> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#getByUniqueId(java.lang.String)
	 */
	@Override
	public User getByUniqueId(String string) {
		return Context.getUserService().getUserByUuid(string);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
	 */
	@Override
	public User newInstance(String type) {
		return new User();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#asRepresentation(T,
	 *      org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public SimpleObject asRepresentation(User instance, Representation rep) throws ConversionException {
		UserAndPassword1_8 userPass = new UserAndPassword1_8(instance);
		UserResource1_8 userResource = (UserResource1_8) Context.getService(RestService.class).getResourceByName(
		    RestConstants.VERSION_1 + "/user");
		return userResource.asRepresentation(userPass, rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#getProperty(T,
	 *      java.lang.String)
	 */
	@Override
	public Object getProperty(User instance, String propertyName) throws ConversionException {
		UserAndPassword1_8 userPass = new UserAndPassword1_8(instance);
		UserResource1_8 userResource = (UserResource1_8) Context.getService(RestService.class).getResourceByName(
		    RestConstants.VERSION_1 + "/user");
		return userResource.getProperty(userPass, propertyName);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		UserAndPassword1_8 userPass = new UserAndPassword1_8((User) instance);
		UserResource1_8 userResource = (UserResource1_8) Context.getService(RestService.class).getResourceByName(
		    RestConstants.VERSION_1 + "/user");
		userResource.setProperty(userPass, propertyName, value);
	}
}
