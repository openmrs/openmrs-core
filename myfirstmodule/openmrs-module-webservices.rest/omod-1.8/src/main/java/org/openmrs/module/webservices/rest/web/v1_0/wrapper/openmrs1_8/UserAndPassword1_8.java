/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.User;

/**
 * This class is a wrapper for org.openmrs.User and password that needs to be sent for creating a
 * new User by a webservice call. Requires extending BaseOpenmrsMetadata to be able to interact with
 * MetadataDelegatingCrudResource and making instance of metadata type
 * 
 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource
 */
public class UserAndPassword1_8 extends BaseOpenmrsMetadata implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	//Fields
	private String password;
	
	private User user;
	
	//Constructors
	/** default constructor */
	public UserAndPassword1_8() {
		user = new User();
	}
	
	/**
	 * @param user
	 */
	public UserAndPassword1_8(User user) {
		this.user = user;
	}
	
	/**
	 * @return password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return user the User property
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return id
	 */
	@Override
	public Integer getId() {
		return getUser().getId();
	}
	
	/**
	 * @param integer the Id to set
	 */
	@Override
	public void setId(Integer integer) {
		getUser().setId(integer);
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsObject#getUuid()
	 */
	@Override
	public String getUuid() {
		return getUser().getUuid();
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsObject#setUuid(java.lang.String)
	 */
	@Override
	public void setUuid(String uuid) {
		getUser().setUuid(uuid);
	}
}
