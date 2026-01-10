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
import org.hibernate.envers.Audited
import java.util.Date

/**
 * FieldAnswer
 * 
 * @version 1.0
 */
@Entity
@Table(name = "field_answer")
@Audited
open class FieldAnswer : BaseOpenmrsObject {
	
	@Transient
	private var dirty: Boolean = false
	
	@Column(name = "date_created", length = 19)
	var dateCreated: Date? = null
		set(value) {
			dirty = true
			field = value
		}
	
	@ManyToOne
	@JoinColumn(name = "answer_id")
	@Id
	var concept: Concept? = null
		set(value) {
			dirty = true
			field = value
		}
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	var creator: User? = null
		set(value) {
			dirty = true
			field = value
		}
	
	@ManyToOne
	@JoinColumn(name = "field_id")
	@Id
	var answerField: Field? = null
		set(value) {
			dirty = true
			field = value
		}
	
	/** default constructor */
	constructor()
	
	/**
	 * @return Returns the field.
	 */
	fun getField(): Field? {
		return answerField
	}
	
	/**
	 * @param field The field to set.
	 */
	fun setField(field: Field?) {
		this.answerField = field
	}
	
	/**
	 * @return boolean whether or not this fieldAnswer has been modified
	 *
	 * @deprecated as of 2.0, use {@link #getDirty()}
	 */
	@Deprecated("use getDirty()", ReplaceWith("getDirty()"))
	@JsonIgnore
	fun isDirty(): Boolean {
		return getDirty()
	}
	
	/**
	 * @return boolean whether or not this fieldAnswer has been modified
	 */
	fun getDirty(): Boolean {
		return dirty
	}
	
	fun setClean() {
		dirty = false
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	override fun getId(): Int? {
		throw UnsupportedOperationException()
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	override fun setId(id: Int?) {
		throw UnsupportedOperationException()
	}
	
	companion object {
		const val serialVersionUID = 5656L
	}
}
