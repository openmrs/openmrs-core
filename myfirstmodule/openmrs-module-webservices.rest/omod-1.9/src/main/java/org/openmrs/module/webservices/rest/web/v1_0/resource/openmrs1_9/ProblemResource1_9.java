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

import org.openmrs.activelist.Problem;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ProblemResource1_8;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for Problem, supporting
 * standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/problem", supportedClass = Problem.class, supportedOpenmrsVersions = { "1.9.* - 1.12.*" })
public class ProblemResource1_9 extends ProblemResource1_8 {
	
	/**
	 * Annotated setter for Problem
	 * 
	 * @param problem
	 * @param value
	 */
	
	@PropertySetter("problem")
	public static void setProblem(Problem problem, Object value) {
		problem.setProblem(new ConceptResource1_9().getByUniqueId((String) value));
	}
	
}
