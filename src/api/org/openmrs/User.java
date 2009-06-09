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
package org.openmrs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.load.Replace;
import org.simpleframework.xml.load.Validate;

/**
 * Defines a User in the system. A user is simply an extension of a person and all that that
 * implies. A user is defined as someone who will be manipulating the system and must log in or is
 * being referred to in another part of the system (as a provider, creator, etc) Users are special
 * <code>Person</code>s in that they have login credentials (login/password) and can have special
 * user properties. User properties are just simple key-value pairs for either quick info or display
 * specific info that needs to be persisted (like locale preferences, search options, etc)
 */
public class User extends Person implements java.io.Serializable {
	
	public static final long serialVersionUID = 4489L;
	
	private transient Log log = LogFactory.getLog(getClass());
	
	// Fields
	
	private Integer userId;
	
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
		super(userId);
		this.userId = userId;
	}
	
	/** constructor with person object */
	public User(Person person) {
		super(person);
		if (person != null)
			userId = person.getPersonId();
	}
	
	/**
	 * Return true if this user has all privileges
	 * 
	 * @return true/false if this user is defined as a super user
	 */
	public boolean isSuperUser() {
		Set<Role> tmproles = getAllRoles();
		
		Role role = new Role(OpenmrsConstants.SUPERUSER_ROLE); // default administrator with
		// complete control
		
		if (tmproles.contains(role))
			return true;
		
		return false;
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
		if (privilege == null || privilege.equals(""))
			return true;
		
		if (isSuperUser())
			return true;
		
		Set<Role> tmproles = getAllRoles();
		
		// loop over the roles and check each for the privilege
		for (Iterator<Role> i = tmproles.iterator(); i.hasNext();) {
			if (i.next().hasPrivilege(privilege))
				return true;
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
		if (ignoreSuperUser == false) {
			if (isSuperUser())
				return true;
		}
		
		if (roles == null)
			return false;
		
		Set<Role> tmproles = getAllRoles();
		
		if (log.isDebugEnabled())
			log.debug("User #" + userId + " has roles: " + tmproles);
		
		Role role = new Role(r);
		
		if (tmproles.contains(role))
			return true;
		
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
			if (privs != null)
				privileges.addAll(privs);
		}
		
		return privileges;
	}
	
	/**
	 * Compares two objects for similarity This must pass through to the parent object
	 * (org.openmrs.Person) in order to get similarity of person/user objects
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 * @see org.openmrs.Person#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	/**
	 * The hashcode for a user/person is used to index the objects in a tree This must pass through
	 * to the parent object (org.openmrs.Person) in order to get similarity of person/user objects
	 * 
	 * @see org.openmrs.Person#hashCode()
	 */
	public int hashCode() {
		return super.hashCode();
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
		
		if (log.isDebugEnabled())
			log.debug("User's base roles: " + baseRoles);
		
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
		if (roles == null)
			roles = new HashSet<Role>();
		if (!roles.contains(role) && role != null)
			roles.add(role);
		
		return this;
	}
	
	/**
	 * Remove the given Role from the list of roles for this User
	 * 
	 * @param role
	 * @return this user with the given role removed
	 */
	public User removeRole(Role role) {
		if (roles != null)
			roles.remove(role);
		
		return this;
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
		super.setPersonId(userId);
		this.userId = userId;
	}
	
	/**
	 * Overrides the parent setPersonId(Integer) so that we can be sure user id is also set
	 * correctly.
	 * 
	 * @see org.openmrs.Person#setPersonId(java.lang.Integer)
	 */
	public void setPersonId(Integer personId) {
		super.setPersonId(personId);
		this.userId = personId;
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
	 * @return Returns the secretQuestion.
	 */
	public String getSecretQuestion() {
		return secretQuestion;
	}
	
	/**
	 * @param secretQuestion The secretQuestion to set.
	 */
	public void setSecretQuestion(String secretQuestion) {
		this.secretQuestion = secretQuestion;
	}
	
	public String toString() {
		return "" + getPersonName();
	}
	
	/**
	 * @return Returns the userProperties.
	 */
	public Map<String, String> getUserProperties() {
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
		if (getUserProperties() == null)
			userProperties = new HashMap<String, String>();
		
		userProperties.put(prop, value);
	}
	
	/**
	 * Convenience method. Removes the given property from the user's properties
	 */
	public void removeUserProperty(String prop) {
		if (getUserProperties() != null && userProperties.containsKey(prop))
			userProperties.remove(prop);
	}
	
	/**
	 * Get prop property from this user's properties. If prop is not found in properties, return
	 * empty string
	 * 
	 * @param prop
	 * @return property value
	 */
	public String getUserProperty(String prop) {
		if (getUserProperties() != null && userProperties.containsKey(prop))
			return userProperties.get(prop);
		
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
		if (getUserProperties() != null && userProperties.containsKey(prop))
			return userProperties.get(prop);
		
		return defaultValue;
	}
	
	/**
	 * @deprecated use <tt>getGivenName</tt> on <tt>Person</tt>
	 * @return String user's first name
	 */
	public String getFirstName() {
		return getGivenName();
	}
	
	/**
	 * @deprecated use <tt>getFamilyName</tt> on <tt>Person</tt>
	 * @return String user's last name
	 */
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
		
		if ((proficientLocales == null) || (!parsedProficientLocalesProperty.equals(proficientLocalesProperty))) {
			parsedProficientLocalesProperty = proficientLocalesProperty;
			proficientLocales = new ArrayList<Locale>();
			
			String[] proficientLocalesArray = proficientLocalesProperty.split(",");
			for (String proficientLocaleSpec : proficientLocalesArray) {
				if (proficientLocaleSpec.length() > 0) {
					Locale proficientLocale = LocaleUtility.fromSpecification(proficientLocaleSpec);
					if (!proficientLocales.contains(proficientLocale)) {
						proficientLocales.add(proficientLocale);
						if (proficientLocale.getCountry() != "") {
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
