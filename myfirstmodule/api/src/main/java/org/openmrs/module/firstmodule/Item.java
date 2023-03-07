/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.firstmodule;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Please note that a corresponding table schema must be created in liquibase.xml.
 */
//Uncomment 2 lines below if you want to make the Item class persistable, see also PetientDaoTest and liquibase.xml
//@Entity(name = "firstmodule.Item")
//@Table(name = "firstmodule_item")
public class Item extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue
	@Column(name = "firstmodule_item_id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "owner")
	private User owner;
	
	@Basic
	@Column(name = "description", length = 255)
	private String description;
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
