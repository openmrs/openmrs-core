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

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.serialization.OpenmrsSerializer;

/**
 * Object representation of a Serialized Object as stored in the database.
 */
public class SerializedObject extends BaseOpenmrsMetadata {
	
	private Integer id;
	
	private Class<? extends OpenmrsObject> type;
	
	private Class<? extends OpenmrsObject> subtype;
	
	private Class<? extends OpenmrsSerializer> serializationClass;
	
	private String serializedData;
	
	/**
	 * Default Constructor
	 */
	public SerializedObject() {
	}
	
	//***** Instance methods
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Serialized " + subtype + " named <" + getName() + ">";
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
	
}
