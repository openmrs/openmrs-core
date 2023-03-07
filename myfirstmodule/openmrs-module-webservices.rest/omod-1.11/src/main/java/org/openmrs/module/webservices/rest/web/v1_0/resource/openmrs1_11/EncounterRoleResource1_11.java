/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.EncounterRoleResource1_9;

/**
 * {@link Resource} for {@link EncounterRole}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/encounterrole", supportedClass = EncounterRole.class, supportedOpenmrsVersions = {
        "1.11.* - 9.*" })
public class EncounterRoleResource1_11 extends EncounterRoleResource1_9 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<EncounterRole> doSearch(RequestContext context) {
		return new NeedsPaging<EncounterRole>(Context.getEncounterService().getEncounterRolesByName(
		    context.getParameter("q")), context);
	}
}
