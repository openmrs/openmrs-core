/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
