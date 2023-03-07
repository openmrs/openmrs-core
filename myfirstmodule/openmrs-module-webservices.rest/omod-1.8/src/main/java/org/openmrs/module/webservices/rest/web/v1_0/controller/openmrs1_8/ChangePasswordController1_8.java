/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.validation.ValidationException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/password")
public class ChangePasswordController1_8 extends BaseRestController {
	
	@Qualifier("userService")
	@Autowired
	private UserService userService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void changeOwnPassword(@RequestBody Map<String, String> body) {
		String oldPassword = body.get("oldPassword");
		String newPassword = body.get("newPassword");
		if (!Context.isAuthenticated()) {
			throw new APIAuthenticationException("Must be authenticated to change your own password");
		}
		try {
			userService.changePassword(oldPassword, newPassword);
		}
		catch (APIException ex) {
			// this happens if they give the wrong oldPassword
			throw new ValidationException(ex.getMessage());
		}
	}
	
	@RequestMapping(value = "/{userUuid}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void changeOthersPassword(@PathVariable("userUuid") String userUuid, @RequestBody Map<String, String> body) {
		String newPassword = body.get("newPassword");
		Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
		Context.addProxyPrivilege("Get Users"); // support later versions of OpenMRS
		User user;
		try {
			user = userService.getUserByUuid(userUuid);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.removeProxyPrivilege("Get Users");
		}
		
		if (user == null || user.getUserId() == null) {
			throw new NullPointerException();
		} else {
			userService.changePassword(user, newPassword);
		}
	}
	
	// This probably belongs in the base class, but we don't want to test all the behaviors that would change
	@ExceptionHandler(NullPointerException.class)
	@ResponseBody
	public SimpleObject handleNotFound(NullPointerException exception, HttpServletRequest request,
	        HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return RestUtil.wrapErrorResponse(exception, "User not found");
	}
	
}
