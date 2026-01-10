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

import org.hibernate.envers.Audited

/**
 * Form
 *
 * @version 1.0
 */
@Audited
open class Form : BaseChangeableOpenmrsMetadata {
	
	var formId: Int? = null
	
	var version: String? = null
	
	var build: Int? = null
	
	var published: Boolean? = false
	
	var encounterType: EncounterType? = null
	
	var formFields: MutableSet<FormField>? = null
	
	/** default constructor */
	constructor()
	
	/**
	 * Constructor with id
	 *
	 * <strong>Should</strong> set formId with given parameter
	 */
	constructor(formId: Int?) {
		this.formId = formId
	}
	
	/**
	 * @return Returns the formFields.
	 */
	fun getOrderedFormFields(): List<FormField>? {
		return formFields?.let { fields ->
			val fieldSet = fields.toMutableSet()
			val fieldList = mutableListOf<FormField>()
			val fieldSize = fieldSet.size
			
			repeat(fieldSize) {
				var fieldNum = 0
				var next: FormField? = null
				
				for (ff in fieldSet) {
					val ffFieldNumber = ff.fieldNumber
					if (ffFieldNumber != null) {
						if (ffFieldNumber < fieldNum || fieldNum == 0) {
							fieldNum = ffFieldNumber
							next = ff
						}
					} else {
						if (fieldNum == 0) {
							next = ff
						}
					}
				}
				
				next?.let {
					fieldList.add(it)
					fieldSet.remove(it)
				}
			}
			
			fieldList
		}
	}
	
	/**
	 * Adds a FormField to the list of form fields
	 *
	 * @param formField FormField to be added
	 */
	fun addFormField(formField: FormField?) {
		if (formFields == null) {
			formFields = mutableSetOf()
		}
		formField?.let {
			if (!formFields!!.contains(it)) {
				it.form = this
				formFields!!.add(it)
			}
		}
	}
	
	/**
	 * Removes a FormField from the list of form fields
	 *
	 * @param formField FormField to be removed
	 */
	fun removeFormField(formField: FormField?) {
		formFields?.remove(formField)
	}
	
	override fun toString(): String {
		return formId?.toString() ?: ""
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	override fun getId(): Int? {
		return formId
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	override fun setId(id: Int?) {
		formId = id
	}
	
	companion object {
		const val serialVersionUID = 845634L
	}
}
