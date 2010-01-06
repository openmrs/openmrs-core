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
package org.openmrs.api.db;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.serialization.OpenmrsSerializer;

/**
 * Object representation of a Serialized Object as stored in the database.
 */
public class SerializedObject extends BaseOpenmrsMetadata {
	
	private Integer id;
	
	private String type;
	
	private String subtype;
	
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
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the subtype
	 */
	public String getSubtype() {
		return subtype;
	}
	
	/**
	 * @param subtype the subtype to set
	 */
	public void setSubtype(String subtype) {
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
