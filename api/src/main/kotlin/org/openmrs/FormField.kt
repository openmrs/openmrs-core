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

import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import java.io.Serializable

/**
 * The FormField object relates/orders the fields on a form. A form can
 * have many 0 to n fields associated with it in a hierarchical manner. This FormField object governs
 * what/how that takes place
 *
 * @see org.openmrs.Form
 * @see org.openmrs.Field
 */
@Audited
open class FormField : BaseChangeableOpenmrsMetadata, Comparable<FormField> {
	
	var formFieldId: Int? = null
	
	var parent: FormField? = null
	
	var form: Form? = null
	
	var field: Field? = null
	
	var fieldNumber: Int? = null
	
	var fieldPart: String? = null
	
	var pageNumber: Int? = null
	
	var minOccurs: Int? = null
	
	var maxOccurs: Int? = null
	
	var required: Boolean? = false
	
	var sortWeight: Float? = null
	
	/** default constructor */
	constructor()
	
	/** constructor with id */
	constructor(formFieldId: Int?) {
		this.formFieldId = formFieldId
	}
	
	/**
	 * @return Returns the required status.
	 * 
	 * @deprecated as of 2.0, use {@link #getRequired()}
	 */
	@Deprecated("use getRequired()", ReplaceWith("getRequired()"))
	@JsonIgnore
	fun isRequired(): Boolean? {
		return getRequired()
	}
	
	/**
	 * @return same as isRequired()
	 */
	fun getRequired(): Boolean {
		return required ?: false
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	override fun toString(): String {
		return formFieldId?.toString() ?: "null"
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	override fun getId(): Int? {
		return formFieldId
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	override fun setId(id: Int?) {
		formFieldId = id
	}
	
	override fun compareTo(other: FormField): Int {
		return DefaultComparator().compare(this, other)
	}
	
	/**
	 * Provides a default comparator.
	 * @since 1.12
	 */
	class DefaultComparator : Comparator<FormField>, Serializable {
		
		override fun compare(ff1: FormField, ff2: FormField): Int {
			// Compare by sortWeight
			if (ff1.sortWeight != null || ff2.sortWeight != null) {
				if (ff1.sortWeight == null) return -1
				if (ff2.sortWeight == null) return 1
				val c = ff1.sortWeight!!.compareTo(ff2.sortWeight!!)
				if (c != 0) return c
			}
			
			// Compare by pageNumber
			if (ff1.pageNumber != null || ff2.pageNumber != null) {
				if (ff1.pageNumber == null) return -1
				if (ff2.pageNumber == null) return 1
				val c = ff1.pageNumber!!.compareTo(ff2.pageNumber!!)
				if (c != 0) return c
			}
			
			// Compare by fieldNumber
			if (ff1.fieldNumber != null || ff2.fieldNumber != null) {
				if (ff1.fieldNumber == null) return -1
				if (ff2.fieldNumber == null) return 1
				val c = ff1.fieldNumber!!.compareTo(ff2.fieldNumber!!)
				if (c != 0) return c
			}
			
			// Compare by fieldPart
			if (ff1.fieldPart != null || ff2.fieldPart != null) {
				if (ff1.fieldPart == null) return -1
				if (ff2.fieldPart == null) return 1
				val c = ff1.fieldPart!!.compareTo(ff2.fieldPart!!)
				if (c != 0) return c
			}
			
			// Compare by field name
			if (ff1.field != null && ff2.field != null) {
				val c = ff1.field!!.name!!.compareTo(ff2.field!!.name!!)
				if (c != 0) return c
			}
			
			// Compare by formFieldId
			if (ff1.formFieldId == null && ff2.formFieldId != null) return -1
			if (ff1.formFieldId != null && ff2.formFieldId == null) return 1
			if (ff1.formFieldId == null && ff2.formFieldId == null) return 1
			
			return ff1.formFieldId!!.compareTo(ff2.formFieldId!!)
		}
		
		companion object {
			private const val serialVersionUID = 1L
		}
	}
	
	companion object {
		const val serialVersionUID = 3456L
	}
}
