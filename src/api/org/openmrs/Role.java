package org.openmrs;

import java.util.List;

/**
 * Role 
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

	// Property accessors

	/**
	 * 
	 */
	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public List getPrivileges() {
		return this.privileges;
	}

	public void setPrivileges(List privileges) {
		this.privileges = privileges;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Role)) return false;
		return role.equals(((Role)obj).role);
	}

}