/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.util.ReflectionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * {@link Resource} for User, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/user", supportedClass = UserAndPassword1_8.class, supportedOpenmrsVersions = {
        "1.8.* - 1.12.*" })
public class UserResource1_8 extends MetadataDelegatingCrudResource<UserAndPassword1_8> {
	
	/**
	 * The name of the parameter that can be used to restrict a search to roles.
	 */
	public static final String PARAMETER_ROLES = "roles";
	
	public UserResource1_8() {
		
	}
	
	@RepHandler(RefRepresentation.class)
	public SimpleObject asRef(UserAndPassword1_8 delegate) throws ConversionException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		if (delegate.isRetired()) {
			description.addProperty("retired");
		}
		description.addSelfLink();
		return convertDelegateToRepresentation(delegate, description);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("username");
			description.addProperty("systemId");
			description.addProperty("userProperties");
			description.addProperty("person", Representation.REF);
			description.addProperty("privileges", Representation.REF);
			description.addProperty("roles", Representation.REF);
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("username");
			description.addProperty("systemId");
			description.addProperty("userProperties");
			description.addProperty("person", Representation.DEFAULT);
			description.addProperty("privileges", Representation.DEFAULT);
			description.addProperty("roles", Representation.DEFAULT);
			description.addProperty("allRoles", Representation.DEFAULT);
			description.addProperty("proficientLocales");
			description.addProperty("secretQuestion");
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("username");
		description.addRequiredProperty("password");
		description.addRequiredProperty("person");
		
