/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.ConceptDatatype;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ConceptDatatypeResource1_8;

/**
 * {@link Resource} for {@link ConceptDatatype}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptdatatype", supportedClass = ConceptDatatype.class, supportedOpenmrsVersions = {
        "2.0.* - 9.*" })
public class ConceptDatatypeResource2_0 extends ConceptDatatypeResource1_8 {
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptDatatype save(ConceptDatatype conceptDatatype) {
		throw new ResourceDoesNotSupportOperationException();
	}
}
