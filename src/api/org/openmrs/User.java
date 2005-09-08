package org.openmrs;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User
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

	// Property accessors

	/**
	 * 
	 * @hibernate.id column="user_id" generator-class="identity"
	 */
	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * 
	 * @hibernate.property column="username"
	 */
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 
	 * @hibernate.property column="first_name" length="50"
	 */
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * 
	 * @hibernate.property column="middle_name" length="50"
	 */
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * 
	 * @hibernate.property column="last_name" length="50"
	 */
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * 
	 * @hibernate.property column="date_created"
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 * @hibernate.property column="date_changed"
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * 
	 * @hibernate.property column="voided"
	 */
	public Boolean isVoided() {
		return this.voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * 
	 * @hibernate.property column="date_voided"
	 */
	public Date getDateVoided() {
		return this.dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy
	 *            The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy
	 *            The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * 
	 */
	public List getRoles() {
		return this.roles;
	}

	public void setRoles(List roles) {
		this.roles = roles;
	}

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

}