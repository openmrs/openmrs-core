/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link User} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br>
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see User
 * @since 1.5
 */
@Handler(supports = User.class)
public class UserSaveHandler implements SaveHandler<User> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(User user, User creator, Date dateCreated, String other) {
		// if the user doesn't have a system id, generate one
		if (StringUtils.isEmpty(user.getSystemId())) {
			user.setSystemId(Context.getUserService().generateSystemId());
		}
		
		// the framework only automatically recurses on properties that are Collection<OpenmrsObject>
		// so we need to do this manually
		if (user.getPerson() != null) {
			loadLazyHibernateCollections(user);
			RequiredDataAdvice.recursivelyHandle(SaveHandler.class, user.getPerson(), creator, dateCreated, other,
					new ArrayList<>());
		}
	}
	
	private void loadLazyHibernateCollections(User user) {
		if (user.getPerson() instanceof Patient) {
			((Patient) user.getPerson()).getPatientIdentifier();
		}
	}
	
}
