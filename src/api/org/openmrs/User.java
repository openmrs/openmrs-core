package org.openmrs;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class User implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer userId;
	private String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private List roles;

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
		List roles = getRoles();

		check_privileges: for (Iterator i = roles.iterator(); i.hasNext();) {
			Role role = (Role) i.next();
			Privilege oPrivilege = new Privilege();
			oPrivilege.setPrivilege(privilege);
			List privileges = role.getPrivileges();
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
	
	
	// Property accessors

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

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
	public List getRoles() {
		return roles;
	}

	/**
	 * @param roles The roles to set.
	 */
	public void setRoles(List roles) {
		this.roles = roles;
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

	/**
	 * @return Returns the voided.
	 */
	public boolean isVoided() {
		return voided;
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}


}