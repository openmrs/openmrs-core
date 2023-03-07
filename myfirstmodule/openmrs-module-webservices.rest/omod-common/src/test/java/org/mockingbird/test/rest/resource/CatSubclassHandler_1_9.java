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

import io.swagger.models.Model;
import org.mockingbird.test.Animal;
import org.mockingbird.test.Cat;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Fake {@code Resource} used in tests at
 * {@link org.openmrs.module.webservices.rest.web.api.impl.RestServiceImplTest}. Located in a fake
 * package not under org.openmrs.xxx on purpose otherwise it will be picked up by other tests due to
 * {@link org.openmrs.module.webservices.rest.web.OpenmrsClassScanner} and its classpath pattern.
 */
@org.openmrs.module.webservices.rest.web.annotation.SubClassHandler(supportedClass = Cat.class, supportedOpenmrsVersions = { "1.9.*" })
public class CatSubclassHandler_1_9 implements DelegatingSubclassHandler<Animal, Cat> {
	
	@Override
	public String getTypeName() {
		return "cat";
	}
	
	@Override
	public Class<Animal> getSuperclass() {
		return Animal.class;
	}
	
	@Override
	public Class<Cat> getSubclassHandled() {
		return Cat.class;
	}
	
	@Override
	public PageableResult getAllByType(RequestContext context) throws ResourceDoesNotSupportOperationException {
		return null;
	}
	
	@Override
	public String getResourceVersion() {
		return null;
	}
	
	@Override
	public Cat newDelegate() {
		return null;
	}
	
	@Override
	public Cat newDelegate(SimpleObject object) {
		return null;
	}
	
	@Override
	public Cat save(Cat delegate) {
		return null;
	}
	
	@Override
	public void purge(Cat delegate, RequestContext context) throws ResponseException {
		
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		return null;
	}
	
	@Override
	public Model getGETModel(Representation representation) {
		return null;
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		return null;
	}
	
	@Override
	public Model getUPDATEModel(Representation representation) {
		return null;
	}
}
