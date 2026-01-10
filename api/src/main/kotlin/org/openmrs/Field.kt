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

/**
 * Field
 *
 * @version 1.0
 */
@Audited
@Entity
@Table(name = "field")
open class Field : BaseChangeableOpenmrsMetadata {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "field_id")
	var fieldId: Int? = null
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "field_type", nullable = false)
	var fieldType: FieldType? = null
	
	@ManyToOne
	@JoinColumn(name = "concept_id")
	var concept: Concept? = null
	
	@Column(name = "table_name", length = 50)
	var tableName: String? = null
	
	@Column(name = "attribute_name", length = 50)
	var attributeName: String? = null
	
	@Column(name = "default_value", length = 65535)
	var defaultValue: String? = null
	
	@Column(name = "select_multiple", nullable = false)
	var selectMultiple: Boolean? = false
	
	/** default constructor */
	constructor()
	
	/** constructor with id */
	constructor(fieldId: Int?) {
		this.fieldId = fieldId
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getSelectMultiple()}
	 */
	@Deprecated("use getSelectMultiple()", ReplaceWith("selectMultiple"))
	@JsonIgnore
	fun isSelectMultiple(): Boolean? {
		return selectMultiple
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	override fun getId(): Int? {
		return fieldId
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	override fun setId(id: Int?) {
		fieldId = id
	}
	
	companion object {
		const val serialVersionUID = 4454L
	}
}
