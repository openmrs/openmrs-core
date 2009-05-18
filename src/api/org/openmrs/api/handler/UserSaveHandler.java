/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link User} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br/>
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
	public void handle(User user, User creator, Date dateCreated, String other) {
		// if the user doesn't have a system id, generate one
		if (user.getSystemId() == null || user.getSystemId().equals(""))
			user.setSystemId(Context.getUserService().generateSystemId());
	}
}
