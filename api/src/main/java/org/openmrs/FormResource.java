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

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.customdatatype.SingleCustomValue;

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
public class FormResource extends BaseOpenmrsObject implements CustomValueDescriptor, SingleCustomValue<FormResource> {

	private static final long serialVersionUID = 1L;

	private Integer formResourceId;
	
	private Form form;
	
	private String name;
	
	private String valueReference;
	
	private String datatypeClassname;
	
	private String datatypeConfig;
	
	private String preferredHandlerClassname;
	
	private String handlerConfig;
	
	private transient boolean dirty = false;
	
	private transient Object typedValue;
	
	private User changedBy;
	
	private Date dateChanged;
	
	public FormResource() {
		// generic constructor
	}
	
	/**
	 * Create a copy of a provided FormResource, ignoring the uuid and id of the original
	 *
	 * @param old the original FormResource to be copied
	 */
	public FormResource(FormResource old) {
		this.form = old.getForm();
		this.name = old.getName();
		this.valueReference = old.getValueReference();
		this.datatypeClassname = old.getDatatypeClassname();
		this.datatypeConfig = old.getDatatypeConfig();
		this.preferredHandlerClassname = old.getPreferredHandlerClassname();
		this.handlerConfig = old.getHandlerConfig();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getFormResourceId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setFormResourceId(id);
	}
	
	/**
	 * gets the form
	 *
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * sets the form
	 *
	 * @param form the form
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	/**
	 * gets the form resource id
	 *
	 * @return the form resource's id
	 */
	public Integer getFormResourceId() {
		return formResourceId;
	}
	
	/**
	 * sets the form resource id
	 *
	 * @param formResourceId the form resource's id
	 */
	public void setFormResourceId(Integer formResourceId) {
		this.formResourceId = formResourceId;
	}
	
	/**
	 * gets the name of the resource
	 *
	 * @return the name of the resource
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * sets the name of the resource
	 *
	 * @param name the name of the resource
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeClassname()
	 */
	@Override
	public String getDatatypeClassname() {
		return datatypeClassname;
	}
	
	/**
	 * @param datatypeClassname the datatypeClassname to set
	 */
	public void setDatatypeClassname(String datatypeClassname) {
		this.datatypeClassname = datatypeClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeConfig()
	 */
	@Override
	public String getDatatypeConfig() {
		return datatypeConfig;
	}
	
	/**
	 * @param datatypeConfig the datatypeConfig to set
	 */
	public void setDatatypeConfig(String datatypeConfig) {
		this.datatypeConfig = datatypeConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getPreferredHandlerClassname()
	 */
	@Override
	public String getPreferredHandlerClassname() {
		return preferredHandlerClassname;
	}
	
	/**
	 * @param preferredHandlerClassname the preferredHandlerClassname to set
	 */
	public void setPreferredHandlerClassname(String preferredHandlerClassname) {
		this.preferredHandlerClassname = preferredHandlerClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getHandlerConfig()
	 */
	@Override
	public String getHandlerConfig() {
		return handlerConfig;
	}
	
	/**
	 * @param handlerConfig the handlerConfig to set
	 */
	public void setHandlerConfig(String handlerConfig) {
		this.handlerConfig = handlerConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getDescriptor()
	 */
	@Override
	public FormResource getDescriptor() {
		return this;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValueReference()
	 */
	@Override
	public String getValueReference() {
		if (valueReference == null) {
			throw new NotYetPersistedException();
		} else {
			return valueReference;
		}
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#getValue()
	 */
	@Override
	public Object getValue() throws InvalidCustomValueException {
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatype(this).fromReferenceString(getValueReference());
		}
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValue(java.lang.Object)
	 */
	@Override
	public <T> void setValue(T typedValue) throws InvalidCustomValueException {
		this.typedValue = typedValue;
		dirty = true;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#setValueReferenceInternal(java.lang.String)
	 */
	@Override
	public void setValueReferenceInternal(String valueToPersist) throws InvalidCustomValueException {
		this.valueReference = valueToPersist;
	}
	
	/**
	 * @see org.openmrs.customdatatype.SingleCustomValue#isDirty()
	 *
	 * @deprecated as of 2.0, use {@link #getDirty()}
	 */
	@Deprecated
	@JsonIgnore
	@Override
	public boolean isDirty() {
		return getDirty();
	}
	
	public boolean getDirty() {
		return dirty;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The user that changed this object
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the date this object was changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The date this object was changed
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