		description.addProperty("systemId");
		description.addProperty("userProperties");
		description.addProperty("roles");
		description.addProperty("proficientLocales");
		description.addProperty("secretQuestion");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		//FIXME check valid supportedClass
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("username", new StringProperty())
			        .property("systemId", new StringProperty())
			        .property("userProperties", new MapProperty()); //FIXME type
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGetRef"))
			        .property("privileges", new ArrayProperty(new RefProperty("#/definitions/PrivilegeGetRef")))
			        .property("roles", new ArrayProperty(new RefProperty("#/definitions/RoleGetRef")));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGet"))
			        .property("privileges", new ArrayProperty(new RefProperty("#/definitions/PrivilegeGet")))
			        .property("roles", new ArrayProperty(new RefProperty("#/definitions/RoleGet")))
			        .property("allRoles", new ArrayProperty(new RefProperty("#/definitions/RoleGet")))
			        .property("proficientLocales", new ArrayProperty(new ObjectProperty()))
			        .property("secretQuestion", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("username", new StringProperty())
		        .property("password", new StringProperty())
		        .property("person", new RefProperty("#/definitions/PersonCreate"))
		        .property("systemId", new StringProperty())
		        .property("userProperties", new MapProperty()) //FIXME type
		        .property("roles", new ArrayProperty(new RefProperty("#/definitions/RoleCreate")))
		        .property("proficientLocales", new ArrayProperty(new ObjectProperty()))
		        .property("secretQuestion", new StringProperty())
		        
		        .required("username").required("password").required("person");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public UserAndPassword1_8 newDelegate() {
		return new UserAndPassword1_8();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public UserAndPassword1_8 save(UserAndPassword1_8 user) {
		String password = user.getPassword();
		User openmrsUser = Context.getUserService().saveUser(user.getUser(), password);
		Context.refreshAuthenticatedUser();
		if (openmrsUser.getId() != null && StringUtils.isNotBlank(password)) {
			Context.getUserService().changePassword(openmrsUser, password);
		}
		return new UserAndPassword1_8(openmrsUser);
		
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public UserAndPassword1_8 getByUniqueId(String uuid) {
		return new UserAndPassword1_8(Context.getUserService().getUserByUuid(uuid));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(UserAndPassword1_8 user, String reason, RequestContext context) throws ResponseException {
		if (user.isRetired()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getUserService().retireUser(Context.getUserService().getUser(user.getId()), reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(UserAndPassword1_8 user, RequestContext context) throws ResponseException {
		if (user == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getUserService().purgeUser(user.getUser());
	}
	
	/**
	 * @param context A {@link RequestContext} that can contain two parameter values: 'q' for the
	 *            user name and 'roles' for the role restriction. If a user name is given, users
	 *            with a user name beginning with this string will be returned (prefix search). If
	 *            no user name is given, the search will not be restricted to specific user names.
	 *            The roles have to be given as a comma separated string. If multiple roles are
	 *            given, the users having at least one of the roles will be returned. A role may be
	 *            specified either by its UUID or by its display name. If no role parameter is
	 *            given, the search will not be restricted to roles.
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<UserAndPassword1_8> doSearch(RequestContext context) {
		// determine roles 
		List<Role> foundRoles = null;
		final String requestedRolesParameter = context.getParameter(PARAMETER_ROLES);
		if (requestedRolesParameter != null) {
			foundRoles = getRequestedRoles(requestedRolesParameter);
		}
		
		// forward query
		final List<User> users;
		if (isNoRequestedRoleFound(foundRoles)) {
			// for an empty role list there shall be no results
			users = Collections.emptyList();
		} else {
			// Note: a null value for roles is interpreted as 'no restriction to roles'
			users = Context.getUserService().getUsers(context.getParameter("q"), foundRoles, context.getIncludeAll());
		}
		
		// convert to UserAndPassword class
		final List<UserAndPassword1_8> usersResult = new ArrayList<UserAndPassword1_8>();
		for (User user : users) {
			usersResult.add(new UserAndPassword1_8(user));
		}
		
		return new NeedsPaging<UserAndPassword1_8>(usersResult, context);
	}
	
	private boolean isNoRequestedRoleFound(List<Role> roles) {
		return roles != null && roles.isEmpty();
	}
	
	/**
	 * @param rolesParameter A comma separated list of role names or role UUIDs. May not be null.
	 * @return A non-null list of existing {@link Role}s that may be empty, if no valid roles are
	 *         found.
	 */
	private List<Role> getRequestedRoles(final String rolesParameter) {
		final List<Role> result = new ArrayList<Role>();
		
		final List<String> roleStrings = Arrays.asList(StringUtils.split(rolesParameter, ","));
		for (String roleString : roleStrings) {
			// try with uuid
			Role role = Context.getUserService().getRoleByUuid(roleString);
			
			// try with display name
			if (role == null) {
				role = Context.getUserService().getRole(roleString);
			}
			
			// add if found
			if (role != null) {
				result.add(role);
			}
		}
		return result;
	}
	
	/**
	 * Overrides BaseDelegatingResource getProperty method to get properties from User property of
	 * UserAndPassword instead of UserAndPassword itself
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#setProperty(T,
	 *      java.lang.String, java.lang.Object)
	 * @param instance
	 * @param propertyName
	 * @throws ConversionException
	 */
	@Override
	public Object getProperty(UserAndPassword1_8 instance, String propertyName) throws ConversionException {
		try {
			if (propertyName.equals("password")) {
				return instance.getPassword();
			} else {
				// try to find a @PropertyGetter-annotated method
				Method annotatedGetter = ReflectionUtil.findPropertyGetterMethod(this, propertyName);
				if (annotatedGetter != null) {
					return annotatedGetter.invoke(this, instance);
				}
				return PropertyUtils.getProperty(instance.getUser(), propertyName);
			}
		}
		catch (Exception ex) {
			throw new ConversionException(propertyName, ex);
		}
	}
	
	/**
	 * Overrides BaseDelegatingResource setProperty method to allow properties to be set on User
	 * property of UserAndPassword instead of UserAndPassword itself
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#setProperty(T,
	 *      java.lang.String, java.lang.Object)
	 * @param instance
	 * @param propertyName
	 * @param value
	 * @throws ConversionException
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		try {
			UserAndPassword1_8 userAndPassword = (UserAndPassword1_8) instance;
			if (propertyName.equals("password")) {
				userAndPassword.setPassword(value != null ? value.toString() : null);
			} else {
				// just treat every other property like a we're on the User object
				super.setProperty(userAndPassword.getUser(), propertyName, value);
			}
		}
		catch (Exception ex) {
			throw new ConversionException(propertyName, ex);
		}
	}
	
	/**
	 * @param user
	 * @return roles for user
	 * @see User#getRoles()
	 */
	public Set<Role> getRoles(UserAndPassword1_8 user) {
		if (user.getUser().getRoles() == null)
			return null;
		return RestUtil.removeRetiredData(user.getUser().getRoles());
	}
	
	/**
	 * @param user
	 * @return all roles for user
	 * @see User#getAllRoles()
	 */
	public Set<Role> getAllRoles(UserAndPassword1_8 user) {
		if (user.getUser().getRoles() == null)
			return null;
		return RestUtil.removeRetiredData(user.getUser().getAllRoles()); //Get all active roles, including inherited roles
	}
	
	/**
	 * @param user
	 * @return username or systemId (for concise display purposes)
	 */
	@Override
	@PropertyGetter("display")
	public String getDisplayString(UserAndPassword1_8 user) {
		StringBuilder ret = new StringBuilder();
		User u = user.getUser();
		ret.append(StringUtils.isNotEmpty(u.getUsername()) ? u.getUsername() : u.getSystemId());
		return ret.toString();
	}
	
	/**
	 * Overridden here since the unique id is not on UserAndPassword directly
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUniqueId(java.lang.Object)
	 */
	@Override
	protected String getUniqueId(UserAndPassword1_8 delegate) {
		return delegate.getUser().getUuid();
	}
	
	/**
	 * Overridden here since the auditInfo is not on UserAndPassword directly, but on the User
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#getAuditInfo(java.lang.Object)
	 */
	@PropertyGetter("auditInfo")
	public SimpleObject getAuditInfo(UserAndPassword1_8 delegate) throws Exception {
		return super.getAuditInfo(delegate.getUser());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<UserAndPassword1_8> doGetAll(RequestContext context) {
		List<UserAndPassword1_8> users = new ArrayList<UserAndPassword1_8>();
		for (User user : Context.getUserService().getAllUsers()) {
			users.add(new UserAndPassword1_8(user));
		}
		return new NeedsPaging<UserAndPassword1_8>(users, context);
	}
}
