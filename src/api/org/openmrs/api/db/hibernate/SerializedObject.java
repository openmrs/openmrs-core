/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.Date;

import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.serialization.OpenmrsSerializer;

/**
 * Object representation of a Serialized Object as stored in the database.
 */
public class SerializedObject {
	
	private Integer id;
	private String name;
	private String description;
	private Class<? extends OpenmrsObject> type;
	private Class<? extends OpenmrsObject> subtype;
	private Class<? extends OpenmrsSerializer> serializationClass;
	private String serializedData;
	private User creator;	
	private Date dateCreated;
	private User changedBy;	
	private Date dateChanged;	
	private Boolean retired = false;	
	private User retiredBy;
	private Date dateRetired;
	private String retireReason;
	
	/**
	 * Default Constructor
	 */
	public SerializedObject() { }
	
	//***** Instance methods
	
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "Serialized " + subtype + " named <" + name + ">";
    }
	
	//***** Property accessors
	
	/**
     * @return the id
     */
    public Integer getId() {
    	return id;
    }

	/**
     * @param id the id to set
     */
    public void setId(Integer id) {
    	this.id = id;
    }

	/**
     * @return the name
     */
    public String getName() {
    	return name;
    }
	
    /**
     * @param name the name to set
     */
    public void setName(String name) {
    	this.name = name;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
    	return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
    	this.description = description;
    }

	/**
     * @return the type
     */
    public Class<? extends OpenmrsObject> getType() {
    	return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Class<? extends OpenmrsObject> type) {
    	this.type = type;
    }

    /**
     * @return the subtype
     */
    public Class<? extends OpenmrsObject> getSubtype() {
    	return subtype;
    }
	
    /**
     * @param subtype the subtype to set
     */
    public void setSubtype(Class<? extends OpenmrsObject> subtype) {
    	this.subtype = subtype;
    }
    
    /**
     * @return the serializationClass
     */
    public Class<? extends OpenmrsSerializer> getSerializationClass() {
    	return serializationClass;
    }

    /**
     * @param serializationClass the serializationClass to set
     */
    public void setSerializationClass(Class<? extends OpenmrsSerializer> serializationClass) {
    	this.serializationClass = serializationClass;
    }

	/**
     * @return the serializedData
     */
    public String getSerializedData() {
    	return serializedData;
    }

    /**
     * @param serializedData the serializedData to set
     */
    public void setSerializedData(String serializedData) {
    	this.serializedData = serializedData;
    }

	/**
     * @return the creator
     */
    public User getCreator() {
    	return creator;
    }
	
    /**
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
    	this.creator = creator;
    }
	
    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
    	return dateCreated;
    }
	
    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
    	this.dateCreated = dateCreated;
    }
	
    /**
     * @return the changedBy
     */
    public User getChangedBy() {
    	return changedBy;
    }
	
    /**
     * @param changedBy the changedBy to set
     */
    public void setChangedBy(User changedBy) {
    	this.changedBy = changedBy;
    }
	
    /**
     * @return the dateChanged
     */
    public Date getDateChanged() {
    	return dateChanged;
    }
	
    /**
     * @param dateChanged the dateChanged to set
     */
    public void setDateChanged(Date dateChanged) {
    	this.dateChanged = dateChanged;
    }
	
    /**
     * @return the retired
     */
    public Boolean getRetired() {
    	return retired;
    }

    /**
     * @param retired the retired to set
     */
    public void setRetired(Boolean retired) {
    	this.retired = retired;
    }
	
    /**
     * @return the retiredBy
     */
    public User getRetiredBy() {
    	return retiredBy;
    }

    /**
     * @param retiredBy the retiredBy to set
     */
    public void setRetiredBy(User retiredBy) {
    	this.retiredBy = retiredBy;
    }

    /**
     * @return the dateRetired
     */
    public Date getDateRetired() {
    	return dateRetired;
    }
	
    /**
     * @param dateRetired the dateRetired to set
     */
    public void setDateRetired(Date dateRetired) {
    	this.dateRetired = dateRetired;
    }
	
    /**
     * @return the retireReason
     */
    public String getRetireReason() {
    	return retireReason;
    }
	
    /**
     * @param retireReason the retireReason to set
     */
    public void setRetireReason(String retireReason) {
    	this.retireReason = retireReason;
    }
}
