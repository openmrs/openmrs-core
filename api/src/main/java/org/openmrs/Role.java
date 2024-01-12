/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.openmrs.util.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A Role is just an aggregater of {@link Privilege}s. {@link User}s contain a number of roles
 * (Users DO NOT contain any privileges directly) Roles can be grouped by inheriting other roles. If
 * a user is given Role A that inherits from Role B, the user has all rights/abilities for both Role
 * A's privileges and for Role B's privileges.
 *
 * @see Privilege
 */

@Entity
@Table(name="role")
public class Role extends BaseOpenmrsObject implements Auditable, Retireable {
	
	public static final long serialVersionUID = 1234233L;
	
	private static final Logger log = LoggerFactory.getLogger(Role.class);
	
	@Id
	@Column(name = "role")
	private String role;
	
	@Column(name = "description", length = 255)
	private String description;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "role_privilege",
		joinColumns = @JoinColumn(name = "role"),
		inverseJoinColumns = @JoinColumn(name = "privilege")
	)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private Set<Privilege> privileges;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "role_role",
		joinColumns = @JoinColumn(name = "child_role"),
		inverseJoinColumns = @JoinColumn(name = "parent_role")
	)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private Set<Role> inheritedRoles;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "role_role",
		joinColumns = @JoinColumn(name = "parent_role"),
		inverseJoinColumns = @JoinColumn(name = "child_role")
	)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private Set<Role> childRoles;
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;
	
	@Column(name = "date_created", nullable = false, length = 19)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed", length = 19)
	private Date dateChanged;
	
	@Column(name = "retired", nullable = false, length = 1)
	private boolean retired;
	
	@ManyToOne
	@JoinColumn(name = "retired_by")
	private User retiredBy;
	
	@Column(name = "date_retired", length = 19)
	private Date dateRetired;
	
	@Column(name = "retire_reason", length = 255)
	private String retireReason;
	
	// Constructors
	
	/** default constructor */
	public Role() {
	}
	
	/** constructor with id */
	public Role(String role) {
		this.role = role;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/** constructor with all database required properties */
	public Role(String role, String description) {
		this.role = role;
		setDescription(description);
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
	
	public String getName() {
		return this.getRole();
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
	}
		
	/**
	 * Adds the given Privilege to the list of privileges
	 *
	 * @param privilege Privilege to add
	 */
	public void addPrivilege(Privilege privilege) {
		if (privileges == null) {
			privileges = new HashSet<>();
		}
		if (privilege != null && !containsPrivilege(privileges, privilege.getPrivilege())) {
			privileges.add(privilege);
		}
	}
	
	private boolean containsPrivilege(Collection<Privilege> privileges, String privilegeName) {
		for (Privilege privilege : privileges) {
			if (privilege.getPrivilege().equals(privilegeName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes the given Privilege from the list of privileges
	 *
	 * @param privilege Privilege to remove
	 */
	public void removePrivilege(Privilege privilege) {
		if (privileges != null) {
			privileges.remove(privilege);
		}
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
	@Override
	public String toString() {
		return this.role;
	}
	
	/**
	 * Looks for the given <code>privilegeName</code> privilege name in this roles privileges. This
	 * method does not recurse through the inherited roles
	 *
	 * @param privilegeName String name of a privilege
	 * @return true/false whether this role has the given privilege
	 * <strong>Should</strong> return false if not found
	 * <strong>Should</strong> return true if found
	 * <strong>Should</strong> not fail given null parameter
	 * <strong>Should</strong> return true for any privilegeName if super user
	 */
	public boolean hasPrivilege(String privilegeName) {
		
		if (RoleConstants.SUPERUSER.equals(this.role)) {
			return true;
		}
		
		if (privileges != null) {
			for (Privilege p : privileges) {
				if (p.getPrivilege().equalsIgnoreCase(privilegeName)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * @return Returns the inheritedRoles.
	 */
	public Set<Role> getInheritedRoles() {
		if (inheritedRoles == null) {
			inheritedRoles = new HashSet<>();
		}
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
		return getInheritedRoles() != null && !getInheritedRoles().isEmpty();
	}
	
	/**
	 * Recursive (if need be) method to return all parent roles of this role
	 *
	 * <strong>Should</strong> only return parent roles
	 * @return Return this role's parents
	 */
	public Set<Role> getAllParentRoles() {
		Set<Role> parents = new HashSet<>();
		if (inheritsRoles()) {
			parents.addAll(this.recurseOverParents(parents));
		}
		return parents;
	}
	
	/**
	 * Returns the full set of roles be looping over inherited roles. Duplicate roles are dropped.
	 *
	 * @param total Roles already looped over
	 * @return Set&lt;Role&gt; Current and inherited roles
	 */
	public Set<Role> recurseOverParents(final Set<Role> total) {
		if (!this.inheritsRoles()) {
			return total;
		}

		Set<Role> allRoles = new HashSet<>(total);
		Set<Role> myRoles = new HashSet<>(this.getInheritedRoles());
		myRoles.removeAll(total);
		// prevent an obvious looping problem
		myRoles.remove(this); 
		allRoles.addAll(myRoles);
		
		for (Role r : myRoles) {
			if (r.inheritsRoles()) {
				allRoles.addAll(r.recurseOverParents(allRoles));
			}
		}
		
		log.debug("Total roles: {}", allRoles);
		
		return allRoles;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.9
	 * @return immediate children
	 */
	public Set<Role> getChildRoles() {
		if (childRoles == null) {
			childRoles = new HashSet<>();
		}
		return childRoles;
	}
	
	/**
	 * @since 1.9
	 * @param childRoles the immediate children to set
	 */
	public void setChildRoles(Set<Role> childRoles) {
		this.childRoles = childRoles;
	}
	
	/**
	 * Convenience method to test whether or not this role is a parent of another role
	 *
	 * @return true/false whether this role is a parent of another role
	 * @since 1.9
	 */
	public boolean hasChildRoles() {
		return getChildRoles() != null && !getChildRoles().isEmpty();
	}
	
	/**
	 * Recursive (if need be) method to return all child roles of this role
	 *
	 * <strong>Should</strong> only return child roles
	 * @return this role's children
	 * @since 1.9
	 */
	public Set<Role> getAllChildRoles() {
		Set<Role> children = new HashSet<>();
		if (hasChildRoles()) {
			children.addAll(this.recurseOverChildren(children));
		}
		return children;
	}
	
	/**
	 * Returns the full set of child roles be looping over children. Duplicate roles are dropped.
	 *
	 * @param total Roles already looped over
	 * @return Set&lt;Role&gt; Current and child roles
	 * @since 1.9
	 */
	public Set<Role> recurseOverChildren(final Set<Role> total) {
		if (!this.hasChildRoles()) {
			return total;
		}

		Set<Role> allRoles = new HashSet<>(total);

		Set<Role> myRoles = new HashSet<>(this.getChildRoles());
		myRoles.removeAll(total);
		// prevent an obvious looping problem
		myRoles.remove(this); 
		allRoles.addAll(myRoles);
		
		for (Role r : myRoles) {
			if (r.hasChildRoles()) {
				allRoles.addAll(r.recurseOverChildren(allRoles));
			}
		}
		
		log.debug("Total roles: {}", allRoles);
		
		return allRoles;
	}
	
	/**
	 * @since 2.7.0
	 * @return User - the user who last changed the Role
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @since 2.7.0
	 * @param changedBy - the user who last changed the Role
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @since 2.7.0
	 * @return Date - the date the Role was last changed
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @since 2.7.0
	 * @param dateChanged - the date the Role was last changed
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @since 2.7.0
	 * @return User - the user who created the Role
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @since 2.7.0
	 * @param creator - the user who created the Role
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @since 2.7.0
	 * @return Date - the date the Role was created
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @since 2.7.0
	 * @param dateCreated - the date the Role was created
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @since 2.7.0
	 * @return Boolean - whether of not this object is retired
	 * @deprecated as of 2.0, use {@link #getRetired()}
	 */
	@Override
	public Boolean isRetired() {
		return retired;
	}
	
	@Override
	public Boolean getRetired() {
		return retired;
	}
	
	/**
	 * @since 2.7.0
	 * @param retired - whether of not this Role is retired
	 */
	@Override
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @since 2.7.0
	 * @return User - the user who retired the Role
	 */
	@Override
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @since 2.7.0
	 * @param retiredBy - the user who retired the Role
	 */
	@Override
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @since 2.7.0
	 * @return Date - the date the Role was retired
	 */
	@Override
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @since 2.7.0
	 * @param dateRetired - the date the Role was retired
	 */
	@Override
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @since 2.7.0
	 * @return String - the reason the Role was retired
	 */
	@Override
	public String getRetireReason() {
		return retireReason;
	}
	
	/**
	 * @since 2.7.0
	 * @param retireReason - the reason the Role was retired
	 */
	@Override
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
}
