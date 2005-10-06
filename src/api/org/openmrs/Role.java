package org.openmrs;

import java.util.HashSet;
import java.util.Set;

/**
 * Role
 * 
 *  @author Burke Mamlin
 *  @version 1.0
 */
public class Role implements java.io.Serializable {

	public static final long serialVersionUID = 1234233L;

	// Fields

	private String role;
	private String description;
	private Set<Privilege> privileges;

	// Constructors

	/** default constructor */
	public Role() {
	}

	/** constructor with id */
	public Role(String role) {
		this.role = role;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Role)) return false;
		return role.equals(((Role)obj).getRole());
	}
	
	public int hashCode() {
		if (this.getRole() == null) return super.hashCode();
		return this.getRole().hashCode();
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
	 * @param privilege
	 */
	public void addPrivilege(Privilege privilege) {
		if (privileges == null)
			privileges = new HashSet<Privilege>();
		if (!privileges.contains(privilege) && privilege != null)
			privileges.add(privilege);
	}
	
	/**
	 * Removes the given Privilege from the list of privileges
	 * @param privilege
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


}