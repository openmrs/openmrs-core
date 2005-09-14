package org.openmrs;

import java.util.List;

/**
 * Role
 * 
 *  @author Burke Mamlin
 *  @version 1.0
 */
public class Role implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private String role;
	private String description;
	private List privileges;

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
		return role.equals(((Role)obj).role);
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
	public List getPrivileges() {
		return privileges;
	}

	/**
	 * @param privileges The privileges to set.
	 */
	public void setPrivileges(List privileges) {
		this.privileges = privileges;
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


}