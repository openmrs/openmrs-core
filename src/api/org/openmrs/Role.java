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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;

/**
 * A Role is just an aggregater of {@link Privilege}s. {@link User}s contain a number of roles
 * (Users DO NOT contain any privileges directly) Roles can be grouped by inheriting other roles. If
 * a user is given Role A that inherits from Role B, the user has all rights/abilities for both Role
 * A's privileges and for Role B's privileges.
 * 
 * @see Privilege
 */
public class Role extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 1234233L;
	
	private static Log log = LogFactory.getLog(Role.class);
	
	// Fields
	
	private String role;
	
	private Set<Privilege> privileges;
	
	private Set<Role> inheritedRoles;
	
	// Constructors
	
	/** default constructor */
	public Role() {
	}
	
	/** constructor with id */
	public Role(String role) {
		this.role = role;
	}
	
	/** constructor with all database required properties */
	public Role(String role, String description) {
		this.role = role;
		setDescription(description);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Role) || role == null)
			return false;
		return role.equals(((Role) obj).getRole());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getRole() == null)
			return super.hashCode();
		return this.getRole().hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return Returns the privileges.
	 */
	public Set<Privilege> getPrivileges() {
		return privileges;
	}
	
	/**
	 * @param privileges The privileges to set.
	 */
	public void setPrivileges(Set<Privilege> privileges) {
		this.privileges = privileges;
	}
	
	/**
	 * Adds the given Privilege to the list of privileges
	 * 
	 * @param privilege Privilege to add
	 */
	public void addPrivilege(Privilege privilege) {
		if (privileges == null)
			privileges = new HashSet<Privilege>();
		if (!privileges.contains(privilege) && privilege != null)
			privileges.add(privilege);
	}
	
	/**
	 * Removes the given Privilege from the list of privileges
	 * 
	 * @param privilege Privilege to remove
	 */
	public void removePrivilege(Privilege privilege) {
		if (privileges != null)
			privileges.remove(privilege);
	}
	
	/**
	 * @return Returns the role.
	 */
	public String getRole() {
		return role;
	}
	
	/**
	 * @param role The role to set.
	 */
	public void setRole(String role) {
		this.role = role;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.role;
	}
	
	/**
	 * Looks for the given <code>privilegeName</code> privilege name in this roles privileges. This
	 * method does not recurse through the inherited roles
	 * 
	 * @param privilegeName String name of a privilege
	 * @return true/false whether this role has the given privilege
	 * @should return false if not found
	 * @should return true if found
	 * @should not fail given null parameter
	 * @should return true for any privilegeName if super user
	 */
	public boolean hasPrivilege(String privilegeName) {
		
		if (OpenmrsConstants.SUPERUSER_ROLE.equals(this.role))
			return true;
		
		if (privileges != null) {
			for (Privilege p : privileges) {
				if (p.getPrivilege().equals(privilegeName))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return Returns the inheritedRoles.
	 */
	public Set<Role> getInheritedRoles() {
		return inheritedRoles;
	}
	
	/**
	 * @param inheritedRoles The inheritedRoles to set.
	 */
	public void setInheritedRoles(Set<Role> inheritedRoles) {
		this.inheritedRoles = inheritedRoles;
	}
	
	/**
	 * Convenience method to test whether or not this role extends/ inherits from any other roles
	 * 
	 * @return true/false whether this role inherits from other roles
	 */
	public boolean inheritsRoles() {
		return (getInheritedRoles() != null && getInheritedRoles().size() > 0);
	}
	
	/**
	 * Recursive (if need be) method to return all parent roles of this role
	 * 
	 * @return Return this role's parents
	 */
	public Set<Role> getAllParentRoles() {
		Set<Role> parents = new HashSet<Role>();
		if (inheritsRoles()) {
			parents.addAll(this.recurseOverParents(parents));
		}
		return parents;
	}
	
	/**
	 * Returns the full set of roles be looping over inherited roles. Duplicate roles are dropped.
	 * 
	 * @param children Roles already looped over
	 * @return Set<Role> Current and inherited roles
	 */
	public Set<Role> recurseOverParents(final Set<Role> children) {
		if (!this.inheritsRoles())
			return children;
		
		Set<Role> allRoles = new HashSet<Role>(); // total roles (parents + children)
		Set<Role> myRoles = new HashSet<Role>(); // new roles
		allRoles.addAll(children);
		
		myRoles.addAll(this.getInheritedRoles());
		myRoles.removeAll(children);
		myRoles.remove(this); // prevent an obvious looping problem
		allRoles.addAll(myRoles);
		
		for (Role r : myRoles) {
			if (r.inheritsRoles())
				allRoles.addAll(r.recurseOverParents(allRoles));
		}
		
		if (log.isDebugEnabled())
			log.debug("Total roles: " + allRoles);
		
		return allRoles;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}
