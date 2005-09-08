package org.openmrs;


/**
 * Privilege 
 */
public class Privilege implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private String privilege;
	private String description;

	// Constructors

	/** default constructor */
	public Privilege() {
	}

	/** constructor with id */
	public Privilege(String privilege) {
		this.privilege = privilege;
	}

	// Property accessors

	/**
	 * 
	 */
	public String getPrivilege() {
		return this.privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
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

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Privilege)) return false;
		return privilege.equals(((Privilege)obj).privilege);
	}
	
}