/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.RoleConstants;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.load.Replace;
import org.simpleframework.xml.load.Validate;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Defines a User Account in the system. This account belongs to a {@link Person} in the system,
 * although that person may have other user accounts. Users have login credentials
 * (username/password) and can have special user properties. User properties are just simple
 * key-value pairs for either quick info or display specific info that needs to be persisted (like
 * locale preferences, search options, etc)
 */
public class User extends BaseOpenmrsMetadata implements java.io.Serializable, Attributable<User> {
	
	public static final long serialVersionUID = 2L;
	
	protected final Log log = LogFactory.getLog(getClass());
	
	// Fields
	
	private Integer userId;
	
	private Person person;
	
	private String systemId;
	
	private String username;
	
	private String secretQuestion;
	
	private Set<Role> roles;
	
	private Map<String, String> userProperties;
	
	private List<Locale> proficientLocales = null;
	
	private String parsedProficientLocalesProperty = "";
	
	// Constructors
	
	/** default constructor */
	public User() {
	}
	
	/** constructor with id */
	public User(Integer userId) {
		this.userId = userId;
	}
	
	/** constructor with person object */
	public User(Person person) {
		this.person = person;
	}
	
	/**
	 * Return true if this user has all privileges
	 * 
	 * @return true/false if this user is defined as a super user
	 */
	public boolean isSuperUser() {
		return containsRole(RoleConstants.SUPERUSER);
	}
	
