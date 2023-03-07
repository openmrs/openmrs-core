/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.mockingbird.test.rest.resource;

import org.mockingbird.test.Bird;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Fake {@code Resource} used in tests at
 * {@link org.openmrs.module.webservices.rest.web.api.impl.RestServiceImplTest}. Located in a fake
 * package not under org.openmrs.xxx on purpose otherwise it will be picked up by other tests due to
 * {@link org.openmrs.module.webservices.rest.web.OpenmrsClassScanner} and its classpath pattern.
 */
@org.openmrs.module.webservices.rest.web.annotation.Resource(name = RestConstants.VERSION_1 + "/bird", order = 1, supportedClass = Bird.class, supportedOpenmrsVersions = { "1.9.*" })
public class BirdResource_1_9 extends DelegatingCrudResource<Bird> {
	
	/**
	 * @see DelegatingResourceHandler#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
	
	/**
	 * @see DelegatingResourceHandler#newDelegate()
	 */
	@Override
	public Bird newDelegate() {
		return new Bird();
	}
	
	/**
	 * @see DelegatingResourceHandler#save(Object)
	 */
	@Override
	public Bird save(Bird delegate) {
		return null;
	}
	
	/**
	 * @see DelegatingResourceHandler#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return null;
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public Bird getByUniqueId(String uniqueId) {
		return null;
	}
	
	/**
	 * @see DelegatingCrudResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(Bird delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingCrudResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(Bird delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
}
