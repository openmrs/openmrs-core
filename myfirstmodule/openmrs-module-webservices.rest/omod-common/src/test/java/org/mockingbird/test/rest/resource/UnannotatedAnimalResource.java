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

import org.mockingbird.test.Animal;
import org.openmrs.module.webservices.rest.web.RequestContext;
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
public class UnannotatedAnimalResource extends DelegatingCrudResource<Animal> {
	
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
	public Animal newDelegate() {
		return new Animal();
	}
	
	/**
	 * @see DelegatingResourceHandler#save(Object)
	 */
	@Override
	public Animal save(Animal delegate) {
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
	public Animal getByUniqueId(String uniqueId) {
		return null;
	}
	
	/**
	 * @see DelegatingCrudResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(Animal delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingCrudResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(Animal delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
}
