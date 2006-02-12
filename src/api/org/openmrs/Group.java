package org.openmrs;

import java.util.HashSet;
import java.util.Set;

/**
 * Group
 * 
 *  @author Ben Wolfe
 *  @version 1.0
 */
public class Group implements java.io.Serializable {

	public static final long serialVersionUID = 1234233L;

	// Fields

	private String group;
	private String description;
	private Set<Role> roles;

	// Constructors

	/** default constructor */
	public Group() {
	}

	/** constructor with id */
	public Group(String group) {
		this.group = group;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Group)) return false;
		return group.equals(((Group)obj).getGroup());
	}
	
	public int hashCode() {
		if (this.getGroup() == null) return super.hashCode();
		return this.getGroup().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Adds the given Role to the list of roles
	 * @param role
	 */
	public void addRole(Role role) {
		if (roles == null)
			roles = new HashSet<Role>();
		if (!roles.contains(role) && role != null)
			roles.add(role);
	}
	
	/**
	 * Removes the given Role from the list of roles
	 * @param role
	 */
	public void removeRole(Role role) {
		if (roles != null)
			roles.remove(role);
	}
	
	/**
	 * @return Returns the group name.
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group The group name to set.
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.group;
	}
}