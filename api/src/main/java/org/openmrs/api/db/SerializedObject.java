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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.openmrs.serialization.OpenmrsSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Object representation of a Serialized Object as stored in the database.
 */
@Entity
@Table(name = "serialized_object")
public class SerializedObject extends BaseChangeableOpenmrsMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serialized_object_id_seq")
	@GenericGenerator(
		name = "serialized_object_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "serialized_object_serialized_object_id_seq")
	)
	@Column(name = "serialized_object_id")
	private Integer id;

	@Column(name = "type", nullable = false, length = 255)
	private String type;

	@Column(name = "subtype", nullable = false, length = 255)
	private String subtype;

	@Column(name = "serialization_class", nullable = false, length = 255)
	private Class<? extends OpenmrsSerializer> serializationClass;

	@Column(name = "serialized_data", length = 16777215)
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
	@Override
	public Integer getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	@Override
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
