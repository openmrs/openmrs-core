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
package org.openmrs;

import java.util.Date;

import org.openmrs.api.context.Context;

/**
 * Global properties are simple key-value pairs persisted in the database GPs can be thought of as
 * something similar to environment variables used in operating systems.
 */
public class GlobalProperty {
	
	private Integer globalPropertyId;

	private String property = "";
	
	private String propertyValue = "";
	
	private String description = "";
	
	private String defaultValue = "";
	
	private GlobalPropertyType propertyType = new GlobalPropertyType();
	
	private GlobalPropertyType defaultPropertyType = new GlobalPropertyType();

	private User createdBy = null;
	
	private Date dateCreated = new Date();
	
	private boolean voided = false;
	
	private Date dateVoided = null;
	
	/**
	 * Default empty constructor
	 */
	public GlobalProperty() {
	}
	
	/**
	 * Constructor defining the key for this GP
	 * 
	 * @param property key to name the property
	 */
	public GlobalProperty(String property) {
		this.property = property;
	}
	
	/**
	 * Constructor defining the key and value of this GP
	 * 
	 * @param property key to name the property
	 * @param value value to give to the property
	 */
	public GlobalProperty(String property, String value) {
		this(property);
		this.propertyValue = value;
	}
	
	/**
	 * Constructor defining key/value/description for this GP
	 * 
	 * @param property key to name the property
	 * @param value value to give to the property
	 * @param description description of how this property is used
	 */
	public GlobalProperty(String property, String value, String description) {
		this(property, value);
		this.description = description;
	}
	
	/**
     * @param defaultValue
     * @param description
     * @param property
     * @param propertyType
     * @param propertyValue
     */
    public GlobalProperty(String description, String property, GlobalPropertyType propertyType, String propertyValue) {
	    this(property, propertyValue, description);
	    this.propertyType = propertyType;
    }

	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * @param property The property to set.
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	
	/**
	 * @return Returns the propertyValue.
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	
	/**
	 * @param propertyValue The propertyValue to set.
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof GlobalProperty) {
			GlobalProperty gp = (GlobalProperty) o;
			return (property != null && property.equals(gp.getProperty()));
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.property == null)
			return super.hashCode();
		
		int hash = 5 * this.property.hashCode();
		
		return hash;
	}

	/**
     * @param propertyType the propertyType to set
     */
    public void setPropertyType(GlobalPropertyType propertyType) {
	    this.propertyType = propertyType;
    }

	/**
     * @return the propertyType
     */
    public GlobalPropertyType getPropertyType() {
	    return propertyType;
    }

	/**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
	    this.defaultValue = defaultValue;
    }

	/**
     * @return the defaultValue
     */
    public String getDefaultValue() {
	    return defaultValue;
    }

	
    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
    	return createdBy;
    }

	
    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
    	this.createdBy = createdBy;
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
     * @return the voided
     */
    public boolean isVoided() {
    	return voided;
    }

	
    /**
     * @param voided the voided to set
     */
    public void setVoided(boolean voided) {
    	this.voided = voided;
    }

	
    /**
     * @return the dateVoided
     */
    public Date getDateVoided() {
    	return dateVoided;
    }

	
    /**
     * @param dateVoided the dateVoided to set
     */
    public void setDateVoided(Date dateVoided) {
    	this.dateVoided = dateVoided;
    }

	/**
     * @param globalPropertyId the globalPropertyId to set
     */
    public void setGlobalPropertyId(Integer globalPropertyId) {
	    this.globalPropertyId = globalPropertyId;
    }

	/**
     * @return the globalPropertyId
     */
    public Integer getGlobalPropertyId() {
	    return globalPropertyId;
    }

	/**
     * @param defaultPropertyType the defaultPropertyType to set
     */
    public void setDefaultPropertyType(GlobalPropertyType defaultPropertyType) {
	    this.defaultPropertyType = defaultPropertyType;
    }

	/**
     * @return the defaultPropertyType
     */
    public GlobalPropertyType getDefaultPropertyType() {
	    return defaultPropertyType;
    }
    	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
    
}