	/**
	 * This method shouldn't be used directly. Use org.openmrs.api.context.Context#hasPrivilege so
	 * that anonymous/authenticated/proxy privileges are all included Return true if this user has
	 * the specified privilege
	 * 
	 * @param privilege
	 * @return true/false depending on whether user has specified privilege
	 */
	public boolean hasPrivilege(String privilege) {
		
		// All authenticated users have the "" (empty) privilege
		if (StringUtils.isEmpty(privilege)) {
			return true;
		}
		
		if (isSuperUser()) {
			return true;
		}
		
		Set<Role> tmproles = getAllRoles();
		
		// loop over the roles and check each for the privilege
		for (Iterator<Role> i = tmproles.iterator(); i.hasNext();) {
			if (i.next().hasPrivilege(privilege)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if this user has the given String role
	 * 
	 * @param r String name of a role to check
	 * @return Returns true if this user has the specified role, false otherwise
	 */
	public boolean hasRole(String r) {
		return hasRole(r, false);
	}
	
	/**
	 * Checks if this user has the given String role
	 * 
	 * @param r String name of a role to check
	 * @param ignoreSuperUser If this is false, then this method will always return true for a
	 *            superuser.
	 * @return Returns true if the user has the given role, or if ignoreSuperUser is false and the
	 *         user is a superUser
	 */
	public boolean hasRole(String r, boolean ignoreSuperUser) {
		if (!ignoreSuperUser) {
			if (isSuperUser()) {
				return true;
			}
		}
		
		if (roles == null) {
			return false;
		}
		
		Set<Role> tmproles = getAllRoles();
		
		if (log.isDebugEnabled()) {
			log.debug("User #" + userId + " has roles: " + tmproles);
		}
		
		return containsRole(r);
	}
	
	/**
	 * Checks if the user has a given role. Role name comparisons are not case sensitive.
	 * @param  roleName the name of the role to check
	 * @return true if the user has the given role, else false
	 * @should return true if the user has the given role
	 * @should return false if the user does not have the given role
	 * @should be case insensitive
	 */
	public boolean containsRole(String roleName) {
		for (Role role : getAllRoles()) {
			if (role.getRole().equalsIgnoreCase(roleName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get <i>all</i> privileges this user has. This delves into all of the roles that a person has,
	 * appending unique privileges
	 * 
	 * @return Collection of complete Privileges this user has
	 */
	public Collection<Privilege> getPrivileges() {
		Set<Privilege> privileges = new HashSet<Privilege>();
		Set<Role> tmproles = getAllRoles();
		
		Role role;
		for (Iterator<Role> i = tmproles.iterator(); i.hasNext();) {
			role = i.next();
			Collection<Privilege> privs = role.getPrivileges();
			if (privs != null) {
				privileges.addAll(privs);
			}
		}
		
		return privileges;
	}
	
	// Property accessors
	
	/**
	 * Returns all roles attributed to this user by expanding the role list to include the parents
	 * of the assigned roles
	 * 
	 * @return all roles (inherited from parents and given) for this user
	 */
	public Set<Role> getAllRoles() {
		// the user's immediate roles
		Set<Role> baseRoles = new HashSet<Role>();
		
		// the user's complete list of roles including
		// the parent roles of their immediate roles
		Set<Role> totalRoles = new HashSet<Role>();
		if (getRoles() != null) {
			baseRoles.addAll(getRoles());
			totalRoles.addAll(getRoles());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("User's base roles: " + baseRoles);
		}
		
		try {
			for (Role r : baseRoles) {
				totalRoles.addAll(r.getAllParentRoles());
			}
		}
		catch (ClassCastException e) {
			log.error("Error converting roles for user: " + this);
			log.error("baseRoles.class: " + baseRoles.getClass().getName());
			log.error("baseRoles: " + baseRoles.toString());
			Iterator<Role> iter = baseRoles.iterator();
			while (iter.hasNext()) {
				log.error("baseRole: '" + iter.next() + "'");
			}
		}
		return totalRoles;
	}
	
	/**
	 * @return Returns the roles.
	 */
	public Set<Role> getRoles() {
		return roles;
	}
	
	/**
	 * @param roles The roles to set.
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	/**
	 * Add the given Role to the list of roles for this User
	 * 
	 * @param role
	 * @return Returns this user with the given role attached
	 */
	public User addRole(Role role) {
		if (roles == null) {
			roles = new HashSet<Role>();
		}
		if (!roles.contains(role) && role != null) {
			roles.add(role);
		}
		
		return this;
	}
	
	/**
	 * Remove the given Role from the list of roles for this User
	 * 
	 * @param role
	 * @return this user with the given role removed
	 */
	public User removeRole(Role role) {
		if (roles != null) {
			roles.remove(role);
		}
		
		return this;
	}
	
	/**
	 * @see org.openmrs.Attributable#findPossibleValues(java.lang.String)
	 */
	public List<User> findPossibleValues(String searchText) {
		try {
			return Context.getUserService().getUsersByName(searchText, "", false);
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#getPossibleValues()
	 */
	public List<User> getPossibleValues() {
		try {
			return Context.getUserService().getAllUsers();
		}
		catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#hydrate(java.lang.String)
	 */
	public User hydrate(String userId) {
		try {
			return Context.getUserService().getUser(Integer.valueOf(userId));
		}
		catch (Exception e) {
			return new User();
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#serialize()
	 */
	public String serialize() {
		if (getUserId() != null) {
			return "" + getUserId();
		} else {
			return "";
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	public String getDisplayString() {
		String returnString = "";
		if (getPersonName() != null) {
			returnString += getPersonName().getFullName() + " ";
		}
		
		returnString += "(" + getUsername() + ")";
		return returnString;
		
	}
	
	/**
	 * @return Returns the systemId.
	 */
	@Attribute(required = false)
	public String getSystemId() {
		return systemId;
	}
	
	/**
	 * @param systemId The systemId to set.
	 */
	@Attribute(required = false)
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	
	/**
	 * @return Returns the userId.
	 */
	@Attribute(required = true)
	public Integer getUserId() {
		return userId;
	}
	
	/**
	 * @param userId The userId to set.
	 */
	@Attribute(required = true)
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	/**
	 * @deprecated see {@link #setPerson(Person)}
	 */
	@Deprecated
	public void setPersonId(Integer personId) {
		throw new APIException("User.setPersonId", (Object[]) null);
	}
	
	/**
	 * @return the person
	 * @since 1.6
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @return the person, creating a new object if person is null
	 */
	private Person getPersonMaybeCreate() {
		if (person == null) {
			person = new Person();
		}
		return person;
	}
	
	/**
	 * @param person the person to set
	 * @since 1.6
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return Returns the username.
	 */
	@Attribute(required = false)
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username The username to set.
	 */
	@Attribute(required = false)
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @deprecated Use LoginCredentials
	 * @return Returns the secretQuestion.
	 */
	@Deprecated
	public String getSecretQuestion() {
		return secretQuestion;
	}
	
	/**
	 * @deprecated Use LoginCredentials
	 * @param secretQuestion The secretQuestion to set.
	 */
	@Deprecated
	public void setSecretQuestion(String secretQuestion) {
		this.secretQuestion = secretQuestion;
	}
	
	@Override
	public String toString() {
		return username;
	}
	
	/**
	 * @return Returns the userProperties.
	 */
	public Map<String, String> getUserProperties() {
		if (userProperties == null) {
			userProperties = new HashMap<String, String>();
		}
		return userProperties;
	}
	
	/**
	 * @param userProperties A Map<String,String> of the properties to set.
	 */
	public void setUserProperties(Map<String, String> userProperties) {
		this.userProperties = userProperties;
	}
	
	/**
	 * Convenience method. Adds the given property to the user's properties
	 */
	public void setUserProperty(String prop, String value) {
		getUserProperties().put(prop, value);
	}
	
	/**
	 * Convenience method. Removes the given property from the user's properties
	 */
	public void removeUserProperty(String prop) {
		if (getUserProperties() != null && userProperties.containsKey(prop)) {
			userProperties.remove(prop);
		}
	}
	
	/**
	 * Get prop property from this user's properties. If prop is not found in properties, return
	 * empty string
	 * 
	 * @param prop
	 * @return property value
	 */
	public String getUserProperty(String prop) {
		if (getUserProperties() != null && userProperties.containsKey(prop)) {
			return userProperties.get(prop);
		}
		
		return "";
	}
	
	/**
	 * Get prop property from this user's properties. If prop is not found in properties, return
	 * <code>defaultValue</code>
	 * 
	 * @param prop
	 * @param defaultValue
	 * @return property value
	 * @see #getUserProperty(java.lang.String)
	 */
	public String getUserProperty(String prop, String defaultValue) {
		if (getUserProperties() != null && userProperties.containsKey(prop)) {
			return userProperties.get(prop);
		}
		
		return defaultValue;
	}
	
	/**
	 * @see Person#addName(PersonName)
	 */
	public void addName(PersonName name) {
		getPersonMaybeCreate().addName(name);
	}
	
	/**
	 * @see Person#getPersonName()
	 */
	public PersonName getPersonName() {
		return getPerson() == null ? null : getPerson().getPersonName();
	}
	
	/**
	 * Get givenName on the Person this user account belongs to
	 * 
	 * @see Person#getGivenName()
	 */
	public String getGivenName() {
		return getPerson() == null ? null : getPerson().getGivenName();
	}
	
	/**
	 * Get familyName on the Person this user account belongs to
	 * 
	 * @see Person#getFamilyName()
	 */
	public String getFamilyName() {
		return getPerson() == null ? null : getPerson().getFamilyName();
	}
	
	/**
	 * @see org.openmrs.Person#getNames()
	 */
	public Set<PersonName> getNames() {
		return person.getNames();
	}
	
	/**
	 * @deprecated use <tt>getGivenName</tt> on <tt>Person</tt>
	 * @return String user's first name
	 */
	@Deprecated
	public String getFirstName() {
		return getGivenName();
	}
	
	/**
	 * @deprecated use <tt>getFamilyName</tt> on <tt>Person</tt>
	 * @return String user's last name
	 */
	@Deprecated
	public String getLastName() {
		return getFamilyName();
	}
	
	/**
	 * If the serializer wishes, don't serialize this entire object, just the important parts
	 * 
	 * @param sessionMap serialization session information
	 * @return User object to serialize
	 * @see OpenmrsUtil#isShortSerialization(Map)
	 */
	@Replace
	public User replaceSerialization(Map<?, ?> sessionMap) {
		if (OpenmrsUtil.isShortSerialization(sessionMap)) {
			// only serialize the user id
			return new User(getUserId());
		}
		
		// don't do short serialization
		return this;
	}
	
	@Validate
	public void validateSerialization(Map<?, ?> sessionMap) {
		if (OpenmrsUtil.isShortSerialization(sessionMap)) {
			// only serialize the user id
			
		}
		
		return;
	}
	
	/**
	 * Returns a list of Locales for which the User is considered proficient.
	 * 
	 * @return List of the User's proficient locales
	 */
	public List<Locale> getProficientLocales() {
		String proficientLocalesProperty = getUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES);
		
		if ((proficientLocales == null)
		        || (!OpenmrsUtil.nullSafeEquals(parsedProficientLocalesProperty, proficientLocalesProperty))) {
			parsedProficientLocalesProperty = proficientLocalesProperty;
			proficientLocales = new ArrayList<Locale>();
			if (proficientLocalesProperty != null) {
				String[] proficientLocalesArray = proficientLocalesProperty.split(",");
				for (String proficientLocaleSpec : proficientLocalesArray) {
					if (proficientLocaleSpec.length() > 0) {
						Locale proficientLocale = LocaleUtility.fromSpecification(proficientLocaleSpec);
						if (!proficientLocales.contains(proficientLocale)) {
							proficientLocales.add(proficientLocale);
							if (StringUtils.isNotEmpty(proficientLocale.getCountry())) {
								// add the language also
								Locale languageOnlyLocale = LocaleUtility.fromSpecification(proficientLocale.getLanguage());
								if (!proficientLocales.contains(languageOnlyLocale)) {
									proficientLocales.add(LocaleUtility.fromSpecification(proficientLocale.getLanguage()));
								}
							}
						}
					}
				}
			}
		}
		
		// return a copy so that the list isn't changed by other processes
		return new ArrayList<Locale>(proficientLocales);
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getUserId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setUserId(id);
	}
	
}
