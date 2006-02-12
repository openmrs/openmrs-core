package org.openmrs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;

/**
 * User
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class User extends Person implements java.io.Serializable {

	public static final long serialVersionUID = 4489L;
	public Log log = LogFactory.getLog(getClass());

	// Fields

	private String systemId;
	private Integer userId;
	private String username;
	private String firstName;
	private String middleName;
	private String lastName;
	private String secretQuestion;
	private Set<Role> roles;
	private Set<Group> groups;
	private Map<String, String> properties;

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
	 * Return true is this user has all privileges
	 * @return
	 */
	public boolean isSuperUser() {
		Set<Role> tmproles = getRoles();

		if (groups != null)
			for (Group g : groups) {
				tmproles.addAll(g.getRoles());
			}
		
		Role role = new Role(OpenmrsConstants.SUPERUSER_ROLE);	//default administrator with complete control
		
		if (tmproles.contains(role))
			return true;
		
		return false;
	}

	/**
	 * return true if this user has the specified privilege
	 * @param privilege
	 * @return true/false
	 */
	public boolean hasPrivilege(String privilege) {

		if (isSuperUser())
			return true;
		
		Set<Role> tmproles = getRoles();

		if (groups != null)
			for (Group g : groups) {
				tmproles.addAll(g.getRoles());
			}
		
		Role role;
		
		Privilege oPrivilege = new Privilege(privilege);
		
		for (Iterator i = tmproles.iterator(); i.hasNext();) {
			role = (Role) i.next();
		
			if (role.hasPrivilege(oPrivilege.getPrivilege()))
				return true;
		}

		return false;
	}
	
	public Collection<Privilege> getPrivileges() {
		Set<Privilege> privileges = new HashSet<Privilege>();
		Set<Role> tmproles = getRoles();

		if (groups != null)
			for (Group g : groups) {
				tmproles.addAll(g.getRoles());
			}
		
		tmproles.addAll(getRoles());
		
		Role role;
		for (Iterator i = tmproles.iterator(); i.hasNext();) {
			role = (Role) i.next();
			Collection<Privilege> privs = role.getPrivileges();
			if (privs != null)
				privileges.addAll(privs);
		}
		
		return privileges;
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
	public void removeRole(Role role) {
		if (roles != null)
			roles.remove(role);
	}

	/**
	 * @return Returns the groups.
	 */
	public Set<Group> getGroups() {
		return groups;
	}

	/**
	 * @param groups The groups to set.
	 */
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	
	/**
	 * Add the given Group to the list of groups for this User
	 * @param groupservation
	 */
	public void addGroup(Group group) {
		if (groups == null)
			groups = new HashSet<Group>();
		if (!groups.contains(group) && group != null)
			groups.add(group);
	}

	/**
	 * Remove the given obervation from the list of groups for this User
	 * @param groupservation
	 */
	public void removeGroup(Group group) {
		if (groups != null)
			groups.remove(group);
	}

	/**
	 * @return Returns the systemId.
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId The systemId to set.
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
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
		return username;
	}

	/**
	 * @return Returns the properties.
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties The properties to set.
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}