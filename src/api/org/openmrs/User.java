package org.openmrs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class User extends Person implements java.io.Serializable {

	public static final long serialVersionUID = 4489L;

	// Fields

	private Integer userId;
	private String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private Set<Role> roles;

	/*private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;*/

	// Constructors

	/** default constructor */
	public User() {
	}

	/** constructor with id */
	public User(Integer userId) {
		this.userId = userId;
	}

	/**
	 * return true if this user has the specified privilege
	 * @param privilege
	 * @return true/false
	 */
	public boolean hasPrivilege(String privilege) {

		boolean hasPrivilege = false;
		Set roles = getRoles();

		check_privileges: for (Iterator i = roles.iterator(); i.hasNext();) {
			Role role = (Role) i.next();
			Privilege oPrivilege = new Privilege();
			oPrivilege.setPrivilege(privilege);
			Set privileges = role.getPrivileges();
			if (privileges.contains(oPrivilege)) {
				hasPrivilege = true;
				break;
			}
		}

		return hasPrivilege;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User u = (User)obj;;
			return (getUserId().equals(u.getUserId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getUserId() == null) return super.hashCode();
		int hash = 2;
		hash = 31 * hash + this.getUserId(); 
		return hash;
	}
	
	// Property accessors

	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return Returns the lastName.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName The lastName to set.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return Returns the middleName.
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName The middleName to set.
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
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
	 * @param roleservation
	 */
	public void addRole(Role role) {
		if (roles == null)
			roles = new HashSet<Role>();
		if (!roles.contains(role) && role != null)
			roles.add(role);
	}

	/**
	 * Remove the given obervation from the list of roles for this User
	 * @param roleservation
	 */
	public void removeRole(Role roleservation) {
		if (roles != null)
			roles.remove(roleservation);
	}

	/**
	 * @return Returns the userId.
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/*
	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	public Boolean isVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	 */
	
	public String toString() {
		return username;
	}

}