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

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.UserResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;

/**
 * {@link Resource} for User, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/user", supportedClass = UserAndPassword1_8.class, supportedOpenmrsVersions = {
        "2.0.* - 9.*" })
public class UserResource2_0 extends UserResource1_8 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (description != null) {
			description.removeProperty("secretQuestion");
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public UserAndPassword1_8 save(UserAndPassword1_8 user) {
		final User savedUser = createOrUpdateUser(user);
		refreshAuthenticatedUserIfNeeded(savedUser);
		return new UserAndPassword1_8(savedUser);
	}

	private User createOrUpdateUser(UserAndPassword1_8 user) {
		final User openmrsUser;

		if (user.getUser().getUserId() == null) {
			openmrsUser = Context.getUserService().createUser(user.getUser(), user.getPassword());
		} else {
			openmrsUser = Context.getUserService().saveUser(user.getUser());
		}

		return openmrsUser;
	}

	private void refreshAuthenticatedUserIfNeeded(User savedUser) {
		final User authenticatedUser = Context.getAuthenticatedUser();
		if (authenticatedUser != null && authenticatedUser.getId().equals(savedUser.getId())) {
			Context.refreshAuthenticatedUser();
		}
	}
}
