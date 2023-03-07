/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.unrelatedtest.rest.resource;

import io.swagger.models.Model;
import org.openmrs.module.unrelatedtest.UnrelatedGenericChild;
import org.openmrs.module.webservices.rest.doc.SwaggerSpecificationCreatorTest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.test.GenericChildResource;

/**
 * A test resource that is unrelated to the main webservices package.
 * 
 * @see SwaggerSpecificationCreatorTest#testUnrelatedResourceDefinitions()
 */
@Resource(name = RestConstants.VERSION_1 + "/unrelated", supportedClass = UnrelatedGenericChild.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class UnrelatedGenericChildResource extends GenericChildResource {
	
	public static boolean getGETCalled = false;
	
	public static boolean getCREATECalled = false;
	
	public static boolean getUPDATECalled = false;
	
	/*******************************
	 * TEST METHOD IMPLEMENTATIONS * These methods are the ones we want to test against. There
	 * implementaion is unimportant, they just set flags so we can assert the methods were called
	 * correctly by the reflector.
	 */
	
	@Override
	public Model getGETModel(Representation rep) {
		getGETCalled = true;
		return super.getGETModel(rep);
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		getCREATECalled = true;
		return super.getCREATEModel(rep);
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		getUPDATECalled = true;
		return super.getUPDATEModel(rep);
	}
}
