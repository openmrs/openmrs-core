/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.io.Serializable;
import java.util.Comparator;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * The FormField object relates/orders the <code>fields</code> on a <code>form</code> A form can
 * have many 0 to n fields associated with it in a hierarchical manor. This FormField object governs
 * what/how that takes place
 *
 * @see org.openmrs.Form
 * @see org.openmrs.Field
 */
public class FormField extends BaseChangeableOpenmrsMetadata implements java.io.Serializable, Comparable<FormField> {
	
	public static final long serialVersionUID = 3456L;
	
	// Fields
	
	protected Integer formFieldId;
	
	protected FormField parent;
	
	protected Form form;
	
	protected Field field;
	
	protected Integer fieldNumber;
	
	protected String fieldPart;
	
	protected Integer pageNumber;
	
	protected Integer minOccurs;
	
	protected Integer maxOccurs;
	
	protected Boolean required = false;
	
	protected Float sortWeight;
	
	// Constructors
	
	/** default constructor */
	public FormField() {
	}
	
	/** constructor with id */
	public FormField(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the formFieldId.
	 */
	public Integer getFormFieldId() {
		return formFieldId;
	}
	
	/**
	 * @param formFieldId The formFieldId to set.
	 */
	public void setFormFieldId(Integer formFieldId) {
		this.formFieldId = formFieldId;
	}
	
	/**
	 * @return Returns the parent FormField.
	 */
	public FormField getParent() {
		return parent;
	}
	
	/**
	 * @param parent The formField to set as parent.
	 */
	public void setParent(FormField parent) {
		this.parent = parent;
	}
	
	/**
	 * @return Returns the form.
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * @param form The form to set.
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	/**
	 * @return Returns the field.
	 */
	public Field getField() {
		return field;
	}
	
	/**
	 * @param field The field to set.
	 */
	public void setField(Field field) {
		this.field = field;
	}
	
	/**
	 * @return Returns the fieldNumber.
	 */
	public Integer getFieldNumber() {
		return fieldNumber;
	}
	
	/**
	 * @param fieldNumber The fieldNumber to set.
	 */
	public void setFieldNumber(Integer fieldNumber) {
		this.fieldNumber = fieldNumber;
	}
	
	/**
	 * @return Returns the fieldPart.
	 */
	public String getFieldPart() {
		return fieldPart;
	}
	
	/**
	 * @param fieldPart The fieldPart to set.
	 */
	public void setFieldPart(String fieldPart) {
		this.fieldPart = fieldPart;
	}
	
	/**
	 * @return Returns the pageNumber.
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}
	
	/**
	 * @param pageNumber The pageNumber to set.
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	/**
	 * @return Returns the minOccurs.
	 */
	public Integer getMinOccurs() {
		return minOccurs;
	}
	
	/**
	 * @param minOccurs The minOccurs to set.
	 */
	public void setMinOccurs(Integer minOccurs) {
		this.minOccurs = minOccurs;
	}
	
	/**
	 * @return Returns the maxOccurs.
	 */
	public Integer getMaxOccurs() {
		return maxOccurs;
	}
	
	/**
	 * @param maxOccurs The maxOccurs to set.
	 */
	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	
	/**
	 * @return Returns the required status.
	 * 
	 * @deprecated as of 2.0, use {@link #getRequired()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isRequired() {
		return getRequired();
	}
	
	/**
	 * @return same as isRequired()
	 */
	public Boolean getRequired() {
		return required == null ? false : required;
	}
	
	/**
	 * @param required The required status to set.
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	/**
	 * @return Returns the sortWeight.
	 */
	public Float getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight The weight to order the formFields on.
	 */
	public void setSortWeight(Float sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (formFieldId == null) {
			return "null";
		}
		
		return this.formFieldId.toString();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getFormFieldId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFormFieldId(id);
		
	}
	
	@Override
	public int compareTo(FormField other) {
		DefaultComparator pnDefaultComparator = new DefaultComparator();
		return pnDefaultComparator.compare(this, other);
	}
	
	/**
	 Provides a default comparator.
	 @since 1.12
	 **/
	public static class DefaultComparator implements Comparator<FormField>, Serializable {

		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(FormField ff1, FormField ff2) {
			if (ff1.getSortWeight() != null || ff2.getSortWeight() != null) {
				if (ff1.getSortWeight() == null) {
					return -1;
				}
				if (ff2.getSortWeight() == null) {
					return 1;
				}
				int c = ff1.getSortWeight().compareTo(ff2.getSortWeight());
				if (c != 0) {
					return c;
				}
			}
			if (ff1.getPageNumber() != null || ff2.getPageNumber() != null) {
				if (ff1.getPageNumber() == null) {
					return -1;
				}
				if (ff2.getPageNumber() == null) {
					return 1;
				}
				int c = ff1.getPageNumber().compareTo(ff2.getPageNumber());
				if (c != 0) {
					return c;
				}
			}
			if (ff1.getFieldNumber() != null || ff2.getFieldNumber() != null) {
				if (ff1.getFieldNumber() == null) {
					return -1;
				}
				if (ff2.getFieldNumber() == null) {
					return 1;
				}
				int c = ff1.getFieldNumber().compareTo(ff2.getFieldNumber());
				if (c != 0) {
					return c;
				}
			}
			if (ff1.getFieldPart() != null || ff2.getFieldPart() != null) {
				if (ff1.getFieldPart() == null) {
					return -1;
				}
				if (ff2.getFieldPart() == null) {
					return 1;
				}
				int c = ff1.getFieldPart().compareTo(ff2.getFieldPart());
				if (c != 0) {
					return c;
				}
			}
			if (ff1.getField() != null && ff2.getField() != null) {
				int c = ff1.getField().getName().compareTo(ff2.getField().getName());
				if (c != 0) {
					return c;
				}
			}
			if (ff1.getFormFieldId() == null && ff2.getFormFieldId() != null) {
				return -1;
			}
			if (ff1.getFormFieldId() != null && ff2.getFormFieldId() == null) {
				return 1;
			}
			if (ff1.getFormFieldId() == null && ff2.getFormFieldId() == null) {
				return 1;
			}
			
			return ff1.getFormFieldId().compareTo(ff2.getFormFieldId());
		}
	}
}
