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

import jakarta.persistence.*
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.envers.Audited
import org.openmrs.customdatatype.CustomDatatypeUtil
import org.openmrs.customdatatype.CustomValueDescriptor
import org.openmrs.customdatatype.NotYetPersistedException
import org.openmrs.customdatatype.SingleCustomValue
import java.util.Date

/**
 * A FormResource is meant as a way for modules to add arbitrary information to
 * a Form. FormResources are essentially just key-value pairs. The value is
 * stored as a custom datatype. A Form can have 0-n FormResources but only one
 * FormResource per name per Form.
 *
 * The <i>name</i> of a resource specifies one of many resources that can be
 * stored for a particular owner. Only one resource for each name will
 * ever be saved.
 *
 * @since 1.9
 */
@Entity
@Table(name = "form_resource")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AttributeOverride(name = "uuid", column = Column(name = "uuid", unique = true, nullable = false, length = 38))
@Audited
open class FormResource : BaseOpenmrsObject, CustomValueDescriptor, SingleCustomValue<FormResource> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "form_resource_form_resource_id_seq")
	@GenericGenerator(
		name = "form_resource_form_resource_id_seq",
		strategy = "native",
		parameters = [Parameter(name = "sequence", value = "form_resource_form_resource_id_seq")]
	)
	@Column(name = "form_resource_id")
	var formResourceId: Int? = null
	
	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	var form: Form? = null
	
	@Column(name = "name", length = 255, nullable = true)
	var name: String? = null
	
	@Lob
	@Column(name = "value_reference", length = 65535, nullable = true)
	private var valueReference: String? = null
	
	@Column(name = "datatype", length = 255)
	override var datatypeClassname: String? = null
	
	@Lob
	@Column(name = "datatype_config", length = 65535)
	override var datatypeConfig: String? = null
	
	@Column(name = "preferred_handler", length = 255)
	override var preferredHandlerClassname: String? = null
	
	@Lob
	@Column(name = "handler_config", length = 65535)
	override var handlerConfig: String? = null
	
	@Transient
	private var dirty = false
	
	@Transient
	private var typedValue: Any? = null
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	var changedBy: User? = null
	
	@Column(name = "date_changed", length = 19)
	var dateChanged: Date? = null
	
	constructor()
	
	/**
	 * Create a copy of a provided FormResource, ignoring the uuid and id of the original
	 *
	 * @param old the original FormResource to be copied
	 */
	constructor(old: FormResource) {
		this.form = old.form
		this.name = old.name
		this.valueReference = old.getValueReference()
		this.datatypeClassname = old.datatypeClassname
		this.datatypeConfig = old.datatypeConfig
		this.preferredHandlerClassname = old.preferredHandlerClassname
		this.handlerConfig = old.handlerConfig
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	override fun getId(): Int? {
		return formResourceId
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	override fun setId(id: Int?) {
		formResourceId = id
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getDescriptor()
	 */
	override fun getDescriptor(): FormResource {
		return this
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValueReference()
	 */
	override fun getValueReference(): String {
		return valueReference ?: throw NotYetPersistedException()
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValue()
	 */
	override fun getValue(): Any? {
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatype(this).fromReferenceString(getValueReference())
		}
		return typedValue
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValue(java.lang.Object)
	 */
	override fun <T> setValue(typedValue: T) {
		this.typedValue = typedValue
		dirty = true
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReferenceInternal(java.lang.String)
	 */
	override fun setValueReferenceInternal(valueToPersist: String?) {
		this.valueReference = valueToPersist
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#isDirty()
	 *
	 * @deprecated as of 2.0, use {@link #getDirty()}
	 */
	@Deprecated("use getDirty()", ReplaceWith("getDirty()"))
	@JsonIgnore
	override fun isDirty(): Boolean {
		return getDirty()
	}
	
	fun getDirty(): Boolean {
		return dirty
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}
