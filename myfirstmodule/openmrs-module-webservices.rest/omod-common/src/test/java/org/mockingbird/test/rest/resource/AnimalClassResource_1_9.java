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
import org.mockingbird.test.AnimalClass;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Fake {@code Resource} used in tests at
 * {@link org.openmrs.module.webservices.rest.web.api.impl.RestServiceImplTest}. Located in a fake
 * package not under org.openmrs.xxx on purpose otherwise it will be picked up by other tests due to
 * {@link org.openmrs.module.webservices.rest.web.OpenmrsClassScanner} and its classpath pattern.
 */
@org.openmrs.module.webservices.rest.web.annotation.SubResource(path = "class", parent = AnimalResource_1_9.class, supportedClass = AnimalClass.class, supportedOpenmrsVersions = { "1.9.*" })
public class AnimalClassResource_1_9 extends DelegatingSubResource<AnimalClass, Animal, AnimalResource_1_9> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return null;
	}
	
	@Override
	public Animal getParent(AnimalClass instance) {
		return null;
	}
	
	@Override
	public void setParent(AnimalClass instance, Animal parent) {
		
	}
	
	@Override
	public PageableResult doGetAll(Animal parent, RequestContext context) throws ResponseException {
		return null;
	}
	
	@Override
	public AnimalClass getByUniqueId(String uniqueId) {
		return null;
	}
	
	@Override
	protected void delete(AnimalClass delegate, String reason, RequestContext context) throws ResponseException {
		
	}
	
	@Override
	public void purge(AnimalClass delegate, RequestContext context) throws ResponseException {
		
	}
	
	@Override
	public String getUri(Object instance) {
		return "name";
	}
	
	@Override
	public AnimalClass newDelegate() {
		return null;
	}
	
	@Override
	public AnimalClass save(AnimalClass delegate) {
		return null;
	}
}
