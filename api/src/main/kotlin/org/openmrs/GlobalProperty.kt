/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Cacheable
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.openmrs.customdatatype.CustomDatatype
import org.openmrs.customdatatype.CustomDatatypeUtil
import org.openmrs.customdatatype.CustomValueDescriptor
import org.openmrs.customdatatype.SingleCustomValue
import java.util.Date

/**
 * Global properties are simple key-value pairs persisted in the database GPs can be thought of as
 * something similar to environment variables used in operating systems.
 */
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class GlobalProperty() : BaseOpenmrsObject(), CustomValueDescriptor, SingleCustomValue<GlobalProperty> {
	
	var property: String = ""
	
	var propertyValue: String = ""
	
	@Transient
	private var typedValue: Any? = null
	
	// if true, indicates that setValue has been called, and we need to invoke CustomDatatype's save
	private var dirty: Boolean = false
	
	var description: String = ""
	
	override var datatypeClassname: String? = null
	
	override var datatypeConfig: String? = null
	
	override var preferredHandlerClassname: String? = null
	
	override var handlerConfig: String? = null
	
	var changedBy: User? = null
	
	var dateChanged: Date? = null
	
	var viewPrivilege: Privilege? = null
	
	var editPrivilege: Privilege? = null
	
	var deletePrivilege: Privilege? = null
	
	/**
	 * Constructor defining the key for this GP
	 *
	 * @param property key to name the property
	 */
	constructor(property: String) : this() {
		this.property = property
	}
	
	/**
	 * Constructor defining the key and value of this GP
	 *
	 * @param property key to name the property
	 * @param value value to give to the property
	 */
	constructor(property: String, value: String) : this(property) {
		this.propertyValue = value
	}
	
	/**
	 * Constructor defining key/value/description for this GP
	 *
	 * @param property key to name the property
	 * @param value value to give to the property
	 * @param description description of how this property is used
	 */
	constructor(property: String, value: String, description: String) : this(property, value) {
		this.description = description
	}
	
	/**
	 * Constructor defining key/value/description/customDatatype/datatypeConfig
	 *
	 * @param property
	 * @param value
	 * @param description
	 * @param datatypeClass
	 * @param datatypeConfig
	 *
	 * @since 1.9
	 */
	constructor(
		property: String,
		value: String,
		description: String,
		datatypeClass: Class<out CustomDatatype<*>>,
		datatypeConfig: String
	) : this(property, value, description) {
		this.datatypeClassname = datatypeClass.name
		this.datatypeConfig = datatypeConfig
	}
	
	override fun getId(): Int {
		throw UnsupportedOperationException()
	}
	
	override fun setId(id: Int?) {
		throw UnsupportedOperationException()
	}
	
	override fun toString(): String {
		return "property: $property value: $propertyValue"
	}
	
	override fun getDescriptor(): GlobalProperty = this
	
	override fun getValueReference(): String = propertyValue
	
	override fun setValueReferenceInternal(valueToPersist: String?) {
		propertyValue = valueToPersist ?: ""
	}
	
	override fun getValue(): Any? {
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatypeOrDefault(this).fromReferenceString(valueReference)
		}
		return typedValue
	}
	
	override fun <T> setValue(typedValue: T) {
		this.typedValue = typedValue
		dirty = true
	}
	
	/**
	 * @deprecated as of 2.0, use [getDirty]
	 */
	@Deprecated("use getDirty() instead", ReplaceWith("getDirty()"))
	@JsonIgnore
	override fun isDirty(): Boolean = getDirty()
	
	fun getDirty(): Boolean = dirty
	
	companion object {
		private const val serialVersionUID = 1L
	}
}
